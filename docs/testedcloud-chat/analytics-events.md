# TestedCloud Chat — Analytics Events

## 1\. Purpose

This document defines the analytics event model for TestedCloud Chat.

The goal is to track important user and system activity from the Android application and connect those events to the TestedCloud event-driven analytics architecture.

The analytics model should demonstrate:

* Application event tracking
* Cloud Run event ingestion
* Pub/Sub event transport
* Cloud Run event processing
* BigQuery analytics storage
* Looker Studio dashboarding
* Privacy-aware event design
* Operational observability

## 2\. Analytics Goals

Primary goals:

* Track key product actions.
* Measure basic chat usage.
* Validate that app events can flow into Google Cloud.
* Support BigQuery-based analysis.
* Support Looker Studio dashboarding.
* Avoid storing unnecessary personal or sensitive data.
* Avoid storing full message text in analytics.
* Keep event schema simple and extensible.
* Align with the existing TestedCloud event-driven platform.

## 3\. Analytics Non-Goals

The MVP analytics system will not attempt to implement:

* Full product analytics platform
* User behavior profiling
* Advertising analytics
* Precise location tracking
* Contact syncing
* Message content analytics
* Full retention policy automation
* Advanced AI-based analysis in Release 1

Future releases may add advanced analytics and AI insights after the basic pipeline is validated.

## 4\. High-Level Event Flow

Recommended analytics flow:

```text
TestedCloud Chat Android App
    |
    | HTTPS POST /events
    v
Cloud Run Event Collector
    |
    | Pub/Sub publish
    v
Pub/Sub Topic: testedcloud-chat-events
    |
    | Push or pull subscription
    v
Cloud Run Consumer
    |
    | BigQuery insert
    v
BigQuery Dataset: testedcloud\_chat
    |
    v
Looker Studio Dashboard
```

This extends the existing TestedCloud portfolio pattern:

```text
Producer
    |
    v
Pub/Sub
    |
    v
Cloud Run
    |
    v
BigQuery
    |
    v
Dashboard
```

## 5\. Event Design Principles

Analytics events should follow these principles:

* Events should be small.
* Events should have a clear event type.
* Events should use stable field names.
* Events should include timestamps.
* Events should avoid message content.
* Events should avoid unnecessary personal data.
* Events should include a unique event ID.
* Events should include application source.
* Events should include app version when available.
* Events should not block core chat functionality.
* Failed analytics should not prevent messages from being sent.

## 6\. Event Categories

Event categories:

|Category|Purpose|
|-|-|
|App lifecycle|Track app opens and session-related events|
|Authentication|Track sign-in and sign-out|
|Conversation|Track conversation creation and access|
|Messaging|Track message send/read activity|
|Profile|Track profile updates|
|System|Track app errors and diagnostic events|
|AI future|Track future AI-assisted actions|

## 7\. Required MVP Events

The MVP should track the following events:

```text
app\_opened
user\_signed\_in
user\_signed\_out
conversation\_created
message\_sent
```

These events are enough to demonstrate:

* App usage
* Authentication activity
* Chat creation
* Messaging activity
* End-to-end analytics pipeline

## 8\. Future Events

Future event candidates:

```text
message\_read
profile\_updated
conversation\_opened
conversation\_archived
conversation\_deleted
notification\_received
notification\_opened
app\_error
firestore\_permission\_denied
ai\_suggestion\_requested
ai\_suggestion\_accepted
ai\_suggestion\_rejected
```

These should be added only after the MVP is stable.

## 9\. Standard Event Schema

All analytics events should follow a common schema.

```json
{
  "event\_id": "uuid",
  "event\_type": "message\_sent",
  "source": "testedcloud-chat-android",
  "origin": "firebase",
  "user\_id": "uid\_123",
  "conversation\_id": "conversation\_456",
  "message\_id": "message\_789",
  "created\_at": "2026-05-05T21:00:00Z",
  "metadata": {
    "platform": "android",
    "app\_version": "0.1.0",
    "message\_length": 42
  }
}
```

## 10\. Standard Event Fields

