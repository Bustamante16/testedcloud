from datetime import datetime, timezone
from typing import Any, Dict, Optional
from uuid import uuid4

from fastapi import FastAPI, HTTPException
from pydantic import BaseModel, Field


SUPPORTED_EVENT_TYPES = {
    "conversation_created",
    "message_sent",
    "conversation_deleted_for_user",
}


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



@app.get("/health")
def health() -> Dict[str, Any]:
    return {
        "health": "healthy",
        "service": "testedcloud-chat-events-api",
        "version": "0.1.0",
        "pubsub_enabled": False,
        "firebase_token_validation_enabled": False,
        "timestamp": utc_now_iso(),
    }


@app.post("/events")
def collect_event(event: AnalyticsEvent) -> Dict[str, Any]:
    validate_event(event)

    return {
        "accepted": True,
        "event_id": event.event_id,
        "event_type": event.event_type,
        "target": "local-validation-only",
        "pubsub_published": False,
        "received_at": utc_now_iso(),
    }
