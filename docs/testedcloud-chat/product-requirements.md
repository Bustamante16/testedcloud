# TestedCloud Chat — Product Requirements Document

## 1\. Purpose

TestedCloud Chat is the first application module in the TestedCloud ecosystem.

The goal is to build a secure, cloud-backed messaging application that demonstrates mobile application architecture, Firebase integration, real-time data synchronization, event-driven analytics, IAM discipline, observability, and production-style documentation.

This project extends TestedCloud from a hybrid cloud infrastructure lab into a product-oriented cloud portfolio demo.

## 2\. Product Name

Recommended public name:

```text
TestedCloud Chat
```

Internal shorthand:

```text
TC Chat
```

Avoid using a separate standalone brand such as `TestedChat` as the primary brand because the current domain strategy is centered on:

```text
testedcloud.com
```

Recommended public URLs:

```text
testedcloud.com/chat
chat.testedcloud.com
```

The first URL can be used for the public product landing page.  
The second URL can be reserved for a future web app or protected chat interface.

## 3\. Strategic Fit

TestedCloud Chat supports the broader TestedCloud roadmap by demonstrating how a cloud-backed application can be designed, secured, monitored, and connected to analytics.

Current TestedCloud platform:

```text
On-prem UI/API
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
Looker Studio / Monitoring
```

TestedCloud Chat adds:

```text
Android App
    |
    v
Firebase Auth
    |
    v
Firestore
    |
    v
Cloud Run / Pub/Sub / BigQuery Analytics
```

This creates a strong portfolio story:

> TestedCloud demonstrates hybrid infrastructure modernization. TestedCloud Chat demonstrates application modernization using Firebase, serverless APIs, event-driven analytics, and secure cloud architecture.

## 4\. Target Audience

Primary audience:

* Google Cloud Customer Engineer recruiters
* Cloud hiring managers
* Technical interviewers
* Cloud architecture reviewers
* Portfolio reviewers
* Future users of the TestedCloud demo ecosystem

Secondary audience:

* Developers learning Firebase and Google Cloud integration
* Industrial / OT teams exploring cloud-assisted collaboration
* Internal demo users authorized by the project owner

## 5\. Problem Statement

Many organizations need lightweight collaboration tools that can securely connect operational users, cloud services, and analytics workflows.

TestedCloud Chat is not intended to compete with full-scale messaging platforms. Instead, it demonstrates how to design and document a secure, cloud-backed messaging application using Google Cloud and Firebase technologies.

The product answers this portfolio question:

> Can the TestedCloud platform support a real application workload with identity, real-time data, event tracking, analytics, security rules, and operational documentation?

## 6\. Product Goals

### 6.1 Primary Goals

* Build a working Android messaging MVP.
* Use Firebase Authentication for user identity.
* Use Firestore for real-time chat messages.
* Implement basic user profiles.
* Implement basic conversation and message structures.
* Add Firestore security rules.
* Track important product events.
* Send selected events to Google Cloud analytics.
* Store analytics events in BigQuery.
* Visualize usage and product behavior in Looker Studio.
* Document architecture, security, data model, and evidence.

### 6.2 Portfolio Goals

* Demonstrate Firebase + Google Cloud integration.
* Demonstrate application-level architecture.
* Demonstrate secure data access patterns.
* Demonstrate event-driven analytics.
* Demonstrate production-style planning.
* Demonstrate privacy and data safety awareness.
* Demonstrate a roadmap for future AI-assisted functionality.

### 6.3 Career Goals

This project should support narratives for roles such as:

* Google Cloud Customer Engineer
* Cloud Architect
* Solutions Architect
* Application Modernization Specialist
* Firebase / Serverless Solutions Engineer
* Hybrid Cloud / Edge Modernization Consultant

## 7\. Non-Goals

The MVP will not attempt to build a full commercial messaging platform.

Out of scope for initial releases:

* End-to-end encryption
* Voice calls
* Video calls
* Group calls
* Large-scale media sharing
* Public user discovery
* Payment processing
* Enterprise admin console
* Full moderation system
* AI assistant responses in Release 1
* Google Play public production release in the first phase

These may be considered later after the architecture and MVP are stable.

## 8\. Product Scope

### 8.1 Release 0 — Planning and Architecture

Release 0 creates documentation and repository structure before writing production app code.

Deliverables:

```text
docs/testedcloud-chat/product-requirements.md
docs/testedcloud-chat/architecture.md
docs/testedcloud-chat/data-model.md
docs/testedcloud-chat/security-privacy.md
docs/testedcloud-chat/analytics-events.md
docs/testedcloud-chat/roadmap.md
```

Repository folders:

```text
apps/testedcloud-chat/
firebase/testedcloud-chat/
cloudrun/testedcloud-chat-api/
docs/testedcloud-chat/
```

