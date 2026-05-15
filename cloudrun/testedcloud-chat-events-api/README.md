# TestedCloud Chat Events API

Cloud Run analytics collector skeleton for TestedCloud Chat.

## Purpose

This service receives privacy-aware analytics events from the TestedCloud Chat Android app.

Current phase:

    Local validation only

Not implemented yet:

    Pub/Sub publishing
    Firebase ID token validation
    BigQuery writes

## Endpoints

### GET /health

Returns service health.

### POST /events

Accepts a supported analytics event.

Supported MVP event types:

    conversation_created
    message_sent
    conversation_deleted_for_user

Deferred event:

    conversation_reactivated_by_message

## Local Run

    cd cloudrun/testedcloud-chat-events-api
    python3 -m venv .venv
    source .venv/bin/activate
    pip install -r requirements.txt
    uvicorn app:app --reload --port 8088

## Local Test

Health:

    curl -i http://localhost:8088/health

Valid event:

    curl -i -X POST http://localhost:8088/events \
      -H "Content-Type: application/json" \
      -d '{"event_type":"message_sent","source":"testedcloud-chat-android","origin":"firebase","user_id":"uid_test_user","conversation_id":"conversation_test","created_at":"2026-05-14T12:05:00Z","metadata":{"conversation_type":"direct","message_length":42}}'

Privacy rejection test:

    curl -i -X POST http://localhost:8088/events \
      -H "Content-Type: application/json" \
      -d '{"event_type":"message_sent","source":"testedcloud-chat-android","origin":"firebase","user_id":"uid_test_user","conversation_id":"conversation_test","created_at":"2026-05-14T12:05:00Z","metadata":{"conversation_type":"direct","message_text":"this should be rejected"}}'

## Privacy Rules

Do not send:

- Full message text
- Participant emails
- Participant display names
- Auth tokens
- Passwords
- Contact lists
- Phone numbers
- Precise location

## Target Future Flow

    Android App
    -> Cloud Run Events API
    -> Pub/Sub testedcloud-chat-events
    -> Cloud Run Consumer
    -> BigQuery testedcloud_chat.events
    -> Looker Studio
