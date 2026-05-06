# TestedCloud Chat — Roadmap

## 1\. Purpose

This document defines the roadmap for TestedCloud Chat.

TestedCloud Chat is the first application module in the TestedCloud ecosystem. It extends the TestedCloud portfolio from hybrid infrastructure modernization into application modernization using Firebase, Firestore, Android, Cloud Run, Pub/Sub, BigQuery, and production-style documentation.

The roadmap is intentionally phased so the project remains achievable, demonstrable, and useful for portfolio and interview conversations.

## 2\. Product Positioning

Recommended positioning:

> TestedCloud Chat is a secure, cloud-backed messaging demo within the TestedCloud ecosystem. It demonstrates Firebase Authentication, Firestore real-time messaging, Google Cloud event analytics, IAM discipline, observability, privacy awareness, and production-style application modernization documentation.

Short positioning:

```text
Secure real-time messaging demo powered by Firebase and Google Cloud.
```

Portfolio positioning:

```text
Infrastructure modernization: TestedCloud Core Platform
Application modernization: TestedCloud Chat
```

## 3\. Roadmap Principles

The roadmap follows these principles:

* Build in small validated increments.
* Document before and after implementation.
* Capture evidence for each major milestone.
* Prioritize security and privacy early.
* Keep the MVP small.
* Avoid overbuilding the chat app.
* Connect application events to the existing TestedCloud analytics pipeline.
* Use low-cost managed/serverless services.
* Prepare for Google Play only after MVP validation.
* Keep the public portfolio narrative clear.

## 4\. Phase Overview

|Phase|Name|Main Outcome|
|-|-|-|
|Phase 0|Planning and Documentation|Product, architecture, data, security, analytics, and roadmap docs|
|Phase 1|Firebase Foundation|Firebase app, Auth, Firestore, initial rules|
|Phase 2|Android MVP Skeleton|Android app shell with branding and navigation|
|Phase 3|Real-Time Messaging MVP|Users, conversations, and messages working|
|Phase 4|Security Validation|Firestore rules tested and evidence captured|
|Phase 5|Analytics Pipeline|App events to Cloud Run, Pub/Sub, BigQuery|
|Phase 6|Dashboard and Monitoring|Looker Studio dashboard and Cloud Monitoring alerts|
|Phase 7|Portfolio Integration|Public landing page and documentation links|
|Phase 8|Google Play Closed Testing Prep|Privacy, data safety, app listing, internal/closed testing|
|Phase 9|Future Enhancements|AI, notifications, web client, industrial workflows|

## 5\. Phase 0 — Planning and Documentation

### Objective

Create the foundational documentation before implementation.

### Deliverables

```text
docs/testedcloud-chat/product-requirements.md
docs/testedcloud-chat/architecture.md
docs/testedcloud-chat/data-model.md
docs/testedcloud-chat/security-privacy.md
docs/testedcloud-chat/analytics-events.md
docs/testedcloud-chat/roadmap.md
```

### Success Criteria

* Product scope is clear.
* MVP is defined.
* Architecture is documented.
* Data model is documented.
* Security and privacy expectations are documented.
* Analytics events are documented.
* Implementation phases are clear.

### Status

```text
In progress
```

## 6\. Phase 1 — Firebase Foundation

### Objective

Create the Firebase foundation for TestedCloud Chat.

### Deliverables

```text
Firebase project/app configured
Android app registered in Firebase
Firebase Authentication enabled
Cloud Firestore enabled
Initial Firestore rules drafted
Local Firebase configuration added to Android project
```

### Recommended Decisions

MVP sign-in method:

```text
Email/password
```

Future sign-in method:

```text
Google sign-in
```

Firestore mode:

```text
Native mode
```

### Evidence to Capture

```text
docs/evidence/testedcloud-chat/firebase-project-validation.txt
docs/evidence/testedcloud-chat/firebase-auth-config.txt
docs/evidence/testedcloud-chat/firestore-config.txt
```

