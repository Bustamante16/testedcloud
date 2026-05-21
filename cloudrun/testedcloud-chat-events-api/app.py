import json
import os
from datetime import datetime, timezone
from typing import Any, Dict, Optional
from uuid import uuid4

from fastapi import FastAPI, Header, HTTPException, Request
from firebase_admin import auth as firebase_auth
from firebase_admin import initialize_app
from google.cloud import pubsub_v1
from pydantic import BaseModel, Field


SUPPORTED_EVENT_TYPES = {
    "conversation_created",
    "message_sent",
    "conversation_deleted_for_user",
}

GCP_PROJECT_ID = os.getenv("GCP_PROJECT_ID", "")
PUBSUB_TOPIC_ID = os.getenv("PUBSUB_TOPIC_ID", "")
PUBSUB_ENABLED = os.getenv("PUBSUB_ENABLED", "false").lower() == "true"

FIREBASE_PROJECT_ID = os.getenv("FIREBASE_PROJECT_ID", "")
FIREBASE_TOKEN_VALIDATION_ENABLED = (
    os.getenv("FIREBASE_TOKEN_VALIDATION_ENABLED", "false").lower() == "true"
)

publisher = pubsub_v1.PublisherClient() if PUBSUB_ENABLED else None

firebase_app = None
if FIREBASE_TOKEN_VALIDATION_ENABLED:
    if not FIREBASE_PROJECT_ID:
        raise RuntimeError("FIREBASE_PROJECT_ID is required when Firebase token validation is enabled")

    firebase_app = initialize_app(options={"projectId": FIREBASE_PROJECT_ID})


class AnalyticsEvent(BaseModel):
    event_id: str = Field(default_factory=lambda: str(uuid4()))
    event_type: str
    source: str
    origin: Optional[str] = "firebase"
    user_id: str
    conversation_id: Optional[str] = None
    message_id: Optional[str] = None
    created_at: str
    metadata: Dict[str, Any] = Field(default_factory=dict)


app = FastAPI(
    title="TestedCloud Chat Analytics Collector",
    version="0.1.0",
)


def utc_now_iso() -> str:
    return datetime.now(timezone.utc).isoformat()


def reject_if_sensitive_metadata(event: AnalyticsEvent) -> None:
    forbidden_keys = {
        "message_text",
        "text",
        "body",
        "email",
        "recipient_email",
        "participant_email",
        "participant_emails",
        "display_name",
        "participant_display_name",
        "participant_display_names",
        "token",
        "auth_token",
        "id_token",
        "password",
    }

    normalized_keys = {key.lower() for key in event.metadata.keys()}
    blocked = forbidden_keys.intersection(normalized_keys)

    if blocked:
        raise HTTPException(
            status_code=400,
            detail={
                "accepted": False,
                "error": "sensitive_metadata_not_allowed",
                "blocked_keys": sorted(blocked),
            },
        )


def validate_event(event: AnalyticsEvent) -> None:
    if event.event_type not in SUPPORTED_EVENT_TYPES:
        raise HTTPException(
            status_code=400,
            detail={
                "accepted": False,
                "error": "invalid_event_type",
                "supported_event_types": sorted(SUPPORTED_EVENT_TYPES),
            },
        )

    if event.source != "testedcloud-chat-android":
        raise HTTPException(
            status_code=400,
            detail={
                "accepted": False,
                "error": "invalid_source",
                "expected_source": "testedcloud-chat-android",
            },
        )

    if not event.user_id.strip():
        raise HTTPException(
            status_code=400,
            detail={"accepted": False, "error": "missing_user_id"},
        )

    if not event.created_at.strip():
        raise HTTPException(
            status_code=400,
            detail={"accepted": False, "error": "missing_created_at"},
        )

    reject_if_sensitive_metadata(event)

    if event.event_type in {
        "conversation_created",
        "message_sent",
        "conversation_deleted_for_user",
    } and not event.conversation_id:
        raise HTTPException(
            status_code=400,
            detail={"accepted": False, "error": "missing_conversation_id"},
        )

    if event.event_type == "message_sent":
        if "message_length" not in event.metadata:
            raise HTTPException(
                status_code=400,
                detail={"accepted": False, "error": "missing_message_length"},
            )

    if event.event_type == "conversation_deleted_for_user":
        if event.metadata.get("delete_scope") != "for_me":
            raise HTTPException(
                status_code=400,
                detail={
                    "accepted": False,
                    "error": "invalid_delete_scope",
                    "expected": "for_me",
                },
            )

        if event.metadata.get("delete_model") != "deletedAtByUser":
            raise HTTPException(
                status_code=400,
                detail={
                    "accepted": False,
                    "error": "invalid_delete_model",
                    "expected": "deletedAtByUser",
                },
            )