|Field|Type|Required|Description|
|-|-|-|-|
|`event\_id`|string|Yes|Unique event identifier|
|`event\_type`|string|Yes|Event name|
|`source`|string|Yes|Application or producer|
|`origin`|string|No|Origin layer or system|
|`user\_id`|string|No|Firebase UID|
|`conversation\_id`|string|No|Conversation ID|
|`message\_id`|string|No|Message ID|
|`created\_at`|timestamp/string|Yes|Client-side event creation time|
|`metadata`|object|No|Optional event context|

Server-side fields added by the collector or consumer:

|Field|Type|Description|
|-|-|-|
|`received\_at`|timestamp|Time event was received by Cloud Run collector|
|`processed\_at`|timestamp|Time event was written by consumer|
|`processing\_status`|string|Processing result|
|`validation\_status`|string|Validation result|

## 11\. Source Values

Allowed MVP source values:

```text
testedcloud-chat-android
```

Future source values:

```text
testedcloud-chat-web
testedcloud-chat-api
testedcloud-chat-admin
```

## 12\. Origin Values

Allowed MVP origin values:

```text
android
firebase
firestore
cloudrun
```

Recommended usage:

|Origin|Usage|
|-|-|
|`android`|Event initiated from app UI|
|`firebase`|Event related to Firebase Auth or Firestore|
|`firestore`|Event derived from Firestore action|
|`cloudrun`|Backend event generated by API|

## 13\. Event: app\_opened

### Purpose

Tracks when the app is opened.

### Required Fields

```text
event\_id
event\_type
source
user\_id
created\_at
metadata.platform
metadata.app\_version
```

### Example

```json
{
  "event\_id": "evt\_app\_opened\_001",
  "event\_type": "app\_opened",
  "source": "testedcloud-chat-android",
  "origin": "android",
  "user\_id": "uid\_123",
  "created\_at": "2026-05-05T21:00:00Z",
  "metadata": {
    "platform": "android",
    "app\_version": "0.1.0"
  }
}
```

### Privacy Notes

Do not include device identifiers unless required for debugging.

## 14\. Event: user\_signed\_in

### Purpose

Tracks successful user sign-in.

### Required Fields

```text
event\_id
event\_type
source
user\_id
created\_at
metadata.sign\_in\_method
```

### Example

```json
{
  "event\_id": "evt\_signin\_001",
  "event\_type": "user\_signed\_in",
  "source": "testedcloud-chat-android",
  "origin": "firebase",
  "user\_id": "uid\_123",
  "created\_at": "2026-05-05T21:01:00Z",
  "metadata": {
    "platform": "android",
    "app\_version": "0.1.0",
    "sign\_in\_method": "email\_password"
  }
}
```

### Privacy Notes

Do not include password, ID token, refresh token, or full auth credential.

## 15\. Event: user\_signed\_out

### Purpose

Tracks user sign-out.

### Required Fields

```text
event\_id
event\_type
source
user\_id
created\_at
```

### Example

```json
{
  "event\_id": "evt\_signout\_001",
  "event\_type": "user\_signed\_out",
  "source": "testedcloud-chat-android",
  "origin": "firebase",
  "user\_id": "uid\_123",
  "created\_at": "2026-05-05T21:20:00Z",
  "metadata": {
    "platform": "android",
    "app\_version": "0.1.0"
  }
}
```

## 16\. Event: conversation\_created

### Purpose

Tracks when a new conversation is created.

### Required Fields

```text
event\_id
event\_type
source
user\_id
conversation\_id
created\_at
metadata.conversation\_type
metadata.participant\_count
```

### Example

```json
{
  "event\_id": "evt\_conversation\_001",
  "event\_type": "conversation\_created",
  "source": "testedcloud-chat-android",
  "origin": "firestore",
  "user\_id": "uid\_123",
  "conversation\_id": "conversation\_456",
  "created\_at": "2026-05-05T21:05:00Z",
  "metadata": {
    "platform": "android",
    "app\_version": "0.1.0",
    "conversation\_type": "direct",
    "participant\_count": 2
  }
}
```

### Privacy Notes

Avoid including participant emails or names in analytics events.

## 17\. Event: message\_sent

### Purpose

Tracks when a user sends a message.

### Required Fields

```text
event\_id
event\_type
source
user\_id
conversation\_id
message\_id
created\_at
metadata.message\_length
```

