# TestedCloud Chat — Analytics Collector Design

## 1. Purpose

This document defines the proposed design for the TestedCloud Chat Analytics Collector.

The collector will receive privacy-aware analytics events from the TestedCloud Chat Android app and forward them into the TestedCloud event-driven analytics pipeline.

The design extends TestedCloud Chat from a Firebase-backed real-time messaging app into a cloud analytics workload.

## 2. Current State

TestedCloud Chat currently has:

- Firebase Authentication
- Firestore users, conversations, and messages
- User-scoped conversation delete using `deletedAtByUser`
- Local analytics foundation in Android
- `AnalyticsEvent` model
- `AnalyticsRepository` interface
- `LogcatAnalyticsRepository`
- Local Logcat events:
  - `conversation_created`
  - `message_sent`
  - `conversation_deleted_for_user`

Current analytics destination:

    Android Logcat only

No analytics events are currently sent to Cloud Run, Pub/Sub, BigQuery, or Looker Studio.

## 3. Target Architecture

Target flow:

    TestedCloud Chat Android App
        -> Cloud Run Analytics Collector
        -> Pub/Sub Topic
        -> Cloud Run Consumer
        -> BigQuery
        -> Looker Studio Dashboard

The collector should receive events through HTTPS and publish validated events to Pub/Sub.

## 4. Proposed Cloud Resources

### Cloud Run Collector

Recommended service name:

    testedcloud-chat-events-api

Purpose:

    Receive analytics events from the Android app.

Recommended endpoint:

    POST /events

Recommended runtime:

    Python FastAPI

Recommended region:

    us-central1

### Pub/Sub Topic

Recommended topic:

    testedcloud-chat-events

Purpose:

    Transport validated chat analytics events.

### Pub/Sub Dead-Letter Topic

Recommended DLQ topic:

    testedcloud-chat-events-dlq

Purpose:

    Capture failed analytics events after retry exhaustion.

### Pub/Sub Subscription

Recommended subscription:

    testedcloud-chat-consumer-sub

Purpose:

    Deliver events to the chat analytics consumer.

### Cloud Run Consumer

Recommended service name:

    testedcloud-chat-consumer

Purpose:

    Process chat analytics events and write them to BigQuery.

### BigQuery Dataset

Recommended dataset:

    testedcloud_chat

### BigQuery Table

Recommended table:

    events

Purpose:

    Store structured chat analytics events.

## 5. Collector API

### Endpoint

    POST /events

### Request Headers

Recommended headers:

    Content-Type: application/json
    Authorization: Bearer <Firebase ID token>

Temporary lab option:

    x-api-key: <local/test key>

Production-style recommendation:

    Use Firebase ID token validation.

### Request Body

Base event schema:

    {
      "event_id": "uuid",
      "event_type": "message_sent",
      "source": "testedcloud-chat-android",
      "origin": "firebase",
      "user_id": "firebase_uid",
      "conversation_id": "conversation_id",
      "message_id": "message_id",
      "created_at": "2026-05-14T12:05:00Z",
      "metadata": {
        "conversation_type": "direct",
        "message_length": 42
      }
    }

### Response — Success

    {
      "accepted": true,
      "event_id": "uuid",
      "target": "pubsub",
      "topic": "testedcloud-chat-events"
    }

### Response — Validation Error

    {
      "accepted": false,
      "error": "invalid_event_type"
    }

## 6. Supported MVP Events

Initial supported events:

    conversation_created
    message_sent
    conversation_deleted_for_user

Later event:

    conversation_reactivated_by_message

The reactivation event should be implemented only after deduplication is designed.

## 7. Validation Rules

The collector should validate:

- `event_id` is present
- `event_type` is supported
- `source` equals `testedcloud-chat-android`
- `user_id` is present
- `created_at` is present
- `metadata` is an object if present
- message text is not included
- participant emails are not included
- participant display names are not included

For `message_sent`:

- `conversation_id` is required
- `metadata.message_length` is allowed
- full message text must not be accepted

For `conversation_deleted_for_user`:

- `conversation_id` is required
- `metadata.delete_scope` should be `for_me`
- `metadata.delete_model` should be `deletedAtByUser`

For `conversation_created`:

- `conversation_id` is required
- `metadata.conversation_type` should be `direct`
- `metadata.participant_count` should be `2`

## 8. Authentication Model

### Preferred Design

The preferred authentication model is Firebase ID token validation.

Flow:

    Android app signs in with Firebase Authentication
    Android app obtains Firebase ID token
    Android app sends Authorization: Bearer <token>
    Cloud Run collector verifies token
    Collector confirms token.uid matches event.user_id
    Collector publishes event to Pub/Sub

### Why Firebase Token Validation

Benefits:

- Stronger than API keys
- Aligns with Firebase Authentication
- Prevents unauthenticated event spam
- Allows collector to trust authenticated UID
- Avoids exposing service credentials in the Android app

### Temporary Lab Alternative

A temporary `x-api-key` could be used for early testing.

However, this should be treated as a lab-only shortcut.

Recommended portfolio story:

    Firebase ID token validation is the target design.

## 9. IAM Design

### Service Account: Collector

Recommended service account:

    testedcloud-chat-events-api-sa

Required role:

    Pub/Sub Publisher on topic `testedcloud-chat-events`

No BigQuery permissions should be needed for the collector.

### Service Account: Consumer

Recommended service account:

    testedcloud-chat-consumer-sa

Required roles:

    BigQuery Data Editor on dataset `testedcloud_chat`
    BigQuery Job User if queries/jobs are needed

### Pub/Sub Invoker

Recommended service account:

    testedcloud-chat-pubsub-invoker

Required role:

    Cloud Run Invoker on `testedcloud-chat-consumer`

## 10. BigQuery Table Design

Recommended table:

    majestic-layout-255620.testedcloud_chat.events

## 10.1 Project Boundary Decision

Decision:

    Use the central TestedCloud project `majestic-layout-255620` for the Chat analytics pipeline.

Firebase/Auth/Firestore project:

    majestic-layout-255620-b88b2

Central TestedCloud analytics project:

    majestic-layout-255620

Rationale:

    TestedCloud Chat should demonstrate that a real Firebase-backed Android app can emit operational/product events into the broader TestedCloud analytics platform.

    Keeping Pub/Sub, Cloud Run analytics services, BigQuery, Looker Studio, IAM evidence, monitoring, and DLQ handling in `majestic-layout-255620` keeps the portfolio story aligned with TestedCloud Core.

Project responsibility split:

|Area|Project|Purpose|
|-|-|-|
|Firebase Authentication|majestic-layout-255620-b88b2|Android user identity|
|Cloud Firestore|majestic-layout-255620-b88b2|Chat operational data|
|Android google-services.json|majestic-layout-255620-b88b2|Firebase Android configuration|
|Firestore rules|majestic-layout-255620-b88b2|Chat data authorization|
|Cloud Run analytics collector|majestic-layout-255620|Central event ingestion|
|Pub/Sub chat analytics topic|majestic-layout-255620|Central event transport|
|Cloud Run chat analytics consumer|majestic-layout-255620|Central event processing|
|BigQuery chat analytics dataset|majestic-layout-255620|Central analytics storage|
|Looker Studio dashboard|majestic-layout-255620|Central analytics visualization|

Authentication implication:

    The Cloud Run analytics collector in `majestic-layout-255620` should validate Firebase ID tokens issued by the Firebase project `majestic-layout-255620-b88b2`.

    The collector should verify that the token UID matches the event `user_id`.

Recommended table:

    majestic-layout-255620.testedcloud_chat.events

## 11. BigQuery Schema

Recommended fields:

|Field|Type|Mode|Description|
|-|-|-|-|
|event_id|STRING|REQUIRED|Unique event ID|
|event_type|STRING|REQUIRED|Event type|
|source|STRING|REQUIRED|Producer source|
|origin|STRING|NULLABLE|Origin layer|
|user_id|STRING|NULLABLE|Firebase UID|
|conversation_id|STRING|NULLABLE|Conversation document ID|
|message_id|STRING|NULLABLE|Message document ID|
|created_at|TIMESTAMP|REQUIRED|Client-side event time|
|received_at|TIMESTAMP|REQUIRED|Collector receive time|
|processed_at|TIMESTAMP|NULLABLE|Consumer processing time|
|metadata_json|STRING|NULLABLE|Serialized metadata|
|validation_status|STRING|NULLABLE|Validation result|

## 12. Privacy Requirements

Analytics events must not include:

- Full message text
- Participant emails
- Participant display names
- Contact lists
- Phone numbers
- Precise location
- Authentication tokens
- Raw exception traces
- Firestore security rule payloads

Analytics may include:

- Firebase UID
- Conversation ID
- Message ID
- Event type
- Message length
- Timestamp
- Platform
- App version
- Non-sensitive operational metadata

## 13. Reliability Requirements

Analytics must not block chat functionality.

If analytics fails:

- Message sending must still succeed
- Conversation delete must still succeed
- Conversation creation must still succeed

Android behavior:

    AnalyticsRepository.track() should be non-blocking from a product perspective.
    Failures should be captured locally or ignored safely during MVP.

Collector behavior:

    Invalid events should return clear validation errors.
    Valid events should be published to Pub/Sub.
    Pub/Sub/consumer failures should be handled through retry and DLQ.

## 14. Implementation Plan

### Phase 1 — Backend Collector Skeleton

- Create FastAPI service
- Add `POST /events`
- Add health endpoint
- Validate request body
- Return accepted response
- No Pub/Sub yet

### Phase 2 — Pub/Sub Publish

- Create topic `testedcloud-chat-events`
- Add collector service account
- Grant Pub/Sub Publisher
- Publish validated events to Pub/Sub

### Phase 3 — Consumer and BigQuery

- Create BigQuery dataset `testedcloud_chat`
- Create table `events`
- Create Cloud Run consumer
- Subscribe consumer to Pub/Sub
- Insert events into BigQuery

### Phase 4 — Android HTTP Integration

- Add HTTP analytics repository
- Keep Logcat repository as fallback
- Send events to collector
- Use Firebase ID token auth
- Ensure analytics failure does not block chat

### Phase 5 — Dashboard

- Create BigQuery views
- Create Looker Studio dashboard
- Track event counts, message events, delete events, and active usage

## 15. Validation Plan

Initial validation:

- Collector health endpoint returns healthy
- Collector rejects invalid event type
- Collector accepts valid `message_sent`
- Collector publishes to Pub/Sub
- Consumer writes row to BigQuery
- BigQuery query confirms event exists
- Android emits event without blocking chat

Manual test events:

    conversation_created
    message_sent
    conversation_deleted_for_user

Deferred test event:

    conversation_reactivated_by_message

## 16. Open Decisions

Open questions:

1. Should chat analytics live in `majestic-layout-255620-b88b2` or central TestedCloud Core project `majestic-layout-255620`?
2. Should the first collector use Firebase ID token validation immediately?
3. Should the collector share code patterns with the existing TestedCloud Core FastAPI publisher?
4. Should `conversation_reactivated_by_message` be emitted from Android or derived later in BigQuery?
5. Should message_id be returned from `sendMessage()` to improve analytics accuracy?

## 17. Recommended Next Step

Recommended next step:

    Implement the Cloud Run collector skeleton with:
    - FastAPI
    - GET /health
    - POST /events
    - local validation only
    - no Pub/Sub publish yet

This keeps the implementation small and testable before connecting Pub/Sub and BigQuery.