Purpose:

* Define the product.
* Define the MVP.
* Define the architecture.
* Define the data model.
* Define security expectations.
* Define analytics events.
* Prepare implementation.

### 8.2 Release 1 — Android Chat MVP

Release 1 creates a basic Android app.

Required functionality:

* Firebase Authentication
* Sign in / sign out
* Basic user profile
* Conversation list
* One-to-one chat
* Send message
* Receive messages in real time
* Message timestamp
* Firestore security rules
* Basic error handling
* Basic loading states

Required screens:

```text
Login screen
Conversation list screen
Chat screen
Profile/settings screen
```

Recommended technology:

```text
Android Kotlin
Jetpack Compose
Firebase Authentication
Firestore
Firebase SDK
```

### 8.3 Release 2 — Analytics and Event Pipeline

Release 2 adds analytics flow to Google Cloud.

Events to track:

```text
user\_signed\_in
user\_signed\_out
conversation\_created
message\_sent
message\_received
message\_read
profile\_updated
app\_opened
```

Potential analytics pipeline:

```text
Android App
    |
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
Looker Studio
```

Alternative:

```text
Firebase Analytics
    |
    v
BigQuery Export
```

Recommended for portfolio value:

Use a custom Cloud Run event collector so the architecture connects directly to the existing TestedCloud event-driven pipeline.

### 8.4 Release 3 — Portfolio Integration

Release 3 integrates TestedCloud Chat into the public portfolio.

Landing page update:

```text
testedcloud.com/chat
```

Public portfolio card:

```text
TestedCloud Chat
Secure real-time messaging demo powered by Firebase, Firestore, Cloud Run, Pub/Sub, and BigQuery analytics.
```

Documentation links:

```text
Architecture
Data Model
Security \& Privacy
Analytics Events
Validated Tests
```

### 8.5 Release 4 — Google Play Closed Testing Preparation

Release 4 prepares the app for Google Play closed testing.

Required items:

* App name
* App icon
* Package name
* Privacy policy URL
* Support contact URL
* Data safety form preparation
* Closed testing track
* Internal release notes
* Crash/usage monitoring
* Screenshots
* Basic Play Store listing

Recommended URLs:

```text
https://testedcloud.com/chat
https://testedcloud.com/privacy
https://testedcloud.com/support
```

## 9\. User Stories

### 9.1 Authentication

As a user, I want to sign in securely so that my messages are associated with my identity.

Acceptance criteria:

* User can sign in.
* User can sign out.
* User identity is available to the app.
* Unauthorized users cannot access chat data.

### 9.2 User Profile

As a user, I want a basic profile so other users can recognize me.

Acceptance criteria:

* User has display name.
* User has email or unique identifier.
* User has created timestamp.
* User profile is stored in Firestore.

### 9.3 Conversation List

As a user, I want to see my conversations so I can open an existing chat.

Acceptance criteria:

* User sees only conversations where they are a participant.
* Conversations display latest message preview.
* Conversations display last updated time.

### 9.4 One-to-One Messaging

As a user, I want to send and receive messages in real time.

Acceptance criteria:

* User can send a text message.
* Message appears in the chat thread.
* Recipient sees the message in real time.
* Messages include sender ID and timestamp.

### 9.5 Event Tracking

As the platform owner, I want important application events to be tracked so that usage can be analyzed.

Acceptance criteria:

* Message sent events are captured.
* Sign-in events are captured.
* Conversation creation events are captured.
* Events are stored in analytics destination.
* Events can be queried in BigQuery.

### 9.6 Security Rules

As the platform owner, I want Firestore rules to prevent unauthorized access.

Acceptance criteria:

* User can only read conversations where they are a participant.
* User can only create messages as themselves.
* User cannot spoof sender ID.
* User cannot read unrelated conversations.

## 10\. Functional Requirements

### 10.1 Authentication

* Use Firebase Authentication.
* Support at least one sign-in method for MVP.
* Recommended MVP method: email/password or Google sign-in.
* Store authenticated user UID.
* Use UID in Firestore access rules.

### 10.2 Users

Firestore collection:

```text
users/{userId}
```

Fields:

```text
uid
displayName
email
photoUrl
createdAt
lastLoginAt
status
```

### 10.3 Conversations

Firestore collection:

```text
conversations/{conversationId}
```

Fields:

```text
conversationId
type
participantIds
createdAt
updatedAt
lastMessageText
lastMessageAt
createdBy
```

MVP supports:

```text
type = direct
```

### 10.4 Messages

Firestore subcollection:

```text
conversations/{conversationId}/messages/{messageId}
```

Fields:

```text
messageId
conversationId
senderId
text
createdAt
updatedAt
status
```

MVP message status:

```text
sent
```

Future statuses:

```text
delivered
read
deleted
```

### 10.5 Analytics Events

Potential collection or event payload:

```text
event\_id
event\_type
user\_id
conversation\_id
message\_id
source
origin
created\_at
metadata
```

Events can be sent to:

```text
Cloud Run event collector
Pub/Sub topic
BigQuery table
```

## 11\. Non-Functional Requirements

### 11.1 Security

* Use Firebase Authentication.
* Apply least-privilege Firestore security rules.
* Do not expose service account keys in the app.
* Do not hardcode sensitive secrets in the Android app.
* Do not allow unauthenticated Firestore reads.
* Do not allow users to write messages as other users.

### 11.2 Privacy

* Keep MVP data minimal.
* Avoid collecting unnecessary personal data.
* Document what data is collected.
* Provide a privacy policy before any Play Store release.
* Avoid sensitive data collection in MVP.

### 11.3 Reliability

* Firestore real-time updates should work consistently.
* App should handle offline or poor connectivity gracefully when possible.
* Basic error messages should be shown when operations fail.
* Analytics pipeline failures should not block message sending.

### 11.4 Observability

Track:

* App sign-ins
* Message sends
* Conversation creation
* Cloud Run API errors
* Pub/Sub publish failures
* BigQuery insert errors
* Firestore rule denials during testing

### 11.5 Cost

The MVP should remain low-cost.

Cost-conscious services:

* Firebase Authentication
* Firestore low-volume usage
* Cloud Run scale-to-zero
* Pub/Sub low-volume events
* BigQuery small analytics table
* Looker Studio dashboard

Budget controls:

* Keep existing TestedCloud budget alerts.
* Avoid high-frequency automated event generation.
* Limit dashboard refresh frequency.
* Avoid unnecessary always-on infrastructure.

## 12\. Proposed Architecture

### 12.1 MVP Architecture

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
Real-time messaging
```

### 12.2 Analytics Architecture

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
    |
    v
Looker Studio Dashboard
```

### 12.3 Integration With Existing TestedCloud

Existing TestedCloud components can be extended:

```text
Pub/Sub
Cloud Run
BigQuery
Looker Studio
Cloud Monitoring
IAM
GitHub documentation
Public portfolio
```

Potential new resources:

```text
Firebase project/app
Firestore database
testedcloud-chat-events topic
testedcloud-chat-consumer service
testedcloud\_chat BigQuery dataset
chat analytics views
chat dashboard
```

## 13\. Data Model Draft

### 13.1 users

```json
{
  "uid": "string",
  "displayName": "string",
  "email": "string",
  "photoUrl": "string",
  "createdAt": "timestamp",
  "lastLoginAt": "timestamp",
  "status": "active"
}
```

### 13.2 conversations

```json
{
  "conversationId": "string",
  "type": "direct",
  "participantIds": \["uid\_1", "uid\_2"],
  "createdAt": "timestamp",
  "updatedAt": "timestamp",
  "lastMessageText": "string",
  "lastMessageAt": "timestamp",
  "createdBy": "uid"
}
```

### 13.3 messages

```json
{
  "messageId": "string",
  "conversationId": "string",
  "senderId": "uid",
  "text": "string",
  "createdAt": "timestamp",
  "updatedAt": "timestamp",
  "status": "sent"
}
```

### 13.4 analytics\_events

```json
{
  "event\_id": "string",
  "event\_type": "message\_sent",
  "user\_id": "uid",
  "conversation\_id": "string",
  "message\_id": "string",
  "source": "testedcloud-chat-android",
  "origin": "firebase",
  "created\_at": "timestamp",
  "processed\_at": "timestamp",
  "metadata": {}
}
```

## 14\. Security Model Draft

### 14.1 Firestore Access Principles

* Users must be authenticated.
* Users can read their own profile.
* Users can update limited fields in their own profile.
* Users can read conversations where their UID is included in `participantIds`.
* Users can create messages only in conversations where they are participants.
* `senderId` must match the authenticated UID.
* Users cannot read conversations where they are not participants.

### 14.2 Backend Access Principles

* Android app should not hold service account credentials.
* Cloud Run should use dedicated runtime service accounts.
* Pub/Sub publishing should use least privilege.
* BigQuery writes should use a dedicated service account.
* Public APIs should validate authentication tokens.

## 15\. Analytics Events Draft

### 15.1 Required MVP Events

```text
app\_opened
user\_signed\_in
user\_signed\_out
conversation\_created
message\_sent
```

### 15.2 Future Events

```text
message\_read
profile\_updated
conversation\_deleted
notification\_received
notification\_opened
ai\_suggestion\_requested
ai\_suggestion\_accepted
```

### 15.3 Event Payload Example