### Example

```json
{
  "event\_id": "evt\_message\_001",
  "event\_type": "message\_sent",
  "source": "testedcloud-chat-android",
  "origin": "firestore",
  "user\_id": "uid\_123",
  "conversation\_id": "conversation\_456",
  "message\_id": "message\_789",
  "created\_at": "2026-05-05T21:08:00Z",
  "metadata": {
    "platform": "android",
    "app\_version": "0.1.0",
    "message\_length": 42,
    "message\_type": "text"
  }
}
```

### Privacy Notes

Do not include:

```text
message\_text
recipient\_email
recipient\_display\_name
full conversation content
```

This is important for privacy and reduces analytics data sensitivity.

## 18\. Event: message\_read

MVP status:

```text
Future
```

### Purpose

Tracks when a user reads a message or conversation.

### Example

```json
{
  "event\_id": "evt\_message\_read\_001",
  "event\_type": "message\_read",
  "source": "testedcloud-chat-android",
  "origin": "firestore",
  "user\_id": "uid\_123",
  "conversation\_id": "conversation\_456",
  "message\_id": "message\_789",
  "created\_at": "2026-05-05T21:09:00Z",
  "metadata": {
    "platform": "android",
    "app\_version": "0.1.0"
  }
}
```

## 19\. Event: profile\_updated

MVP status:

```text
Future
```

### Purpose

Tracks when a user updates their profile.

### Privacy Notes

Do not include the full changed profile values unless necessary. Prefer metadata such as:

```json
{
  "updated\_fields": \["displayName"]
}
```

## 20\. Event: app\_error

MVP status:

```text
Future
```

### Purpose

Tracks app-level errors in a privacy-safe way.

### Example

```json
{
  "event\_id": "evt\_error\_001",
  "event\_type": "app\_error",
  "source": "testedcloud-chat-android",
  "origin": "android",
  "user\_id": "uid\_123",
  "created\_at": "2026-05-05T21:10:00Z",
  "metadata": {
    "platform": "android",
    "app\_version": "0.1.0",
    "error\_category": "firestore\_write\_failed",
    "screen": "ChatScreen"
  }
}
```

Do not include full stack traces with secrets, tokens, or sensitive payloads in analytics events.

## 21\. Event Validation Rules

Cloud Run collector should validate:

|Field|Validation|
|-|-|
|`event\_id`|Required, non-empty string|
|`event\_type`|Required, allowlisted|
|`source`|Required, allowlisted|
|`created\_at`|Required, valid timestamp|
|`user\_id`|Optional but must be string if present|
|`conversation\_id`|Optional but must be string if present|
|`message\_id`|Optional but must be string if present|
|`metadata`|Optional object, size-limited|

Allowlisted MVP event types:

```text
app\_opened
user\_signed\_in
user\_signed\_out
conversation\_created
message\_sent
```

## 22\. Event Size Limits

Recommended limits:

|Item|Limit|
|-|-|
|Total event payload|16 KB|
|Metadata object|8 KB|
|String field|512 characters|
|Event type|100 characters|
|Source|100 characters|

Reason:

Keep events small, predictable, and cheap to process.

## 23\. Authentication for Event Collector

Recommended implementation:

* Android app obtains Firebase ID token.
* App sends token as Authorization Bearer header.
* Cloud Run collector validates Firebase ID token.
* Collector extracts UID from token.
* Collector compares token UID to event `user\_id` if present.
* Collector rejects mismatched user IDs.

Header:

```text
Authorization: Bearer <Firebase ID token>
```

Important:

Do not log the token.

## 24\. Event Collector API Contract

Endpoint:

```text
POST /events
```

Request headers:

```text
Content-Type: application/json
Authorization: Bearer <Firebase ID token>
```

Request body:

```json
{
  "event\_id": "evt\_123",
  "event\_type": "message\_sent",
  "source": "testedcloud-chat-android",
  "origin": "firestore",
  "user\_id": "uid\_123",
  "conversation\_id": "conversation\_456",
  "message\_id": "message\_789",
  "created\_at": "2026-05-05T21:08:00Z",
  "metadata": {
    "platform": "android",
    "app\_version": "0.1.0",
    "message\_length": 42
  }
}
```

