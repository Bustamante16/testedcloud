# TestedCloud Chat Consumer

Cloud Run consumer for TestedCloud Chat analytics events.

## Purpose

Receives Pub/Sub push messages from testedcloud-chat-events and inserts structured analytics events into BigQuery.

## Target

    majestic-layout-255620.testedcloud_chat.events

## Endpoints

    GET /health
    POST /

## Environment Variables

    GCP_PROJECT_ID=majestic-layout-255620
    BIGQUERY_DATASET_ID=testedcloud_chat
    BIGQUERY_TABLE_ID=events

## Local Run

    cd cloudrun/testedcloud-chat-consumer
    python3 -m venv .venv
    source .venv/bin/activate
    pip install -r requirements.txt
    uvicorn app:app --reload --port 8090

## Local Test

The POST endpoint expects a Pub/Sub push envelope with base64-encoded JSON in message.data.
