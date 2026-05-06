# TestedCloud Chat — Security and Privacy

## 1\. Purpose

This document defines the initial security and privacy model for TestedCloud Chat.

TestedCloud Chat is a Firebase-backed Android messaging application within the TestedCloud ecosystem. It uses Firebase Authentication, Firestore, and optional Google Cloud analytics services.

The goal of this document is to establish security principles before implementation, so the MVP is built with clear access control, privacy awareness, least privilege, and production-style documentation.

## 2\. Security Goals

Primary security goals:

* Require authenticated access to chat functionality.
* Prevent users from reading conversations they do not participate in.
* Prevent users from sending messages as another user.
* Prevent unauthenticated access to Firestore data.
* Keep chat data separate from analytics data.
* Avoid hardcoded secrets in source code.
* Avoid service account keys in the mobile app.
* Use least-privilege IAM for backend services.
* Validate event payloads before publishing to Pub/Sub.
* Document data collection and privacy expectations.

## 3\. Privacy Goals

Primary privacy goals:

* Collect only the minimum data needed for the MVP.
* Avoid sensitive personal data collection.
* Avoid contacts upload in the MVP.
* Avoid location tracking in the MVP.
* Avoid payment data.
* Avoid health, biometric, or highly sensitive data.
* Provide a privacy policy before any Google Play release.
* Be transparent about what data is collected.
* Keep analytics metadata minimal.
* Support future user data deletion planning.

## 4\. Scope

This document covers:

* Firebase Authentication
* Firestore access model
* Firestore security rules strategy
* Android client security considerations
* Cloud Run analytics API security
* Pub/Sub and BigQuery IAM model
* Privacy and data safety expectations
* Google Play readiness considerations
* Threat model
* Validation plan

Out of scope for MVP:

* End-to-end encryption
* Enterprise admin console
* Advanced moderation
* Full legal privacy review
* SOC 2 / ISO compliance
* Production commercial launch
* Public Google Play production release

## 5\. Security Architecture Overview

MVP security architecture:

```text
Android App
    |
    | Firebase Authentication
    v
Firebase Auth UID
    |
    v
Firestore Security Rules
    |
    v
Users / Conversations / Messages
```

Extended analytics security architecture:

```text
Android App
    |
    | HTTPS event request
    v
Cloud Run Event Collector
    |
    | Validates event
    v
Pub/Sub Topic
    |
    v
Cloud Run Consumer
    |
    | Dedicated service account
    v
BigQuery Dataset
```

## 6\. Identity Model

Firebase Authentication is the source of user identity.

Primary identity:

```text
request.auth.uid
```

Firestore user profile path:

```text
users/{userId}
```

The `userId` must match:

```text
request.auth.uid
```

Identity principles:

* Authentication is required before accessing conversations.
* UID is the main authorization anchor.
* Email is not used as the primary authorization key.
* Users cannot claim another UID.
* User identity should be validated server-side where backend APIs are used.

## 7\. Authentication Requirements

MVP authentication requirements:

|Requirement|Status|
|-|-|
|User must sign in before accessing chat|Required|
|User profile created after first sign-in|Required|
|Sign-out supported|Required|
|Anonymous public chat access|Not allowed|
|Service account credentials in app|Not allowed|

Recommended MVP sign-in method:

```text
Email/password
```

Future sign-in method:

```text
Google sign-in
```

Reason:

Email/password is easier for early testing. Google sign-in can be added after the Firestore data model and rules are validated.

## 8\. Firestore Access Model

Firestore collections:

```text
users/{userId}
conversations/{conversationId}
conversations/{conversationId}/messages/{messageId}
```

Access model:

|Resource|Access Rule|
|-|-|
|`users/{userId}`|User can read/update limited fields on their own profile|
|`conversations/{conversationId}`|User can read only if UID is in `participantIds`|
|`messages/{messageId}`|User can read/write only if participant in parent conversation|
|Analytics data|Not stored in Firestore MVP chat collections|

## 9\. Firestore Security Principles

Rules should follow these principles:

* Deny by default.
* Allow only authenticated users.
* Use UID-based checks.
* Validate participant membership.
* Validate sender identity.
* Prevent privilege escalation.
* Keep rules readable.
* Add tests for key allow/deny cases.

Core checks:

```text
request.auth != null
request.auth.uid == userId
request.auth.uid in resource.data.participantIds
request.resource.data.senderId == request.auth.uid
```

## 10\. User Profile Security

Path:

```text
users/{userId}
```

Required rules:

