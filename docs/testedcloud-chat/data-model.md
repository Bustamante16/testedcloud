# TestedCloud Chat — Data Model

## 1\. Purpose

This document defines the initial data model for TestedCloud Chat.

The model is designed for a secure, cloud-backed Android messaging MVP using:

* Firebase Authentication
* Cloud Firestore
* Cloud Run analytics event collector
* Pub/Sub
* BigQuery
* Looker Studio

The goal is to keep the MVP simple while creating a data structure that can grow into analytics, dashboards, notifications, and future AI-assisted features.

## 2\. Design Principles

The data model follows these principles:

* Keep chat data and analytics data separate.
* Use Firebase Authentication UID as the primary user identity.
* Use participant-based access control for conversations.
* Avoid unnecessary personal data.
* Keep Firestore document structures simple and readable.
* Avoid large unbounded documents.
* Store messages as subcollections under conversations.
* Design analytics events for BigQuery and dashboarding.
* Do not rely on client-provided data for sensitive authorization decisions.
* Keep the MVP compatible with future features such as read receipts, notifications, and AI assistance.

## 3\. Main Entities

Core Firestore entities:

|Entity|Firestore Path|Purpose|
|-|-|-|
|User|`users/{userId}`|Stores basic user profile metadata|
|Conversation|`conversations/{conversationId}`|Stores conversation metadata|
|Message|`conversations/{conversationId}/messages/{messageId}`|Stores messages inside a conversation|

Analytics entities:

|Entity|Destination|Purpose|
|-|-|-|
|Chat Event|Cloud Run / Pub/Sub / BigQuery|Tracks product and usage events|
|Event Metadata|BigQuery JSON or STRING|Stores optional event context|

## 4\. Firestore Collection: users

### 4.1 Path

```text
users/{userId}
```

The `userId` should match the Firebase Authentication UID.

### 4.2 Purpose

The `users` collection stores minimal profile information needed for the chat MVP.

It should not store sensitive personal data beyond what is required for basic identity and display.

### 4.3 Document ID Strategy

Use Firebase Authentication UID as document ID:

```text
users/{auth.uid}
```

Example:

```text
users/uid\_123
```

### 4.4 Fields

|Field|Type|Required|Description|
|-|-|-|-|
|`uid`|string|Yes|Firebase Authentication UID|
|`displayName`|string|Yes|User display name|
|`email`|string|Yes|User email from auth provider|
|`photoUrl`|string / null|No|Optional profile image URL|
|`createdAt`|timestamp|Yes|User profile creation time|
|`lastLoginAt`|timestamp|Yes|Last known login time|
|`status`|string|Yes|User status, such as `active`|
|`updatedAt`|timestamp|No|Last profile update time|

### 4.5 Example Document

```json
{
  "uid": "uid\_123",
  "displayName": "Dario",
  "email": "user@example.com",
  "photoUrl": null,
  "createdAt": "2026-05-05T20:00:00Z",
  "lastLoginAt": "2026-05-05T21:00:00Z",
  "status": "active",
  "updatedAt": "2026-05-05T21:00:00Z"
}
```

### 4.6 Access Pattern

Common queries:

```text
Read current user profile
Create current user profile after first sign-in
Update current user's display name
```

### 4.7 Security Notes

* Users can read their own profile.
* Users can update limited fields in their own profile.
* Users should not be allowed to update `uid`, `email`, or privileged fields directly.
* Public profile discovery should not be included in MVP unless explicitly needed.

## 5\. Firestore Collection: conversations

### 5.1 Path

```text
conversations/{conversationId}
```

### 5.2 Purpose

The `conversations` collection stores metadata about chat conversations.

For the MVP, only one-to-one conversations are required.

### 5.3 Document ID Strategy

Recommended options:

Option A — Auto-generated Firestore ID:

```text
conversations/{autoId}
```

Option B — Deterministic ID for direct conversations:

```text
conversations/{uidA\_uidB}
```

Recommended MVP approach:

```text
Use Firestore auto-generated IDs first.
```

Reason:

Auto-generated IDs are simpler for MVP implementation. A future version can enforce deterministic IDs to prevent duplicate direct conversations.

### 5.4 Fields

