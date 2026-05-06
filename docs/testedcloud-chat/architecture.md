# TestedCloud Chat — Architecture

## 1\. Purpose

This document defines the initial architecture for TestedCloud Chat.

TestedCloud Chat is a secure, cloud-backed messaging application within the TestedCloud ecosystem. It is designed to demonstrate application modernization using Android, Firebase, Firestore, Cloud Run, Pub/Sub, BigQuery, Cloud Monitoring, IAM, and production-style documentation.

The architecture should remain simple enough to build as an MVP, but strong enough to support a professional cloud portfolio narrative.

## 2\. Architecture Goals

Primary architecture goals:

* Provide secure user authentication.
* Support real-time chat using Firestore.
* Use Firebase and Google Cloud services in a clean application architecture.
* Track important product events.
* Connect application events to the existing TestedCloud analytics pipeline.
* Store analytics events in BigQuery.
* Support dashboarding through Looker Studio.
* Apply least-privilege IAM where backend services are used.
* Keep cost low by using serverless and managed services.
* Document decisions, risks, and validation evidence.

## 3\. High-Level Architecture

```text
Android App
    |
    | Firebase SDK
    v
Firebase Authentication
    |
    v
Cloud Firestore
    |
    v
Real-time chat data


Android App
    |
    | HTTPS analytics event
    v
Cloud Run Event Collector
    |
    v
Pub/Sub
    |
    v
Cloud Run Consumer
    |
    v
BigQuery
    |
    v
Looker Studio / Analytics Dashboard
```

## 4\. Architecture Summary

|Layer|Component|Purpose|
|-|-|-|
|Client|Android app|User interface for chat|
|Identity|Firebase Authentication|User authentication|
|Real-time data|Firestore|Users, conversations, messages|
|Event ingestion|Cloud Run event collector|Receives product analytics events|
|Event transport|Pub/Sub|Decouples event producers and consumers|
|Event processing|Cloud Run consumer|Validates and writes events|
|Analytics storage|BigQuery|Stores event analytics|
|Dashboard|Looker Studio|Visualizes usage and behavior|
|Observability|Cloud Monitoring / Logs|Tracks service health|
|Security|IAM / Firestore Rules|Controls access|

## 5\. MVP Architecture

The first MVP should be intentionally simple.

```text
Android App
    |
    v
Firebase Authentication
    |
    v
Firestore
    |
    v
Users / Conversations / Messages
```

MVP capabilities:

* Sign in
* Sign out
* Create or load user profile
* View conversation list
* Open one-to-one chat
* Send text message
* Receive messages in real time
* Enforce Firestore security rules

MVP does not require Cloud Run or Pub/Sub to function as a chat app. Those are added in the analytics phase.

## 6\. Extended Analytics Architecture

The extended architecture connects TestedCloud Chat to the broader TestedCloud event-driven analytics model.

```text
TestedCloud Chat Android App
    |
    | POST /events
    v
Cloud Run: testedcloud-chat-events-api
    |
    | publish
    v
Pub/Sub: testedcloud-chat-events
    |
    | push subscription
    v
Cloud Run: testedcloud-chat-consumer
    |
    | insert rows
    v
BigQuery: testedcloud\_chat.events
    |
    v
Looker Studio dashboard
```

This supports the portfolio story:

> A real mobile app produces operational and product events that are ingested through serverless APIs, transported through Pub/Sub, processed by Cloud Run, stored in BigQuery, and visualized in dashboards.

## 7\. Recommended Google Cloud Resource Names

Recommended resource naming should remain consistent with the existing TestedCloud project style.

Project:

```text
majestic-layout-255620
```

Firebase app:

```text
testedcloud-chat-android
```

Firestore database:

```text
(default)
```

Cloud Run services:

```text
testedcloud-chat-events-api
testedcloud-chat-consumer
```

Pub/Sub topic:

```text
testedcloud-chat-events
```

Pub/Sub subscription:

```text
testedcloud-chat-consumer-sub
```

BigQuery dataset:

```text
testedcloud\_chat
```

BigQuery table:

```text
events
```

