from fastapi import FastAPI, Header, HTTPException
from fastapi.middleware.cors import CORSMiddleware
from pydantic import BaseModel
from datetime import datetime
import uuid
import json
import os
from google.cloud import pubsub_v1
from google.cloud import bigquery

app = FastAPI(title="TestedCloud Hybrid Lab")

app.add_middleware(
    CORSMiddleware,
    allow_origins=["https://ui.testedcloud.com"],
    allow_credentials=False,
    allow_methods=["*"],
    allow_headers=["*"],
)

PROJECT_ID = os.getenv("GCP_PROJECT_ID", "majestic-layout-255620")
TOPIC_ID = os.getenv("PUBSUB_TOPIC_ID", "testedcloud-events")
API_KEY = os.getenv("TESTEDCLOUD_API_KEY", "dev-key-change-me")
BQ_TABLE_ID = "majestic-layout-255620.testedcloud_events.hybrid_events"
bq_client = bigquery.Client(project=PROJECT_ID)

publisher = pubsub_v1.PublisherClient()
topic_path = publisher.topic_path(PROJECT_ID, TOPIC_ID)

class EventPayload(BaseModel):
    source: str = "on-prem-nuc"
    event_type: str = "test_event"
    message: str = "Hello from TestedCloud hybrid lab"

@app.get("/")
def root():
    return {
        "status": "ok",
        "service": "testedcloud-onprem-api",
        "location": "NUC Ubuntu Server",
        "timestamp": datetime.utcnow().isoformat()
    }

@app.get("/health")
def health():
    return {
        "health": "healthy",
        "node": "on-prem-nuc",
        "gcp_project": PROJECT_ID,
        "pubsub_topic": TOPIC_ID
    }

@app.post("/send-event")
def send_event(
    payload: EventPayload,
    x_api_key: str = Header(None),
    cf_user_email: str = Header(None, alias="Cf-Access-Authenticated-User-Email")
):
    if x_api_key != API_KEY:
        raise HTTPException(status_code=401, detail="Invalid API key")

    event = {
        "event_id": str(uuid.uuid4()),
        "received_at": datetime.utcnow().isoformat(),
        "source": payload.source,
        "event_type": payload.event_type,
        "message": payload.message,
        "origin": "on-prem-nuc",
        "user_email": cf_user_email or "unknown"
    }

    # 🔥 Publicar en Pub/Sub
    future = publisher.publish(
        topic_path,
        json.dumps(event).encode("utf-8")
    )
    message_id = future.result()

    # ✅ IMPORTANTE: este return es lo que te faltaba
    return {
        "published": True,
        "target": "google-cloud-pubsub",
        "project_id": PROJECT_ID,
        "topic": TOPIC_ID,
        "message_id": message_id,
        "event": event
    }


@app.get("/trace-event/{event_id}")
def trace_event(event_id: str, x_api_key: str = Header(None)):
    if x_api_key != API_KEY:
        raise HTTPException(status_code=401, detail="Invalid API key")

    query = f"""
    SELECT
      event_id,
      source,
      event_type,
      message,
      origin,
      received_at,
      processed_at,
      TIMESTAMP_DIFF(processed_at, received_at, SECOND) AS latency_seconds
    FROM `{BQ_TABLE_ID}`
    WHERE event_id = @event_id
    ORDER BY processed_at DESC
    LIMIT 1
    """

    job_config = bigquery.QueryJobConfig(
        query_parameters=[
            bigquery.ScalarQueryParameter("event_id", "STRING", event_id)
        ]
    )

    rows = list(bq_client.query(query, job_config=job_config).result())

    if not rows:
        return {
            "found": False,
            "event_id": event_id,
            "status": "processing_or_not_found"
        }

    row = dict(rows[0])

    return {
        "found": True,
        "status": "processed",
        "event": row
    }