* User can create their own profile.
* User can read their own profile.
* User can update limited fields in their own profile.
* User cannot update `uid` to another value.
* User cannot modify privileged fields.
* User cannot write another user's profile.

Allowed user-editable fields for MVP:

```text
displayName
photoUrl
updatedAt
lastLoginAt
```

Restricted fields:

```text
uid
email
createdAt
status
roles
isAdmin
```

MVP should not include admin roles.

## 11\. Conversation Security

Path:

```text
conversations/{conversationId}
```

Required rules:

* User can read a conversation only if their UID is in `participantIds`.
* User can create a direct conversation only if their UID is included in `participantIds`.
* MVP direct conversations should have exactly two participants.
* User cannot create a conversation for other users without including themselves.
* User cannot modify `createdBy` after creation.
* User cannot change conversation type arbitrarily.

Required direct conversation constraints:

```text
type == "direct"
participantCount == 2
participantIds.size() == 2
request.auth.uid in participantIds
```

## 12\. Message Security

Path:

```text
conversations/{conversationId}/messages/{messageId}
```

Required rules:

* User can read messages only if they are a participant in the parent conversation.
* User can create messages only if they are a participant.
* `senderId` must equal `request.auth.uid`.
* Message type must be `text` for MVP.
* Message text must not be empty.
* Message text should have a maximum length.
* User cannot write a message into a conversation they do not belong to.

Recommended MVP message limit:

```text
1 to 1000 characters
```

## 13\. Draft Firestore Rule Logic

This is conceptual rule logic, not the final deployable file.

```text
rules\_version = '2';

service cloud.firestore {
  match /databases/{database}/documents {

    function isSignedIn() {
      return request.auth != null;
    }

    function isSelf(userId) {
      return isSignedIn() \&\& request.auth.uid == userId;
    }

    function isConversationParticipant(conversationId) {
      return isSignedIn()
        \&\& request.auth.uid in get(/databases/$(database)/documents/conversations/$(conversationId)).data.participantIds;
    }

    match /users/{userId} {
      allow read: if isSelf(userId);
      allow create: if isSelf(userId)
        \&\& request.resource.data.uid == request.auth.uid;
      allow update: if isSelf(userId);
      allow delete: if false;
    }

    match /conversations/{conversationId} {
      allow read: if isSignedIn()
        \&\& request.auth.uid in resource.data.participantIds;

      allow create: if isSignedIn()
        \&\& request.auth.uid in request.resource.data.participantIds
        \&\& request.resource.data.type == "direct"
        \&\& request.resource.data.participantCount == 2;

      allow update: if isSignedIn()
        \&\& request.auth.uid in resource.data.participantIds;

      allow delete: if false;

      match /messages/{messageId} {
        allow read: if isConversationParticipant(conversationId);

        allow create: if isConversationParticipant(conversationId)
          \&\& request.resource.data.senderId == request.auth.uid
          \&\& request.resource.data.type == "text";

        allow update: if false;
        allow delete: if false;
      }
    }
  }
}
```

Important:

Final rules should be tested before use and may need refinement for server timestamps, field validation, and immutable fields.

## 14\. Android Client Security

The Android app should not contain:

* Service account keys
* Cloud Run private secrets
* OAuth client secrets intended for backend use
* Admin credentials
* Billing information
* Notification channel IDs
* Private API keys intended to remain confidential

Android client may contain Firebase client configuration, but it should be handled intentionally.

Important distinction:

Firebase client configuration is not the same as a service account private key. However, exposing the config means Firestore rules and authentication must be correct because client config alone does not secure data.

## 15\. Backend API Security

If Cloud Run analytics API is used, it should:

* Accept only expected event payloads.
* Validate required fields.
* Validate event type against an allowlist.
* Reject malformed payloads.
* Avoid logging sensitive message text if not needed.
* Add server-side `processed\_at`.
* Publish only validated events to Pub/Sub.
* Use a dedicated service account.

Recommended endpoint:

```text
POST /events
```

Recommended validation:

```text
event\_type must be known
source must be testedcloud-chat-android
created\_at must be present
metadata must be limited
payload size must be limited
```

## 16\. Analytics Privacy

Analytics should avoid storing full message text.

Recommended event for `message\_sent`:

```json
{
  "event\_type": "message\_sent",
  "user\_id": "uid\_123",
  "conversation\_id": "conversation\_456",
  "message\_id": "message\_789",
  "metadata": {
    "message\_length": 42,
    "platform": "android",
    "app\_version": "0.1.0"
  }
}
```

Do not include:

```text
message\_text
recipient\_email
precise\_location
contacts
sensitive personal data
```