|Field|Type|Required|Description|
|-|-|-|-|
|`conversationId`|string|Yes|Conversation document ID|
|`type`|string|Yes|Conversation type, initially `direct`|
|`participantIds`|array<string>|Yes|Firebase UIDs of participants|
|`participantCount`|number|Yes|Number of participants|
|`createdAt`|timestamp|Yes|Conversation creation time|
|`updatedAt`|timestamp|Yes|Last conversation update time|
|`lastMessageText`|string|No|Preview of last message|
|`lastMessageAt`|timestamp|No|Timestamp of last message|
|`lastMessageSenderId`|string|No|UID of user who sent last message|
|`createdBy`|string|Yes|UID of creator|
|`status`|string|Yes|`active`, `archived`, or future state|

### 5.5 Example Document

```json
{
  "conversationId": "conversation\_123",
  "type": "direct",
  "participantIds": \["uid\_123", "uid\_456"],
  "participantCount": 2,
  "createdAt": "2026-05-05T20:10:00Z",
  "updatedAt": "2026-05-05T20:15:00Z",
  "lastMessageText": "Hello from TestedCloud Chat",
  "lastMessageAt": "2026-05-05T20:15:00Z",
  "lastMessageSenderId": "uid\_123",
  "createdBy": "uid\_123",
  "status": "active"
}
```

### 5.6 Access Pattern

Common queries:

```text
List conversations where participantIds contains current UID
Order conversations by updatedAt descending
Open a conversation by conversationId
Create a new direct conversation
Update last message metadata after sending a message
```

Recommended query:

```text
conversations
  where participantIds array-contains auth.uid
  order by updatedAt desc
```

### 5.7 Security Notes

* User can read a conversation only if their UID is in `participantIds`.
* User can create a conversation only if their UID is included in `participantIds`.
* MVP should restrict direct conversations to two participants.
* Users should not be able to remove other participants in MVP.
* Users should not be able to change `createdBy`.

## 6\. Firestore Subcollection: messages

### 6.1 Path

```text
conversations/{conversationId}/messages/{messageId}
```

### 6.2 Purpose

The `messages` subcollection stores individual chat messages.

Messages are stored under their parent conversation to simplify participant-based access checks.

### 6.3 Document ID Strategy

Use Firestore auto-generated IDs:

```text
conversations/{conversationId}/messages/{autoId}
```

### 6.4 Fields

|Field|Type|Required|Description|
|-|-|-|-|
|`messageId`|string|Yes|Message document ID|
|`conversationId`|string|Yes|Parent conversation ID|
|`senderId`|string|Yes|Firebase UID of sender|
|`text`|string|Yes|Message body|
|`createdAt`|timestamp|Yes|Message creation time|
|`updatedAt`|timestamp / null|No|Message edit time, future use|
|`status`|string|Yes|`sent`, future: `delivered`, `read`, `deleted`|
|`type`|string|Yes|Message type, initially `text`|
|`deleted`|boolean|Yes|Soft delete flag|
|`metadata`|map|No|Future optional metadata|

### 6.5 Example Document

```json
{
  "messageId": "message\_123",
  "conversationId": "conversation\_123",
  "senderId": "uid\_123",
  "text": "Hello from TestedCloud Chat",
  "createdAt": "2026-05-05T20:15:00Z",
  "updatedAt": null,
  "status": "sent",
  "type": "text",
  "deleted": false,
  "metadata": {
    "client": "android",
    "appVersion": "0.1.0"
  }
}
```

### 6.6 Access Pattern

Common queries:

```text
Read latest messages in a conversation
Order messages by createdAt ascending
Send a new message
Paginate older messages
```

Recommended query:

```text
conversations/{conversationId}/messages
  order by createdAt asc
  limit 50
```

For older history:

```text
order by createdAt desc
limit 50
```

then reverse in the client if needed.

### 6.7 Security Notes

* User can read messages only if they are a participant in the parent conversation.
* User can create a message only if they are a participant.
* `senderId` must match `request.auth.uid`.
* User should not be able to create a message as another user.
* Message `text` should have a maximum length.
* Future moderation or filtering can be added later.

## 7\. Optional Future Collection: userConversationRefs

### 7.1 Purpose

For MVP, querying `conversations` with `array-contains` is enough.

A future optimization may add per-user conversation references:

```text
users/{userId}/conversationRefs/{conversationId}
```

This can improve performance and support per-user conversation state.

### 7.2 Possible Fields