Service accounts:

```text
testedcloud-chat-api-sa
testedcloud-chat-consumer-sa
```

## 8\. Client Architecture

The Android app should use a clean separation of concerns.

Recommended Android structure:

```text
apps/testedcloud-chat/
├── app/
│   ├── ui/
│   │   ├── auth/
│   │   ├── conversations/
│   │   ├── chat/
│   │   └── profile/
│   ├── data/
│   │   ├── auth/
│   │   ├── users/
│   │   ├── conversations/
│   │   ├── messages/
│   │   └── analytics/
│   ├── domain/
│   │   ├── models/
│   │   └── usecases/
│   └── core/
│       ├── config/
│       ├── navigation/
│       └── design/
```

Recommended stack:

```text
Kotlin
Jetpack Compose
Firebase Authentication
Cloud Firestore
Firebase SDK
Material 3
```

## 9\. Client Screens

MVP screens:

```text
LoginScreen
ConversationListScreen
ChatScreen
ProfileScreen
```

Future screens:

```text
CreateConversationScreen
SettingsScreen
PrivacyScreen
DebugEventScreen
AdminDiagnosticsScreen
```

## 10\. Firebase Authentication

Firebase Authentication provides user identity.

Initial sign-in options:

* Email/password
* Google sign-in

Recommended MVP path:

```text
Email/password first
Google sign-in later
```

Reason:

Email/password is easier to implement and validate first. Google sign-in can be added after the core app is stable.

Authentication requirements:

* User must be signed in to access conversations.
* UID is the primary user identity.
* UID is used in Firestore security rules.
* User profile should be created after first sign-in.

## 11\. Firestore Data Architecture

Primary collections:

```text
users
conversations
```

Subcollection:

```text
conversations/{conversationId}/messages
```

High-level model:

```text
users/{userId}
conversations/{conversationId}
conversations/{conversationId}/messages/{messageId}
```

## 12\. Firestore Collection: users

Path:

```text
users/{userId}
```

Purpose:

Stores public user profile metadata needed for the chat MVP.

Example:

```json
{
  "uid": "uid\_123",
  "displayName": "Dario",
  "email": "user@example.com",
  "photoUrl": null,
  "createdAt": "timestamp",
  "lastLoginAt": "timestamp",
  "status": "active"
}
```

Security:

* User can read their own profile.
* Other profile visibility should be limited for MVP.
* User can update only approved fields in their own profile.

## 13\. Firestore Collection: conversations

Path:

```text
conversations/{conversationId}
```

Purpose:

Stores metadata for one-to-one conversations.

Example:

```json
{
  "conversationId": "conversation\_123",
  "type": "direct",
  "participantIds": \["uid\_1", "uid\_2"],
  "createdAt": "timestamp",
  "updatedAt": "timestamp",
  "lastMessageText": "Hello",
  "lastMessageAt": "timestamp",
  "createdBy": "uid\_1"
}
```

Security:

* Authenticated users can read only conversations where their UID is in `participantIds`.
* Authenticated users can create conversations only if their UID is included in `participantIds`.
* Users should not be able to add themselves to arbitrary conversations after creation without validation.

## 14\. Firestore Subcollection: messages

Path:

```text
conversations/{conversationId}/messages/{messageId}
```

Purpose:

Stores messages for a conversation.

Example:

```json
{
  "messageId": "message\_123",
  "conversationId": "conversation\_123",
  "senderId": "uid\_1",
  "text": "Hello from TestedCloud Chat",
  "createdAt": "timestamp",
  "updatedAt": null,
  "status": "sent"
}
```

Security:

* User can read messages only if they are a participant in the parent conversation.
* User can create a message only if `senderId` equals their authenticated UID.
* User cannot send a message into a conversation where they are not a participant.

## 15\. Firestore Security Rule Strategy

The first security rule strategy should focus on participant-based access.

Core checks:

```text
request.auth != null
request.auth.uid in conversation.participantIds
message.senderId == request.auth.uid
```

Security rule principles:

* Deny by default.
* Explicitly allow required operations.
* Keep rules readable.
* Avoid overly broad reads.
* Test rules before demoing the app.