### Success Criteria

* Firebase app exists.
* Firebase Authentication is enabled.
* Firestore database exists.
* Android app can connect to Firebase.
* No service account keys are committed.

## 7\. Phase 2 — Android MVP Skeleton

### Objective

Create the Android project skeleton with TestedCloud branding.

### Recommended Stack

```text
Kotlin
Jetpack Compose
Material 3
Firebase SDK
Firestore SDK
```

### Deliverables

```text
apps/testedcloud-chat/
Android package configured
Basic app theme
TestedCloud branding applied
Navigation structure created
Login screen placeholder
Conversation list placeholder
Chat screen placeholder
Profile screen placeholder
```

### Recommended Package Name

```text
com.testedcloud.chat
```

### Success Criteria

* App builds locally.
* App launches on emulator or Android device.
* Navigation works between placeholder screens.
* Branding is visible.
* No secrets are committed.

### Evidence to Capture

```text
docs/evidence/testedcloud-chat/android-build-validation.txt
docs/evidence/testedcloud-chat/android-screenshot-home.txt
```

## 8\. Phase 3 — Real-Time Messaging MVP

### Objective

Implement the core chat functionality.

### Deliverables

```text
Sign in
Sign out
Create/load user profile
Conversation list
One-to-one conversation
Send message
Read messages in real time
Last message preview
Basic loading/error states
```

### Firestore Collections

```text
users/{userId}
conversations/{conversationId}
conversations/{conversationId}/messages/{messageId}
```

### Success Criteria

* User can sign in.
* User profile is created.
* User can see conversations where they are a participant.
* User can send a message.
* Message appears in real time.
* Conversation last message metadata updates.
* App remains stable during basic testing.

### Evidence to Capture

```text
docs/evidence/testedcloud-chat/sign-in-validation.txt
docs/evidence/testedcloud-chat/user-profile-validation.txt
docs/evidence/testedcloud-chat/message-send-validation.txt
docs/evidence/testedcloud-chat/realtime-chat-validation.txt
```

## 9\. Phase 4 — Security Validation

### Objective

Validate that Firestore rules protect chat data correctly.

### Security Tests

|Test|Expected Result|
|-|-|
|Unauthenticated read|Denied|
|Authenticated user reads own profile|Allowed|
|User reads another user's restricted data|Denied|
|User reads own conversation|Allowed|
|User reads unrelated conversation|Denied|
|User sends message as self|Allowed|
|User spoofs `senderId`|Denied|
|User writes to conversation not participating in|Denied|
|User sends empty message|Denied|
|User sends oversized message|Denied|

### Deliverables

```text
Firestore rules file
Rules test notes
Security validation evidence
Denied access evidence
Allowed access evidence
```

### Evidence to Capture

```text
docs/evidence/testedcloud-chat/firestore-rules-validation.txt
docs/evidence/testedcloud-chat/unauthorized-read-denied.txt
docs/evidence/testedcloud-chat/sender-spoof-denied.txt
```

### Success Criteria

* Rules deny unauthorized access.
* Rules allow expected MVP functionality.
* Security evidence is documented.
* No broad public Firestore access exists.

## 10\. Phase 5 — Analytics Pipeline

### Objective

Connect app events to Google Cloud analytics.

### Event Flow

```text
Android App
    |
    v
Cloud Run Event Collector
    |
    v
Pub/Sub: testedcloud-chat-events
    |
    v
Cloud Run Consumer
    |
    v
BigQuery: testedcloud\_chat.events
```

### MVP Events

```text
app\_opened
user\_signed\_in
user\_signed\_out
conversation\_created
message\_sent
```

### Deliverables

```text
Cloud Run event collector
Pub/Sub topic
Pub/Sub subscription
Cloud Run consumer
BigQuery dataset
BigQuery events table
Event validation logic
Basic event logs
```

### Success Criteria