|Field|Type|Description|
|-|-|-|
|`conversationId`|string|Linked conversation|
|`lastReadAt`|timestamp|Last time user read conversation|
|`unreadCount`|number|User-specific unread count|
|`pinned`|boolean|Pinned conversation|
|`muted`|boolean|Muted conversation|
|`archived`|boolean|Archived by user|

### 7.3 MVP Status

```text
Not required for Release 1
```

## 8\. Optional Future Collection: notifications

### 8.1 Purpose

A future release may add notification state.

Possible path:

```text
users/{userId}/notifications/{notificationId}
```

### 8.2 MVP Status

```text
Not required for Release 1
```

## 9\. Analytics Event Model

Analytics events should be separate from Firestore chat data.

Potential destination:

```text
BigQuery dataset: testedcloud\_chat
Table: events
```

### 9.1 Event Fields

|Field|Type|Required|Description|
|-|-|-|-|
|`event\_id`|string|Yes|Unique event ID|
|`event\_type`|string|Yes|Event name|
|`source`|string|Yes|Source application|
|`origin`|string|Yes|Origin system|
|`user\_id`|string|No|Firebase UID|
|`conversation\_id`|string|No|Conversation ID|
|`message\_id`|string|No|Message ID|
|`created\_at`|timestamp|Yes|Event creation time|
|`processed\_at`|timestamp|Yes|Server processing time|
|`metadata`|JSON / string|No|Additional context|

### 9.2 Example Event

```json
{
  "event\_id": "evt\_123",
  "event\_type": "message\_sent",
  "source": "testedcloud-chat-android",
  "origin": "firebase",
  "user\_id": "uid\_123",
  "conversation\_id": "conversation\_123",
  "message\_id": "message\_123",
  "created\_at": "2026-05-05T20:15:00Z",
  "processed\_at": "2026-05-05T20:15:03Z",
  "metadata": {
    "message\_length": 27,
    "platform": "android",
    "app\_version": "0.1.0"
  }
}
```

### 9.3 Required MVP Event Types

```text
app\_opened
user\_signed\_in
user\_signed\_out
conversation\_created
message\_sent
```

### 9.4 Future Event Types

```text
message\_read
profile\_updated
conversation\_archived
conversation\_deleted
notification\_received
notification\_opened
ai\_suggestion\_requested
ai\_suggestion\_accepted
```

## 10\. BigQuery Table: testedcloud\_chat.events

### 10.1 Dataset

```text
testedcloud\_chat
```

### 10.2 Table

```text
events
```

### 10.3 Suggested Schema

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
|`processed\_at`|TIMESTAMP|REQUIRED|
|`metadata`|JSON|NULLABLE|

If JSON is not preferred, use:

```text
metadata STRING
```

### 10.4 Suggested Partitioning

Future recommended partition:

```text
DATE(processed\_at)
```

### 10.5 Suggested Clustering

Future recommended clustering:

```text
event\_type
source
```

## 11\. Data Access Patterns

## 11.1 Sign-In Flow

```text
User signs in
    |
    v
Firebase Auth returns UID
    |
    v
App checks users/{uid}
    |
    v
If missing, app creates profile
    |
    v
App loads conversations
```

## 11.2 Conversation List Flow

```text
App queries conversations
where participantIds contains auth.uid
order by updatedAt desc
```

## 11.3 Send Message Flow

```text
User sends message
    |
    v
App writes message document
    |
    v
App updates conversation lastMessage fields
    |
    v
App emits message\_sent analytics event
```

## 11.4 Read Messages Flow

```text
User opens conversation
    |
    v
App verifies user has access through Firestore rules
    |
    v
App subscribes to messages ordered by createdAt
    |
    v
Messages update in real time
```

## 12\. Firestore Indexes

Likely required index:

```text
Collection: conversations
Fields:
participantIds ARRAY\_CONTAINS
updatedAt DESC
```

Firestore may prompt to create this index automatically when the query is first run.

Messages query:

```text
messages order by createdAt
```

Usually does not require a composite index unless additional filters are added.

## 13\. Validation Rules for MVP

Client-side validation:

|Field|Rule|
|-|-|
|`displayName`|1–60 characters|
|`message.text`|1–1000 characters|
|`conversation.participantIds`|Must include current user|
|`conversation.type`|Must be `direct`|
|`message.type`|Must be `text`|