```json
{
  "event\_type": "message\_sent",
  "source": "testedcloud-chat-android",
  "origin": "firebase",
  "user\_id": "uid\_123",
  "conversation\_id": "conversation\_456",
  "message\_id": "message\_789",
  "created\_at": "2026-05-05T21:00:00Z",
  "metadata": {
    "message\_length": 42,
    "platform": "android"
  }
}
```

## 16\. Success Metrics

### 16.1 Product Metrics

* User can sign in successfully.
* User can create or access a conversation.
* User can send and receive messages.
* Messages sync in real time.
* Firestore security rules prevent unauthorized access.

### 16.2 Cloud Metrics

* Events reach Pub/Sub.
* Events are processed by Cloud Run.
* Events are stored in BigQuery.
* Looker Studio dashboard shows usage metrics.
* Monitoring alerts are configured for relevant backend services.

### 16.3 Portfolio Metrics

* Public documentation is complete.
* Architecture diagram exists.
* Data model is documented.
* Security model is documented.
* Evidence files exist.
* Landing page references TestedCloud Chat.

## 17\. Risks and Mitigations

### 17.1 Scope Creep

Risk:

The app becomes too ambitious.

Mitigation:

Keep Release 1 limited to authentication and basic direct messaging.

### 17.2 Security Rules Complexity

Risk:

Firestore rules become too permissive or too complex.

Mitigation:

Document and test rules early.

### 17.3 Cost Growth

Risk:

Firestore reads or analytics events grow unexpectedly.

Mitigation:

Use low-volume testing, budget alerts, and dashboard refresh discipline.

### 17.4 Branding Confusion

Risk:

Using `TestedChat` as a standalone name creates inconsistency with `testedcloud.com`.

Mitigation:

Use `TestedCloud Chat` as the product name under the TestedCloud ecosystem.

### 17.5 Google Play Requirements

Risk:

Publishing requires privacy, data safety, and testing track requirements.

Mitigation:

Treat Google Play as Release 4, not Release 1.

## 18\. Repository Structure

Recommended structure:

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
└── portfolio/
    └── index.html
```

## 19\. Implementation Plan

### Phase 0 — Planning

* Create documentation folder.
* Commit PRD.
* Create architecture document.
* Create data model document.
* Create security/privacy document.
* Create analytics events document.

### Phase 1 — Firebase Setup

* Create Firebase project or link existing Google Cloud project.
* Register Android app.
* Enable Firebase Authentication.
* Enable Firestore.
* Define initial Firestore rules.
* Store Firebase config safely in Android project.

### Phase 2 — Android App Skeleton

* Create Android project.
* Set package name.
* Configure Firebase SDK.
* Add sign-in screen.
* Add navigation structure.
* Add app theme and TestedCloud branding.

### Phase 3 — Messaging MVP

* Create user profile on first login.
* Create conversation list.
* Implement one-to-one chat screen.
* Send messages to Firestore.
* Read messages in real time.
* Add basic validation.

### Phase 4 — Analytics Pipeline

* Create event collector API.
* Publish events to Pub/Sub.
* Process events into BigQuery.
* Build Looker Studio dashboard.
* Add monitoring and evidence.

### Phase 5 — Portfolio Integration

* Add TestedCloud Chat section to public landing page.
* Add links to documentation.
* Add screenshots or architecture visuals.
* Add demo narrative.

### Phase 6 — Play Store Preparation

* Create privacy policy page.
* Prepare app icon.
* Prepare screenshots.
* Prepare data safety notes.
* Set up closed testing.

## 20\. Open Questions

* Will the MVP use email/password or Google sign-in?
* Will the Firebase project be inside the existing Google Cloud project or a separate project?
* Will analytics use Firebase Analytics export or the custom TestedCloud Pub/Sub pipeline?
* Will the first chat be single-user demo mode or two real authenticated users?
* Should `chat.testedcloud.com` be reserved now or later?
* Should the app be public closed testing only or internal testing first?
* Should AI-assisted message suggestions be included in a later release?

## 21\. Recommended Immediate Next Steps

Recommended next steps:

```text
1. Create docs/testedcloud-chat/ folder.
2. Commit this PRD.
3. Create architecture.md.
4. Create data-model.md.
5. Create security-privacy.md.
6. Decide Firebase project strategy.
7. Start Android app skeleton.
```

## 22\. Final Positioning

TestedCloud Chat should be positioned as:

> A secure, cloud-backed messaging demo within the TestedCloud ecosystem, built to demonstrate Firebase Authentication, Firestore real-time messaging, Google Cloud event analytics, IAM discipline, observability, and production-style application modernization documentation.

This project complements the existing TestedCloud hybrid cloud lab and strengthens the overall portfolio by showing both infrastructure modernization and application modernization.

