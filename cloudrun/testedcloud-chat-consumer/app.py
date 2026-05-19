import base64
import json
import os
from datetime import datetime, timezone
from typing import Any, Dict

from fastapi import FastAPI, HTTPException, Request
from google.cloud import bigquery


GCP_PROJECT_ID = os.getenv("GCP_PROJECT_ID", "majestic-layout-255620")
BIGQUERY_DATASET_ID = os.getenv("BIGQUERY_DATASET_ID", "testedcloud_chat")
BIGQUERY_TABLE_ID = os.getenv("BIGQUERY_TABLE_ID", "events")

bq_client = bigquery.Client(project=GCP_PROJECT_ID)

app = FastAPI(
    title="TestedCloud Chat Analytics Consumer",
    version="0.1.0",
)


def utc_now_iso() -> str:
    return datetime.now(timezone.utc).isoformat()


def table_fqn() -> str:
    return f"{GCP_PROJECT_ID}.{BIGQUERY_DATASET_ID}.{BIGQUERY_TABLE_ID}"


def decode_pubsub_push(envelope: Dict[str, Any]) -> Dict[str, Any]:
    message = envelope.get("message")
    if not isinstance(message, dict):
        raise HTTPException(
            status_code=400,
            detail={"processed": False, "error": "missing_pubsub_message"},
        )

    encoded_data = message.get("data")
    if not encoded_data:
        raise HTTPException(
            status_code=400,
            detail={"processed": False, "error": "missing_pubsub_data"},
        )

    try:
        decoded_bytes = base64.b64decode(encoded_data)
        decoded_text = decoded_bytes.decode("utf-8")
        payload = json.loads(decoded_text)
    except Exception as exc:
        raise HTTPException(
            status_code=400,
            detail={
                "processed": False,
                "error": "invalid_pubsub_payload",
                "message": str(exc),
            },
        ) from exc

    if not isinstance(payload, dict):
        raise HTTPException(
            status_code=400,
            detail={"processed": False, "error": "payload_not_object"},
        )

    return payload


def validate_payload(payload: Dict[str, Any]) -> None:
    required_fields = [
        "event_id",
        "event_type",
        "source",
        "created_at",
        "received_at",
    ]

    missing = [field for field in required_fields if not payload.get(field)]
    if missing:
        raise HTTPException(
            status_code=400,
            detail={
                "processed": False,
                "error": "missing_required_fields",
                "missing": missing,
            },
        )


def payload_to_row(payload: Dict[str, Any]) -> Dict[str, Any]:
    return {
        "event_id": payload.get("event_id"),
        "event_type": payload.get("event_type"),
        "source": payload.get("source"),
        "origin": payload.get("origin"),
        "user_id": payload.get("user_id"),
        "conversation_id": payload.get("conversation_id"),
        "message_id": payload.get("message_id"),
        "created_at": payload.get("created_at"),
        "received_at": payload.get("received_at"),
        "processed_at": utc_now_iso(),
        "metadata_json": json.dumps(payload.get("metadata", {}), default=str),
        "validation_status": "valid",
    }


def insert_row(row: Dict[str, Any]) -> None:
    errors = bq_client.insert_rows_json(table_fqn(), [row])

    if errors:
        raise HTTPException(
            status_code=500,
            detail={
                "processed": False,
                "error": "bigquery_insert_failed",
                "details": errors,
            },
        )


@app.get("/health")
def health() -> Dict[str, Any]:
    return {
        "health": "healthy",
        "service": "testedcloud-chat-consumer",
        "version": "0.1.0",
        "target": table_fqn(),
        "timestamp": utc_now_iso(),
    }


@app.post("/")
async def consume_pubsub_push(request: Request) -> Dict[str, Any]:
    envelope = await request.json()
    payload = decode_pubsub_push(envelope)
    validate_payload(payload)

    row = payload_to_row(payload)
    insert_row(row)

    return {
        "processed": True,
        "event_id": row["event_id"],
        "event_type": row["event_type"],
        "target": table_fqn(),
        "processed_at": row["processed_at"],
    }