Successful response:

```json
{
  "status": "accepted",
  "event\_id": "evt\_123"
}
```

Validation error response:

```json
{
  "status": "rejected",
  "reason": "invalid\_event\_type"
}
```

## 25\. Pub/Sub Topic

Topic:

```text
testedcloud-chat-events
```

Message body should contain the validated event payload plus server-side fields:

```json
{
  "event\_id": "evt\_123",
  "event\_type": "message\_sent",
  "source": "testedcloud-chat-android",
  "origin": "firestore",
  "user\_id": "uid\_123",
  "conversation\_id": "conversation\_456",
  "message\_id": "message\_789",
  "created\_at": "2026-05-05T21:08:00Z",
  "received\_at": "2026-05-05T21:08:01Z",
  "metadata": {
    "platform": "android",
    "app\_version": "0.1.0",
    "message\_length": 42
  }
}
```

## 26\. Pub/Sub Message Attributes

Recommended attributes:

|Attribute|Value|
|-|-|
|`event\_type`|Event type|
|`source`|Event source|
|`schema\_version`|Event schema version|
|`app`|`testedcloud-chat`|

Example:

```text
event\_type=message\_sent
source=testedcloud-chat-android
schema\_version=1
app=testedcloud-chat
```

## 27\. BigQuery Dataset and Table

Dataset:

```text
testedcloud\_chat
```

Table:

```text
events
```

## 28\. BigQuery Schema

Recommended table schema:

|Field|Type|Mode|
|-|-|-|
|`event\_id`|STRING|REQUIRED|
|`event\_type`|STRING|REQUIRED|
|`source`|STRING|REQUIRED|
|`origin`|STRING|NULLABLE|
|`user\_id`|STRING|NULLABLE|
|`conversation\_id`|STRING|NULLABLE|
|`message\_id`|STRING|NULLABLE|
|`created\_at`|TIMESTAMP|REQUIRED|
|`received\_at`|TIMESTAMP|NULLABLE|
|`processed\_at`|TIMESTAMP|REQUIRED|
|`metadata`|JSON|NULLABLE|
|`schema\_version`|STRING|NULLABLE|

If JSON type is not used:

```text
metadata STRING
```

## 29\. Recommended BigQuery Views

Recommended views:

```text
v\_chat\_events
v\_chat\_daily\_usage
v\_chat\_message\_activity
v\_chat\_auth\_activity
v\_chat\_latency\_metrics
```

## 30\. Example Query: Events by Type

```sql
SELECT
  event\_type,
  COUNT(\*) AS event\_count
FROM `majestic-layout-255620.testedcloud\_chat.events`
GROUP BY event\_type
ORDER BY event\_count DESC;
```

## 31\. Example Query: Daily Message Count

```sql
SELECT
  DATE(created\_at) AS event\_date,
  COUNT(\*) AS messages\_sent
FROM `majestic-layout-255620.testedcloud\_chat.events`
WHERE event\_type = 'message\_sent'
GROUP BY event\_date
ORDER BY event\_date DESC;
```

## 32\. Example Query: Processing Latency

```sql
SELECT
  APPROX\_QUANTILES(TIMESTAMP\_DIFF(processed\_at, created\_at, SECOND), 100)\[OFFSET(50)] AS p50\_latency\_seconds,
  APPROX\_QUANTILES(TIMESTAMP\_DIFF(processed\_at, created\_at, SECOND), 100)\[OFFSET(95)] AS p95\_latency\_seconds,
  APPROX\_QUANTILES(TIMESTAMP\_DIFF(processed\_at, created\_at, SECOND), 100)\[OFFSET(99)] AS p99\_latency\_seconds
FROM `majestic-layout-255620.testedcloud\_chat.events`;
```

## 33\. Dashboard Metrics

Initial Looker Studio metrics:

|Metric|Description|
|-|-|
|Total events|Count of all analytics events|
|App opens|Count of `app\_opened`|
|Sign-ins|Count of `user\_signed\_in`|
|Conversations created|Count of `conversation\_created`|
|Messages sent|Count of `message\_sent`|
|Events by type|Distribution of event types|
|Events over time|Time-series event trend|
|Processing latency|p50, p95, p99 latency|
|Event source|Events by source|