* App can send event to collector.
* Collector validates payload.
* Collector publishes to Pub/Sub.
* Consumer receives event.
* Consumer writes to BigQuery.
* Events are queryable in BigQuery.
* Message text is not stored in analytics.

### Evidence to Capture

```text
docs/evidence/testedcloud-chat/chat-event-api-validation.txt
docs/evidence/testedcloud-chat/chat-pubsub-validation.txt
docs/evidence/testedcloud-chat/chat-bigquery-events-sample.txt
```

## 11\. Phase 6 — Dashboard and Monitoring

### Objective

Create observability and analytics dashboards.

### Dashboard Metrics

```text
Total events
App opens
Sign-ins
Messages sent
Conversations created
Events by type
Events over time
Processing latency
```

### Monitoring Alerts

Recommended alerts:

```text
Cloud Run 5xx errors for chat event collector
Cloud Run 5xx errors for chat consumer
Pub/Sub backlog for chat subscription
DLQ messages for chat event pipeline
Unexpected event volume spike
```

### Deliverables

```text
BigQuery views
Looker Studio dashboard
Cloud Monitoring alert policies
Monitoring evidence
Dashboard evidence
```

### Success Criteria

* Dashboard shows chat analytics.
* Monitoring alerts are documented.
* Event pipeline health can be observed.
* Evidence is committed.

### Evidence to Capture

```text
docs/evidence/testedcloud-chat/chat-dashboard-validation.txt
docs/evidence/testedcloud-chat/chat-monitoring-alerts-validation.txt
```

## 12\. Phase 7 — Portfolio Integration

### Objective

Integrate TestedCloud Chat into the public portfolio.

### Public Site Updates

Recommended URL:

```text
https://testedcloud.com/chat
```

Landing page section:

```text
TestedCloud Chat
Secure real-time messaging demo powered by Firebase, Firestore, Cloud Run, Pub/Sub, and BigQuery analytics.
```

### Deliverables

```text
Portfolio card for TestedCloud Chat
Public product page or section
Links to documentation
Architecture summary
Screenshots or mockups
Evidence summary
```

### Success Criteria

* Public landing page references TestedCloud Chat.
* Documentation is discoverable from repo.
* Architecture and roadmap are easy to explain.
* Protected lab remains separate.

## 13\. Phase 8 — Google Play Closed Testing Preparation

### Objective

Prepare for a controlled Google Play closed testing release.

### Deliverables

```text
App icon
App screenshots
Short description
Full description
Privacy policy URL
Support URL
Data safety form notes
Closed testing track
Internal release notes
Crash/usage monitoring strategy
```

### Recommended URLs

```text
https://testedcloud.com/chat
https://testedcloud.com/privacy
https://testedcloud.com/support
```

### Success Criteria

* App can be built as release artifact.
* Privacy policy exists.
* Support URL exists.
* Data safety answers are drafted.
* Closed testing path is documented.

### Important Note

This phase should not begin until the MVP and security validations are complete.

## 14\. Phase 9 — Future Enhancements

Potential future features:

```text
Firebase Cloud Messaging notifications
Read receipts
Typing indicators
Message search
Conversation archiving
Profile pictures
Google sign-in
Web client
Admin diagnostics
AI-assisted message suggestions
AI conversation summaries
Vertex AI integration
Industrial operations chat rooms
SINEC/OT alert notifications
```

Potential cloud enhancements:

```text
BigQuery partitioning
Dataform transformations
Scheduled analytics queries
Cloud Tasks for async workflows
Cloud Armor for public API protection
Firebase App Check
Secret Manager
CI/CD for Android and Cloud Run
```

## 15\. AI Roadmap

AI should be added only after core chat and analytics are stable.

Possible AI features:

```text
AI-assisted message drafting
Conversation summarization
Operational alert explanation
Recommended troubleshooting steps
Chat-based query over lab documentation
SINEC/industrial telemetry assistant
```

Recommended AI positioning:

```text
AI should assist users, not replace secure messaging fundamentals.
```