## 16\. Analytics Event Architecture

Analytics events should be separated from chat data.

The chat app should continue working even if analytics fails.

Event flow:

```text
App action
    |
    v
Create analytics event payload
    |
    v
Send event to Cloud Run collector
    |
    v
Cloud Run validates event
    |
    v
Pub/Sub publishes event
    |
    v
Consumer processes event
    |
    v
BigQuery stores event
```

## 17\. Analytics Event Types

MVP events:

```text
app\_opened
user\_signed\_in
user\_signed\_out
conversation\_created
message\_sent
```

Future events:

```text
message\_read
profile\_updated
notification\_received
notification\_opened
ai\_suggestion\_requested
ai\_suggestion\_accepted
```

## 18\. Analytics Event Payload

Recommended payload:

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
    "message\_length": 42,
    "platform": "android",
    "app\_version": "0.1.0"
  }
}
```

## 19\. Cloud Run Event Collector

Proposed service:

```text
testedcloud-chat-events-api
```

Responsibilities:

* Receive events from Android app.
* Validate event shape.
* Validate authentication if required.
* Add server-side metadata.
* Publish to Pub/Sub.
* Return success/failure response.

The service should not:

* Store chat messages.
* Bypass Firestore rules.
* Store secrets in code.
* Require always-on infrastructure.

Recommended endpoint:

```text
POST /events
```

## 20\. Pub/Sub Topic

Topic:

```text
testedcloud-chat-events
```

Purpose:

Decouples event ingestion from event processing.

Benefits:

* Buffering
* Retry behavior
* Independent scaling
* Future support for additional consumers
* Good portfolio alignment with event-driven architecture

## 21\. Cloud Run Consumer

Service:

```text
testedcloud-chat-consumer
```

Responsibilities:

* Receive Pub/Sub push messages.
* Decode event payload.
* Validate required fields.
* Insert rows into BigQuery.
* Log processing status.
* Return correct HTTP response code.

## 22\. BigQuery Analytics

Dataset:

```text
testedcloud\_chat
```

Table:

```text
events
```

Suggested schema:

```text
event\_id STRING
event\_type STRING
source STRING
origin STRING
user\_id STRING
conversation\_id STRING
message\_id STRING
created\_at TIMESTAMP
processed\_at TIMESTAMP
metadata JSON
```

If JSON type is not desired, metadata can be stored as STRING.

## 23\. Looker Studio Dashboard

Initial dashboard metrics:

* App opens
* Sign-ins
* Messages sent
* Conversations created
* Events by type
* Events over time
* Processing latency
* Error count

Potential dashboard sections:

```text
Usage Overview
Messaging Activity
User Activity
Event Pipeline Health
Cost / Volume Indicators
```

## 24\. IAM Model

Service accounts:

```text
testedcloud-chat-api-sa
testedcloud-chat-consumer-sa
```

Recommended roles:

For `testedcloud-chat-api-sa`:

```text
Pub/Sub Publisher on testedcloud-chat-events
Cloud Logging Writer
```

For `testedcloud-chat-consumer-sa`:

```text
BigQuery Data Editor on testedcloud\_chat dataset
Cloud Logging Writer
```

Avoid granting broad project-level roles where dataset-level or topic-level roles are sufficient.

## 25\. Secrets and Configuration

Do not commit:

```text
google-services.json if it contains sensitive project details you do not want public
service account keys
.env files
API keys intended to remain private
OAuth client secrets
notification channel IDs
```

Important note:

Firebase client configuration is not a service account secret, but it should still be handled intentionally. If the repository is public, document what is safe to expose and what is not.

## 26\. Observability

Observability should include:

* Android client errors where practical
* Firestore rule testing
* Cloud Run logs
* Pub/Sub delivery metrics
* BigQuery insert errors
* Cloud Monitoring alert policies
* Dashboard validation

Potential alerts:

```text
Cloud Run 5xx errors for chat event collector
Cloud Run 5xx errors for chat consumer
Pub/Sub backlog for chat subscription
BigQuery insert failures
Unexpected event volume spike
```

## 27\. Cost Model

Cost-conscious design:

* Firestore for low-volume real-time data
* Cloud Run scale-to-zero
* Pub/Sub low-volume events
* BigQuery small event table
* Looker Studio for dashboarding
* No always-on VM required

Cost risks:

* Excessive Firestore reads due to poor query design
* High-frequency event tracking
* Large message history reads
* Unbounded analytics event generation
* Large BigQuery scans without partitioning

Recommended controls:

* Limit message query size.
* Use pagination.
* Keep analytics event volume low.
* Partition BigQuery table by date later.
* Reuse existing budget alerts.

## 28\. Privacy Considerations

MVP should collect minimal data.

Potential user data:

```text
email
display name
UID
message text
timestamps
usage events
```

Privacy requirements before public release:

* Privacy policy
* Data safety disclosures
* User support contact
* Clear explanation of collected data
* Avoid unnecessary sensitive data collection

## 29\. Security Considerations

Key risks:

* Users reading conversations they do not belong to
* Users spoofing sender ID
* Users writing malformed messages
* Unauthenticated event spam to collector API
* Overly broad IAM roles
* Public repo accidentally exposing secrets

Mitigations:

* Firestore rules
* Authenticated requests
* Sender ID validation
* Server-side event validation
* Least-privilege service accounts
* Secret scanning before commits
* `.gitignore` for local config

## 30\. Deployment Strategy

Phase 1 deployment:

```text
Firebase project configured
Android app local build
Firestore rules deployed
Manual testing with test users
```

Phase 2 deployment:

```text
Cloud Run event collector deployed
Pub/Sub topic created
Consumer deployed
BigQuery dataset/table created
Monitoring alerts configured
```

Phase 3 deployment:

```text
Portfolio page updated
Documentation committed
Evidence captured
Demo screenshots added
```

## 31\. Validation Plan

Validation evidence should be captured in:

```text
docs/evidence/testedcloud-chat/
```

Recommended evidence files:

```text
firebase-auth-validation.txt
firestore-rules-validation.txt
message-send-validation.txt
chat-realtime-validation.txt
chat-event-pipeline-validation.txt
bigquery-events-validation.txt
dashboard-validation.txt
```

## 32\. Architecture Decisions

## 32.1 Use Firebase Auth

Decision:

Use Firebase Authentication for identity.

Reason:

It is native to Firebase, easy to integrate with Android, supports Google Cloud portfolio goals, and works well with Firestore rules.

## 32.2 Use Firestore for Chat Data

Decision:

Use Firestore for users, conversations, and messages.

Reason:

Firestore provides real-time updates, offline-friendly client support, and direct integration with Firebase Authentication security rules.

## 32.3 Use Cloud Run + Pub/Sub for Analytics

Decision:

Use a custom analytics event pipeline through Cloud Run and Pub/Sub.

Reason:

It connects the app to the existing TestedCloud event-driven architecture and demonstrates serverless application analytics beyond basic Firebase usage.

## 32.4 Keep Chat Functionality Independent From Analytics

Decision:

Chat sending should not depend on analytics success.

Reason:

Analytics should not block core user functionality. Failed analytics events should be logged and retried later if necessary.

## 33\. Future Architecture Enhancements

Potential future additions:

* Firebase Cloud Messaging notifications
* AI-assisted message suggestions
* Vertex AI integration
* Chat summarization
* Admin dashboard
* Web app client
* Cloud Tasks for async workflows
* BigQuery partitioned tables
* Dataform or scheduled queries
* CI/CD for Android builds
* GitHub Actions for backend deployments
* Play Store closed testing workflow

## 34\. Final Architecture Positioning

TestedCloud Chat should be described as:

> A Firebase-backed Android messaging application connected to Google Cloud analytics through Cloud Run, Pub/Sub, BigQuery, and Looker Studio, designed with least-privilege IAM, Firestore security rules, privacy awareness, and production-style documentation.

This architecture strengthens the TestedCloud portfolio by showing application modernization in addition to hybrid infrastructure modernization.