def validate_firebase_authorization(
    authorization: Optional[str],
    event: AnalyticsEvent,
) -> Dict[str, Any]:
    if not FIREBASE_TOKEN_VALIDATION_ENABLED:
        return {
            "enabled": False,
            "uid": None,
        }

    if not authorization:
        raise HTTPException(
            status_code=401,
            detail={
                "accepted": False,
                "error": "missing_authorization_header",
            },
        )

    if not authorization.startswith("Bearer "):
        raise HTTPException(
            status_code=401,
            detail={
                "accepted": False,
                "error": "invalid_authorization_header",
                "expected": "Bearer <Firebase ID token>",
            },
        )

    token = authorization.replace("Bearer ", "", 1).strip()
    if not token:
        raise HTTPException(
            status_code=401,
            detail={
                "accepted": False,
                "error": "missing_bearer_token",
            },
        )

    try:
        decoded_token = firebase_auth.verify_id_token(token, app=firebase_app)
    except Exception as exc:
        raise HTTPException(
            status_code=401,
            detail={
                "accepted": False,
                "error": "firebase_token_verification_failed",
                "message": str(exc),
            },
        ) from exc

    token_uid = decoded_token.get("uid")
    if not token_uid:
        raise HTTPException(
            status_code=401,
            detail={
                "accepted": False,
                "error": "firebase_token_missing_uid",
            },
        )

    if token_uid != event.user_id:
        raise HTTPException(
            status_code=403,
            detail={
                "accepted": False,
                "error": "firebase_uid_mismatch",
            },
        )

    return {
        "enabled": True,
        "uid": token_uid,
    }


def event_to_payload(event: AnalyticsEvent, received_at: str) -> Dict[str, Any]:
    return {
        "event_id": event.event_id,
        "event_type": event.event_type,
        "source": event.source,
        "origin": event.origin,
        "user_id": event.user_id,
        "conversation_id": event.conversation_id,
        "message_id": event.message_id,
        "created_at": event.created_at,
        "received_at": received_at,
        "metadata": event.metadata,
    }


def publish_event(event: AnalyticsEvent, received_at: str) -> str:
    if not PUBSUB_ENABLED:
        return ""

    if not GCP_PROJECT_ID or not PUBSUB_TOPIC_ID:
        raise HTTPException(
            status_code=500,
            detail={
                "accepted": False,
                "error": "pubsub_config_missing",
            },
        )

    if publisher is None:
        raise HTTPException(
            status_code=500,
            detail={
                "accepted": False,
                "error": "pubsub_publisher_not_initialized",
            },
        )

    topic_path = publisher.topic_path(GCP_PROJECT_ID, PUBSUB_TOPIC_ID)
    payload = event_to_payload(event, received_at)
    data = json.dumps(payload, default=str).encode("utf-8")

    try:
        future = publisher.publish(
            topic_path,
            data,
            event_type=event.event_type,
            source=event.source,
        )
        return future.result(timeout=10)
    except Exception as exc:
        raise HTTPException(
            status_code=500,
            detail={
                "accepted": False,
                "error": "pubsub_publish_failed",
                "message": str(exc),
            },
        ) from exc

@app.get("/health")
def health() -> Dict[str, Any]:
    return {
        "health": "healthy",
        "service": "testedcloud-chat-events-api",
        "version": "0.1.0",
        "pubsub_enabled": PUBSUB_ENABLED,
        "pubsub_topic": PUBSUB_TOPIC_ID if PUBSUB_ENABLED else None,
        "gcp_project": GCP_PROJECT_ID if PUBSUB_ENABLED else None,
        "firebase_token_validation_enabled": FIREBASE_TOKEN_VALIDATION_ENABLED,
        "firebase_project_id": FIREBASE_PROJECT_ID if FIREBASE_TOKEN_VALIDATION_ENABLED else None,
        "timestamp": utc_now_iso(),
    }


@app.post("/events")
def collect_event(
    event: AnalyticsEvent,
    authorization: Optional[str] = Header(default=None),
) -> Dict[str, Any]:
    validate_event(event)
    auth_context = validate_firebase_authorization(authorization, event)

    received_at = utc_now_iso()
    message_id = publish_event(event, received_at)

    return {
        "accepted": True,
        "event_id": event.event_id,
        "event_type": event.event_type,
        "target": "google-cloud-pubsub" if PUBSUB_ENABLED else "local-validation-only",
        "pubsub_published": PUBSUB_ENABLED,
        "pubsub_message_id": message_id if PUBSUB_ENABLED else None,
        "topic": PUBSUB_TOPIC_ID if PUBSUB_ENABLED else None,
        "firebase_token_validation_enabled": FIREBASE_TOKEN_VALIDATION_ENABLED,
        "authenticated_uid": auth_context.get("uid"),
        "received_at": received_at,
    }