## 17\. Pub/Sub Security

Topic:

```text
testedcloud-chat-events
```

Recommended access:

|Principal|Role|Scope|
|-|-|-|
|`testedcloud-chat-api-sa`|Pub/Sub Publisher|Topic-level|
|`testedcloud-chat-consumer-sa`|Pub/Sub Subscriber|Subscription-level if pull, or Cloud Run invoker if push pattern requires it|

Avoid:

* Project-level Editor
* Default Compute Engine service account
* Unnecessary broad Pub/Sub Admin
* Public unauthenticated publishing

## 18\. BigQuery Security

Dataset:

```text
testedcloud\_chat
```

Table:

```text
events
```

Recommended access:

|Principal|Role|Scope|
|-|-|-|
|`testedcloud-chat-consumer-sa`|BigQuery Data Editor|Dataset-level|
|Dashboard viewer|BigQuery Data Viewer|Dataset/view-level|
|Dashboard job runner|BigQuery Job User|Project-level if needed|

Avoid storing sensitive chat message content in analytics tables.

## 19\. IAM Principles

IAM model should follow:

* Dedicated service accounts per workload.
* Least privilege.
* Dataset-level roles where practical.
* Topic-level Pub/Sub roles where practical.
* No broad default service account permissions.
* No service account keys unless absolutely required.
* Prefer workload identity and managed service identities.

Proposed service accounts:

```text
testedcloud-chat-api-sa
testedcloud-chat-consumer-sa
```

## 20\. Privacy Data Inventory

Potential data collected in MVP:

|Data|Purpose|Required?|
|-|-|-|
|Firebase UID|Identity and authorization|Yes|
|Email|Sign-in and profile|Yes, depending on provider|
|Display name|Chat identification|Yes|
|Photo URL|Optional profile display|No|
|Message text|Core chat function|Yes|
|Conversation IDs|Chat organization|Yes|
|Timestamps|Ordering and audit context|Yes|
|Analytics event type|Usage analytics|Yes|
|Message length|Analytics metadata|Optional|
|App version|Debugging and analytics|Optional|

Data not collected in MVP:

```text
contacts
precise location
payment data
health data
biometrics
voice recordings
video recordings
advertising identifiers
```

## 21\. Data Minimization

MVP should minimize data collection:

* Use UID instead of storing extra identifiers.
* Store only display name and email required for sign-in/profile.
* Do not store message text in analytics events.
* Avoid unnecessary metadata.
* Avoid contacts import.
* Avoid device-level identifiers unless needed for debugging.
* Avoid location collection.

## 22\. Data Retention

MVP retention:

```text
Chat messages: retained during testing
User profiles: retained during testing
Analytics events: retained during testing
```

Future retention features:

* User account deletion
* Message deletion
* Conversation deletion
* Analytics retention policy
* Export user data
* Delete user data

## 23\. Google Play Data Safety Considerations

Before Google Play release, prepare answers for:

* What data is collected?
* Why is data collected?
* Is data shared?
* Is data encrypted in transit?
* Can users request deletion?
* Is data optional or required?
* Is the app intended for children?
* Does the app contain ads?
* Does the app use analytics?

Likely MVP data categories:

```text
Personal info: email, name
Messages: message text
App activity: app interactions
Diagnostics: crash logs if enabled later
```

Likely security statements:

```text
Data is encrypted in transit
Authentication is required
Data access is restricted by Firestore security rules
```

## 24\. Privacy Policy Requirements

Before external testing, create:

```text
https://testedcloud.com/privacy
```

Privacy policy should explain:

* What TestedCloud Chat is
* What data is collected
* Why data is collected
* How data is used
* Whether data is shared
* How long data is retained
* How users can request deletion
* Contact email
* Effective date

For MVP portfolio testing, the policy can be simple but clear.

## 25\. Threat Model

## 25.1 Threat: Unauthorized Conversation Read

Risk:

A user reads a conversation they do not belong to.

Mitigation:

* Firestore rules check `participantIds`.
* App queries only user conversations.
* Test deny cases.

## 25.2 Threat: Sender Spoofing

Risk:

A user sends a message with another user's `senderId`.

Mitigation:

* Firestore rules require `senderId == request.auth.uid`.

## 25.3 Threat: Unauthenticated Access

Risk:

Unauthenticated users read or write chat data.

Mitigation:

* All Firestore reads/writes require `request.auth != null`.

## 25.4 Threat: Excessive Message Writes

Risk:

A user spams messages.

Mitigation for MVP:

* Message length validation.
* Future rate limiting.
* Future App Check.
* Future Cloud Functions or backend moderation.

## 25.5 Threat: Analytics Event Spam

Risk:

Public event collector receives fake events.

Mitigation:

* Validate Firebase ID token in Cloud Run event collector.
* Restrict accepted event types.
* Add rate limiting later.
* Monitor event volume.
* Avoid trusting analytics events for authorization.

## 25.6 Threat: Secret Exposure in GitHub

Risk:

Sensitive files are committed.

Mitigation:

* `.gitignore`
* Secret scanning
* Avoid service account keys
* Use sanitized examples
* Review before commit

## 26\. Firebase App Check

Firebase App Check is recommended for future hardening.

MVP status:

```text
Optional / future enhancement
```

Purpose:

* Reduce abuse from non-legitimate clients.
* Add a layer of protection for Firebase resources.
* Improve confidence before public testing.

## 27\. Encryption

Firebase and Google Cloud services provide encryption in transit and at rest by default.

MVP does not include end-to-end encryption.

Important distinction:

```text
Transport/service encryption != end-to-end encryption
```

If this becomes a real messaging product, end-to-end encryption would need a separate design.

## 28\. Logging Guidelines

Do log:

```text
event\_id
event\_type
request status
processing status
error category
service name
timestamp
```

Do not log:

```text
message text
passwords
auth tokens
refresh tokens
private keys
full ID tokens
sensitive personal information
```

## 29\. Monitoring and Alerts

Recommended future alerts:

```text
Cloud Run 5xx errors for chat event collector
Cloud Run 5xx errors for chat consumer
Pub/Sub backlog for testedcloud-chat-consumer-sub
DLQ messages for chat analytics events
BigQuery insert failures
Unexpected event volume spike
Firestore permission-denied spike
```

## 30\. Evidence Plan

Recommended evidence folder:

```text
docs/evidence/testedcloud-chat/
```

Recommended evidence files:

```text
firebase-auth-validation.txt
firestore-rules-validation.txt
firestore-deny-unauthorized-read.txt
message-send-validation.txt
message-sender-spoof-denied.txt
chat-event-api-validation.txt
chat-pubsub-validation.txt
chat-bigquery-validation.txt
chat-monitoring-alerts-validation.txt
```

## 31\. Security Validation Tests

MVP security tests:

|Test|Expected Result|
|-|-|
|Unauthenticated user reads conversations|Denied|
|User reads own conversation|Allowed|
|User reads unrelated conversation|Denied|
|User sends message as self|Allowed|
|User sends message with different senderId|Denied|
|User creates direct conversation including self|Allowed|
|User creates conversation excluding self|Denied|
|User sends empty message|Denied|
|User sends oversized message|Denied|
|User updates restricted profile fields|Denied|

## 32\. Privacy Validation Tests

Privacy checks:

|Check|Expected Result|
|-|-|
|No contacts collection|Confirmed|
|No location permission|Confirmed|
|No payment data|Confirmed|
|Analytics event excludes message text|Confirmed|
|Privacy policy exists before external testing|Required|
|Support/contact URL exists before Play testing|Required|

## 33\. Open Security Questions

Open questions:

* Will MVP use email/password or Google sign-in?
* Will Firebase App Check be enabled before closed testing?
* Will analytics API require Firebase ID token validation from day one?
* Should message delete be supported in Release 1?
* Should user account deletion be implemented before Google Play closed testing?
* Should logs include hashed user IDs instead of raw UID?
* Should BigQuery analytics store pseudonymized user IDs?
* Should chat data and analytics use separate Firebase/GCP projects?

## 34\. Recommended Immediate Next Steps

Recommended sequence:

```text
1. Commit this security and privacy document.
2. Create analytics-events.md.
3. Draft initial Firestore rules.
4. Create Firebase project/app.
5. Implement authentication.
6. Implement Firestore data model.
7. Validate security rules.
8. Capture evidence.
```

## 35\. Final Positioning

TestedCloud Chat should be positioned as a secure application modernization demo.

Security story:

> TestedCloud Chat uses Firebase Authentication and Firestore security rules to enforce participant-based chat access, while backend analytics uses Cloud Run, Pub/Sub, BigQuery, and least-privilege service accounts. The MVP is designed with privacy awareness, minimal data collection, and production-style security documentation.

Portfolio value:

* Shows secure Firebase architecture.
* Shows participant-based authorization.
* Shows privacy/data safety awareness.
* Shows backend analytics security.
* Shows least-privilege IAM thinking.
* Shows a realistic path toward Google Play readiness.