## 34\. Privacy Rules for Analytics

Analytics must not include:

```text
message\_text
passwords
auth tokens
refresh tokens
service account keys
recipient email
precise location
contact list
payment information
health information
biometric data
```

Analytics may include:

```text
event type
UID
conversation ID
message ID
message length
platform
app version
timestamps
```

Optional future privacy improvement:

```text
hash or pseudonymize user\_id before BigQuery storage
```

## 35\. Error Handling

Collector behavior:

|Scenario|Response|
|-|-|
|Valid event|202 Accepted|
|Invalid schema|400 Bad Request|
|Missing auth token|401 Unauthorized|
|Invalid auth token|401 Unauthorized|
|UID mismatch|403 Forbidden|
|Unknown event type|400 Bad Request|
|Pub/Sub publish failure|500 Internal Server Error|

Consumer behavior:

|Scenario|Response|
|-|-|
|Valid Pub/Sub event|204 / 200|
|Invalid event payload|Return non-2xx to trigger retry or DLQ|
|BigQuery insert failure|Return non-2xx to trigger retry|
|Duplicate event ID|Log and ignore or upsert depending on implementation|

## 36\. Idempotency

Event IDs should be unique.

Recommended ID:

```text
UUID generated by client or collector
```

Consumer should ideally avoid duplicate inserts.

Options:

* Use BigQuery insertId if streaming inserts are used.
* Deduplicate in query views by `event\_id`.
* Use a staging table and scheduled deduplication later.

MVP approach:

```text
Generate unique event\_id and document duplicate handling as a future improvement.
```

## 37\. Monitoring and Alerts

Recommended alerts:

```text
Cloud Run 5xx errors for testedcloud-chat-events-api
Cloud Run 5xx errors for testedcloud-chat-consumer
Pub/Sub backlog for testedcloud-chat-consumer-sub
DLQ messages for chat event pipeline
BigQuery insert failure logs
Unexpected event volume spike
```

## 38\. Evidence Plan

Recommended evidence files:

```text
docs/evidence/testedcloud-chat/chat-event-api-validation.txt
docs/evidence/testedcloud-chat/chat-pubsub-topic-config.txt
docs/evidence/testedcloud-chat/chat-consumer-logs.txt
docs/evidence/testedcloud-chat/chat-bigquery-events-sample.txt
docs/evidence/testedcloud-chat/chat-dashboard-validation.txt
docs/evidence/testedcloud-chat/chat-monitoring-alerts.txt
```

## 39\. Validation Tests

Analytics validation tests:

|Test|Expected Result|
|-|-|
|Send `app\_opened` event|Event accepted and stored|
|Send `user\_signed\_in` event|Event accepted and stored|
|Send `message\_sent` event|Event accepted and stored|
|Send unknown event type|Event rejected|
|Send event without token|Event rejected|
|Send event with mismatched UID|Event rejected|
|Pub/Sub event reaches consumer|Consumer processes event|
|Consumer writes to BigQuery|Row appears in table|
|Dashboard shows event metrics|Dashboard updates|

## 40\. Implementation Phases

## Phase 1 — Local Event Model

* Define event types in Android app.
* Build event payload model.
* Log events locally for development.
* Validate schema shape.

## Phase 2 — Cloud Run Collector

* Build `POST /events`.
* Add validation.
* Add Firebase token validation.
* Publish valid events to Pub/Sub.

## Phase 3 — Pub/Sub and Consumer

* Create topic.
* Create subscription.
* Build consumer.
* Write to BigQuery.

## Phase 4 — Dashboard

* Create views.
* Build Looker Studio dashboard.
* Add evidence files.

## Phase 5 — Monitoring

* Add Cloud Monitoring alert policies.
* Add DLQ where appropriate.
* Document operational response.

## 41\. Open Questions

Open questions:

* Should analytics use Firebase Analytics export instead of a custom collector?
* Should BigQuery store raw UID or hashed UID?
* Should analytics collector be public with Firebase token validation or protected through another mechanism?
* Should `message\_sent` event be emitted by the app or by backend/Firestore trigger?
* Should duplicate events be deduplicated in BigQuery?
* Should analytics events include approximate client timezone?
* Should Cloud Run collector be shared with existing TestedCloud event API or separated?