Server/security validation:

|Field|Rule|
|-|-|
|`message.senderId`|Must equal `request.auth.uid`|
|`conversation.participantIds`|Must include `request.auth.uid`|
|`conversation.participantCount`|Must be 2 for direct MVP|
|`createdAt`|Should use server timestamp where possible|
|`updatedAt`|Should use server timestamp where possible|

## 14\. Data Retention

MVP retention model:

```text
Messages are retained indefinitely during MVP testing.
Analytics events are retained indefinitely during MVP testing.
```

Future retention options:

* User deletion workflow
* Conversation deletion workflow
* Soft-delete messages
* Export/delete user data
* Data retention policy for analytics

## 15\. Privacy Considerations

Potentially collected user data:

```text
email
display name
Firebase UID
message text
timestamps
analytics event metadata
```

Privacy controls:

* Do not collect unnecessary sensitive data.
* Do not collect contacts in MVP.
* Do not collect precise location.
* Do not collect payment data.
* Do not collect health data.
* Keep analytics event metadata minimal.
* Provide privacy policy before Google Play release.

## 16\. Security Considerations

Main risks:

* User reads unauthorized conversation.
* User writes message as another user.
* User edits participant list.
* User creates malformed message.
* User sends excessive messages.
* Public repo exposes config or secrets.

Mitigations:

* Firestore rules.
* Participant-based authorization.
* Sender ID validation.
* Message length limits.
* Rate limiting later if needed.
* Secret scanning before commits.
* Avoid service account keys in mobile app.

## 17\. Example Firestore Structure

```text
users/
  uid\_123/
    uid: "uid\_123"
    displayName: "Dario"
    email: "user@example.com"

conversations/
  conversation\_123/
    type: "direct"
    participantIds: \["uid\_123", "uid\_456"]
    updatedAt: timestamp
    lastMessageText: "Hello"
    messages/
      message\_abc/
        senderId: "uid\_123"
        text: "Hello"
        createdAt: timestamp
      message\_def/
        senderId: "uid\_456"
        text: "Hi"
        createdAt: timestamp
```

## 18\. MVP Data Model Decisions

### 18.1 Use Subcollections for Messages

Decision:

Store messages under conversations.

Reason:

This keeps messages logically grouped and supports conversation-based access checks.

### 18.2 Use UID as User Document ID

Decision:

Use Firebase Auth UID as `users/{userId}`.

Reason:

This simplifies profile lookup and security rule checks.

### 18.3 Use participantIds Array

Decision:

Store participants in `participantIds`.

Reason:

This supports simple conversation list queries using `array-contains`.

### 18.4 Store Last Message Preview in Conversation

Decision:

Store `lastMessageText`, `lastMessageAt`, and `lastMessageSenderId` in the conversation document.

Reason:

This avoids reading the message subcollection for every conversation list item.

### 18.5 Keep Analytics Separate From Chat Data

Decision:

Send product events to a separate analytics pipeline.

Reason:

Chat functionality should not depend on analytics, and analytics should be optimized for BigQuery/dashboard usage.

## 19\. Open Questions

Open design questions:

* Should direct conversation IDs be deterministic to prevent duplicates?
* Should user profile email be visible to other participants?
* Should message edits be allowed in MVP?
* Should soft delete be included in Release 1?
* Should read receipts be included in Release 1 or later?
* Should analytics events be sent directly from the app or through a backend?
* Should the app use Firebase Analytics export or the custom TestedCloud pipeline?
* Should usernames be unique?
* Should there be a test/demo user mode?

## 20\. Recommended Next Steps

Recommended next steps:

```text
1. Review and commit this data model.
2. Create security-privacy.md.
3. Draft Firestore security rules.
4. Create analytics-events.md.
5. Decide sign-in method for MVP.
6. Create Firebase project/app.
7. Implement users/conversations/messages collections.
```

## 21\. Final Positioning

This data model supports a simple but credible chat MVP.

It demonstrates:

* Firebase Authentication identity mapping
* Firestore real-time data modeling
* Participant-based access control
* Chat message organization
* Product analytics design
* BigQuery-ready event structure
* Privacy-aware minimal data collection

The model is intentionally simple for Release 1 but extensible for future features such as read receipts, notifications, AI assistance, and analytics dashboards.