## 16\. Industrial Telemetry Integration Roadmap

Future TestedCloud Chat can become a collaboration layer for industrial telemetry.

Potential examples:

```text
Network alert appears in BigQuery or dashboard
Cloud Run sends event to TestedCloud Chat
User receives operational notification
AI summarizes alert context
Engineer discusses next action in chat
```

Potential sources:

```text
SINEC NMS
SCALANCE
Ruggedcom
SNMP
Syslog
Cloud Monitoring
BigQuery anomalies
```

This would create a strong link between the user's industrial networking background and Google Cloud application modernization.

## 17\. Branding Roadmap

Current ecosystem structure:

```text
TestedCloud
├── TestedCloud Lab
├── TestedCloud Chat
├── TestedCloud AI
└── TestedCloud Edge
```

Recommended naming:

```text
TestedCloud Chat
```

Avoid primary usage:

```text
TestedChat
```

Reason:

The main domain and public brand strategy are centered on `testedcloud.com`.

## 18\. Repository Milestones

Recommended commit sequence:

```text
Add TestedCloud Chat product requirements
Add TestedCloud Chat architecture
Add TestedCloud Chat data model
Add TestedCloud Chat security and privacy model
Add TestedCloud Chat analytics events model
Add TestedCloud Chat roadmap
Add Firebase setup notes
Add Android app skeleton
Add Firestore data model implementation
Add Firestore security rules
Add chat MVP
Add chat analytics event collector
Add chat BigQuery analytics
Add TestedCloud Chat portfolio page
```

## 19\. Suggested Folder Structure

```text
testedcloud-lab/
├── apps/
│   └── testedcloud-chat/
├── cloudrun/
│   └── testedcloud-chat-api/
├── firebase/
│   └── testedcloud-chat/
├── docs/
│   └── testedcloud-chat/
│       ├── product-requirements.md
│       ├── architecture.md
│       ├── data-model.md
│       ├── security-privacy.md
│       ├── analytics-events.md
│       └── roadmap.md
└── docs/
    └── evidence/
        └── testedcloud-chat/
```

## 20\. Portfolio Narrative

Short narrative:

> TestedCloud Chat extends the TestedCloud platform from infrastructure modernization into application modernization. It demonstrates Firebase Authentication, Firestore real-time messaging, serverless analytics ingestion, Pub/Sub event transport, BigQuery analytics, Looker Studio dashboards, IAM discipline, and privacy-aware design.

Interview narrative:

> I started with a hybrid cloud infrastructure lab, then extended the portfolio with TestedCloud Chat, a Firebase-backed messaging app. The app demonstrates secure authentication, real-time Firestore data, participant-based access control, and a custom event pipeline into Cloud Run, Pub/Sub, BigQuery, and Looker Studio. This allowed me to show both infrastructure modernization and application modernization using Google Cloud patterns.

## 21\. Success Definition

The project is successful when:

```text
An Android user can sign in.
The user can send and receive messages.
Firestore rules protect conversation data.
Events are sent to the analytics pipeline.
Events appear in BigQuery.
A dashboard shows usage metrics.
The architecture is documented.
The security model is documented.
Evidence is captured.
The public portfolio references the project.
```

## 22\. Current Recommended Next Step

After committing this roadmap, the next practical step is:

```text
Create Firebase setup plan
```

Recommended next file:

```text
docs/testedcloud-chat/firebase-setup.md
```

Then implementation can begin with Firebase project/app setup.

## 23\. Final Positioning

TestedCloud Chat should remain focused, secure, and demonstrable.

It should not try to become a full messaging platform during the MVP. Its main value is to demonstrate that the TestedCloud ecosystem can support a real application workload with:

* identity
* real-time data
* security rules
* analytics
* serverless backend integration
* dashboarding
* monitoring
* privacy awareness
* production-style documentation

This is the right next step after the TestedCloud Core Platform because it turns the infrastructure lab into a broader cloud application modernization portfolio.