## 42\. Recommended Immediate Next Steps

Recommended next steps:

```text
1. Commit this analytics events document.
2. Create roadmap.md.
3. Draft Firestore rules file.
4. Decide Firebase project strategy.
5. Create Android app skeleton.
6. Implement local analytics event model.
7. Add Cloud Run event collector in a later phase.
```

## 43\. Final Positioning

The TestedCloud Chat analytics model demonstrates how a mobile application can generate privacy-aware product events and send them through a serverless Google Cloud analytics pipeline.

The event architecture connects Firebase application activity with the broader TestedCloud portfolio:

```text
Android App
Firebase Auth
Firestore
Cloud Run
Pub/Sub
BigQuery
Looker Studio
Cloud Monitoring
```

This strengthens the TestedCloud narrative by showing both real-time application functionality and cloud-native analytics integration.


## User-Scoped Delete Analytics Update

### Purpose

This section updates the TestedCloud Chat analytics model after validating the user-scoped conversation delete workflow.

The app now supports:

    Delete conversation for me

The technical model is based on:

    deletedAtByUser: map<uid, timestamp>

Because this is not a global delete, analytics should not use a generic event name such as:

    conversation_deleted

Instead, the analytics event should explicitly represent the user-scoped behavior.

### Updated MVP Chat Events

The recommended MVP analytics events for the current TestedCloud Chat state are:

    conversation_created
    message_sent
    conversation_deleted_for_user
    conversation_reactivated_by_message

These events are enough to describe the core conversation lifecycle without collecting message content.

### Event: conversation_created

Purpose:

    Tracks when a direct conversation is created or first initialized.

Recommended trigger:

    When createDirectConversation creates a new Firestore conversation document.

Do not emit this event when the app simply reuses an existing direct conversation.

Recommended fields:

|Field|Required|Notes|
|-|-|-|
|event_id|Yes|Unique event ID|
|event_type|Yes|conversation_created|
|source|Yes|testedcloud-chat-android|
|origin|Yes|firebase|
|user_id|Yes|Firebase UID of actor|
|conversation_id|Yes|Conversation document ID|
|created_at|Yes|Client-side event timestamp|
|metadata.conversation_type|Yes|direct|
|metadata.participant_count|Yes|2|

Privacy notes:

- Do not include participant emails.
- Do not include participant display names.
- Do not include message text.

Example:

    {
      "event_id": "uuid",
      "event_type": "conversation_created",
      "source": "testedcloud-chat-android",
      "origin": "firebase",
      "user_id": "uid_user_1",
      "conversation_id": "conversation_123",
      "created_at": "2026-05-14T12:00:00Z",
      "metadata": {
        "conversation_type": "direct",
        "participant_count": 2
      }
    }

### Event: message_sent

Purpose:

    Tracks when a user sends a chat message.

Recommended trigger:

    After sendMessage completes successfully.

Recommended fields:

|Field|Required|Notes|
|-|-|-|
|event_id|Yes|Unique event ID|
|event_type|Yes|message_sent|
|source|Yes|testedcloud-chat-android|
|origin|Yes|firebase|
|user_id|Yes|Firebase UID of sender|
|conversation_id|Yes|Conversation document ID|
|message_id|No|Message document ID if available|
|created_at|Yes|Client-side event timestamp|
|metadata.message_length|Yes|Length of message text only|
|metadata.conversation_type|Yes|direct|

Privacy notes:

- Do not include message text.
- Do not include recipient email.
- Do not include recipient display name.
- Message length is acceptable because it supports basic analytics without storing content.

Example:

    {
      "event_id": "uuid",
      "event_type": "message_sent",
      "source": "testedcloud-chat-android",
      "origin": "firebase",
      "user_id": "uid_user_1",
      "conversation_id": "conversation_123",
      "message_id": "message_456",
      "created_at": "2026-05-14T12:05:00Z",
      "metadata": {
        "conversation_type": "direct",
        "message_length": 42
      }
    }

### Event: conversation_deleted_for_user

Purpose:

    Tracks when a user deletes a conversation only from their own view.

Recommended trigger:

    After deleteConversationForUser completes successfully.

This event represents a user-scoped soft delete, not a global delete.

Recommended fields:

|Field|Required|Notes|
|-|-|-|
|event_id|Yes|Unique event ID|
|event_type|Yes|conversation_deleted_for_user|
|source|Yes|testedcloud-chat-android|
|origin|Yes|firebase|
|user_id|Yes|Firebase UID of actor|
|conversation_id|Yes|Conversation document ID|
|created_at|Yes|Client-side event timestamp|
|metadata.delete_scope|Yes|for_me|
|metadata.delete_model|Yes|deletedAtByUser|

Privacy notes:

- Do not include message text.
- Do not include participant emails.
- Do not expose whether the other participant has deleted the conversation.

Example:

    {
      "event_id": "uuid",
      "event_type": "conversation_deleted_for_user",
      "source": "testedcloud-chat-android",
      "origin": "firebase",
      "user_id": "uid_user_1",
      "conversation_id": "conversation_123",
      "created_at": "2026-05-14T12:10:00Z",
      "metadata": {
        "delete_scope": "for_me",
        "delete_model": "deletedAtByUser"
      }
    }

### Event: conversation_reactivated_by_message

Purpose:

    Tracks when a conversation previously deleted by a user becomes visible again because another participant sent a new message after the user's delete timestamp.

Recommended trigger:

    When the app observes a conversation where:

    deletedAtByUser[currentUserId] exists

and:

    lastMessageAt > deletedAtByUser[currentUserId]

This event should be emitted carefully to avoid duplicate events for the same reactivation.

Recommended deduplication key:

    conversation_id + user_id + lastMessageAt

Recommended fields:

|Field|Required|Notes|
|-|-|-|
|event_id|Yes|Unique event ID|
|event_type|Yes|conversation_reactivated_by_message|
|source|Yes|testedcloud-chat-android|
|origin|Yes|firebase|
|user_id|Yes|Firebase UID of user seeing reactivated conversation|
|conversation_id|Yes|Conversation document ID|
|created_at|Yes|Client-side event timestamp|
|metadata.reactivation_reason|Yes|new_message_after_user_delete|
|metadata.delete_model|Yes|deletedAtByUser|

Privacy notes:

- Do not include the message text that caused reactivation.
- Do not include sender email.
- Do not include participant display names.

Example:

    {
      "event_id": "uuid",
      "event_type": "conversation_reactivated_by_message",
      "source": "testedcloud-chat-android",
      "origin": "firebase",
      "user_id": "uid_user_2",
      "conversation_id": "conversation_123",
      "created_at": "2026-05-14T12:20:00Z",
      "metadata": {
        "reactivation_reason": "new_message_after_user_delete",
        "delete_model": "deletedAtByUser"
      }
    }

### Implementation Recommendation

Analytics should be implemented after the current chat behavior remains stable.

Recommended implementation order:

1. Create a local analytics event model in Android.
2. Add a no-op AnalyticsRepository interface.
3. Emit local/log-only events first.
4. Add a Cloud Run collector later.
5. Publish events to Pub/Sub.
6. Write events to BigQuery.
7. Build Looker Studio dashboard views.

### Reliability Rule

Analytics must never block chat functionality.

If analytics fails:

    message sending must still succeed
    conversation delete must still succeed
    conversation creation must still succeed

Errors should be logged locally and later routed to a non-blocking retry or diagnostic mechanism.

### Privacy Rule

Analytics must not include:

- Full message text
- Participant emails
- Participant display names
- Contact lists
- Phone numbers
- Precise location
- Authentication tokens
- Firestore security rule payloads
- Raw exception traces containing secrets

Analytics may include:

- Firebase UID
- Conversation ID
- Message ID
- Message length
- Event type
- App version
- Platform
- Timestamp
- Non-sensitive operational metadata

### Current Status

Status:

    DESIGN UPDATED

Implementation status:

    Not implemented in Android code yet.

Next recommended implementation step:

    Add local Android analytics models and a no-op AnalyticsRepository so the app can emit structured events without sending them to Cloud Run yet.
