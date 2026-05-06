# TestedCloud Chat — Firebase Setup

## 1\. Purpose

This document defines the Firebase setup plan for TestedCloud Chat.

The goal is to prepare Firebase as the application foundation for the Android messaging MVP, including Firebase project alignment, Android app registration, Firebase Authentication, Cloud Firestore, initial security rules, local configuration handling, and validation evidence.

This document should be completed before writing the main Android application code.

## 2\. Scope

This setup covers:

* Firebase project strategy
* Android app registration
* Firebase Authentication
* Cloud Firestore
* Firestore security rules draft
* Firebase client configuration handling
* Local development notes
* Evidence collection
* Repository structure
* Initial validation commands/checks

Out of scope for this setup phase:

* Full Android app implementation
* Cloud Run analytics event collector
* Pub/Sub analytics pipeline
* BigQuery chat analytics
* Google Play release setup
* AI functionality
* Push notifications

## 3\. Recommended Project Strategy

Recommended approach:

```text
Use the existing Google Cloud project:
majestic-layout-255620
```

Reason:

* The existing TestedCloud platform already uses this project.
* Pub/Sub, Cloud Run, BigQuery, IAM, monitoring, and budget alerts are already established.
* It keeps the portfolio story centralized.
* It makes it easier to connect TestedCloud Chat analytics to the existing TestedCloud architecture.

Alternative:

```text
Create a separate Firebase/GCP project for TestedCloud Chat
```

This is cleaner for isolation, but adds more operational overhead.

Recommended MVP decision:

```text
Use majestic-layout-255620 for the initial MVP.
```

## 4\. Firebase Console Setup

Go to:

```text
https://console.firebase.google.com/
```

Recommended path:

```text
Add project
    |
    v
Select existing Google Cloud project
    |
    v
majestic-layout-255620
```

If Firebase is already enabled for the project, open the existing Firebase project instead.

## 5\. Firebase Project Naming

Recommended Firebase display name:

```text
TestedCloud
```

or:

```text
TestedCloud Lab
```

If Firebase requires a project nickname for the app, use:

```text
TestedCloud Chat Android
```

## 6\. Android App Registration

In Firebase Console:

```text
Project Overview
    |
    v
Add app
    |
    v
Android
```

Recommended Android package name:

```text
com.testedcloud.chat
```

Recommended app nickname:

```text
TestedCloud Chat
```

Debug signing certificate SHA-1:

```text
Optional for email/password MVP
Required later for Google sign-in
```

For MVP email/password authentication, SHA-1 can be added later.

## 7\. Firebase Configuration File

After registering the Android app, Firebase provides:

```text
google-services.json
```

This file is normally placed in:

```text
apps/testedcloud-chat/app/google-services.json
```

## 8\. Handling google-services.json

Important distinction:

`google-services.json` is not a service account private key. It contains client configuration for Firebase SDK usage.

However, because the repository may be public, handle it intentionally.

Recommended MVP approach:

```text
Do not commit the real google-services.json initially.
Commit a sanitized example file instead.
```

Recommended files:

```text
apps/testedcloud-chat/app/google-services.example.json
apps/testedcloud-chat/app/google-services.json
```

`.gitignore` should include:

```text
google-services.json
```

The real local file should stay untracked.

## 9\. Repository Structure

Recommended folders:

```text
testedcloud-lab/
├── apps/
│   └── testedcloud-chat/
│       └── app/
│           ├── google-services.example.json
│           └── google-services.json
├── firebase/
│   └── testedcloud-chat/
│       ├── firestore.rules
│       ├── firestore.indexes.json
│       └── README.md
└── docs/
    └── testedcloud-chat/
        ├── product-requirements.md
        ├── architecture.md
        ├── data-model.md
        ├── security-privacy.md
        ├── analytics-events.md
        ├── roadmap.md
        └── firebase-setup.md
```

## 10\. Update .gitignore

Add:

```text
# Firebase local config
google-services.json
GoogleService-Info.plist

# Android local files
local.properties
\*.jks
\*.keystore

# Firebase emulator / local cache
.firebase/
firebase-debug.log
firestore-debug.log
ui-debug.log
```

Before committing, validate:

```bash
git check-ignore -v apps/testedcloud-chat/app/google-services.json
```

Expected:

```text
.gitignore:...:google-services.json apps/testedcloud-chat/app/google-services.json
```

## 11\. Firebase Authentication Setup

In Firebase Console:

```text
Build
    |
    v
Authentication
    |
    v
Get started
```

Enable MVP sign-in provider:

```text
Email/Password
```

Recommended initial configuration:

```text
Email/Password: Enabled
Email link sign-in: Disabled for MVP
```

Future provider:

```text
Google
```

Google sign-in should be added after the MVP authentication and Firestore rules are validated.

## 12\. Firebase Authentication MVP Behavior

Required MVP behavior:

* User can create account.
* User can sign in.
* User can sign out.
* User UID is available to the app.
* App creates/loads `users/{uid}` profile.
* Firestore rules use UID for authorization.

## 13\. Cloud Firestore Setup

In Firebase Console:

```text
Build
    |
    v
Firestore Database
    |
    v
Create database
```

Recommended mode:

```text
Production mode
```

Reason:

This starts with secure defaults and requires explicit rules.

Recommended location:

Use a region compatible with the project and low-cost operation.

Suggested location if prompted:

```text
nam5
```

or an available US multi-region/region appropriate to the existing GCP project.

Important:

Firestore location selection can be permanent for the database. Choose carefully.

## 14\. Initial Firestore Collections

Initial collections:

```text
users
conversations
```

Messages are stored as subcollections:

```text
conversations/{conversationId}/messages
```

Do not manually create all documents unless needed for testing. Firestore collections can be created when the app writes data.

## 15\. Initial Firestore Rules

Create file:

```text
firebase/testedcloud-chat/firestore.rules
```

Initial draft:

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

    function conversationDoc(conversationId) {
      return get(/databases/$(database)/documents/conversations/$(conversationId));
    }

    function isConversationParticipant(conversationId) {
      return isSignedIn()
        \&\& request.auth.uid in conversationDoc(conversationId).data.participantIds;
    }

    match /users/{userId} {
      allow read: if isSelf(userId);

      allow create: if isSelf(userId)
        \&\& request.resource.data.uid == request.auth.uid;

      allow update: if isSelf(userId)
        \&\& request.resource.data.uid == resource.data.uid
        \&\& request.resource.data.email == resource.data.email;

      allow delete: if false;
    }

    match /conversations/{conversationId} {
      allow read: if isSignedIn()
        \&\& request.auth.uid in resource.data.participantIds;

      allow create: if isSignedIn()
        \&\& request.auth.uid in request.resource.data.participantIds
        \&\& request.resource.data.type == "direct"
        \&\& request.resource.data.participantCount == 2
        \&\& request.resource.data.participantIds.size() == 2;

      allow update: if isSignedIn()
        \&\& request.auth.uid in resource.data.participantIds
        \&\& request.resource.data.participantIds == resource.data.participantIds
        \&\& request.resource.data.createdBy == resource.data.createdBy
        \&\& request.resource.data.type == resource.data.type;

      allow delete: if false;

      match /messages/{messageId} {
        allow read: if isConversationParticipant(conversationId);

        allow create: if isConversationParticipant(conversationId)
          \&\& request.resource.data.senderId == request.auth.uid
          \&\& request.resource.data.type == "text"
          \&\& request.resource.data.text is string
          \&\& request.resource.data.text.size() > 0
          \&\& request.resource.data.text.size() <= 1000;

        allow update: if false;
        allow delete: if false;
      }
    }
  }
}
```

Important:

These are initial MVP rules. They must be tested and refined during implementation.

## 16\. Firestore Indexes

Create file:

```text
firebase/testedcloud-chat/firestore.indexes.json
```

Initial draft:

```json
{
  "indexes": \[
    {
      "collectionGroup": "conversations",
      "queryScope": "COLLECTION",
      "fields": \[
        {
          "fieldPath": "participantIds",
          "arrayConfig": "CONTAINS"
        },
        {
          "fieldPath": "updatedAt",
          "order": "DESCENDING"
        }
      ]
    }
  ],
  "fieldOverrides": \[]
}
```

Firestore may also generate index creation links automatically when queries are run.

## 17\. Firebase CLI Setup

Install Firebase CLI if needed:

```bash
npm install -g firebase-tools
```

Validate:

```bash
firebase --version
```

Login:

```bash
firebase login
```

Initialize Firebase files only if needed:

```bash
firebase init firestore
```

When prompted:

```text
Use existing project: majestic-layout-255620
Firestore rules file: firebase/testedcloud-chat/firestore.rules
Firestore indexes file: firebase/testedcloud-chat/firestore.indexes.json
```

If the CLI does not naturally support nested rules path during init, initialize normally and then move/update the files.

## 18\. Deploy Firestore Rules

Deploy rules:

```bash
firebase deploy --only firestore:rules --project majestic-layout-255620
```

Deploy indexes:

```bash
firebase deploy --only firestore:indexes --project majestic-layout-255620
```

If indexes are not ready yet, deploy only rules first.

## 19\. Android Project Integration

When the Android app is created, typical Firebase integration requires:

Project-level Gradle:

```text
com.google.gms.google-services
```

App-level plugin:

```text
id("com.google.gms.google-services")
```

Dependencies will likely include:

```text
Firebase Auth
Cloud Firestore
Firebase BOM
```

Implementation details should be added later in the Android skeleton phase.

## 20\. Evidence Collection

Create evidence folder:

```bash
mkdir -p docs/evidence/testedcloud-chat
```

Recommended evidence files:

```text
docs/evidence/testedcloud-chat/firebase-project-validation.txt
docs/evidence/testedcloud-chat/firebase-auth-config.txt
docs/evidence/testedcloud-chat/firestore-config.txt
docs/evidence/testedcloud-chat/firestore-rules-deploy.txt
docs/evidence/testedcloud-chat/firebase-config-sanitization.txt
```

## 21\. Evidence: Firebase Project Validation

Suggested content:

```text
TestedCloud Chat Firebase Project Validation

Google Cloud project: majestic-layout-255620
Firebase enabled: yes
Android app registered: yes
Android package: com.testedcloud.chat
Authentication enabled: Email/Password
Firestore enabled: yes
Firestore mode: Production
Status: Validated / Pending
```

## 22\. Evidence: Firebase Config Sanitization

Suggested command:

```bash
git status
git check-ignore -v apps/testedcloud-chat/app/google-services.json || true
git ls-files | grep -E "google-services.json|serviceAccount|private\_key|client\_secret" || true
```

Expected:

```text
Real google-services.json is ignored or not committed.
No service account keys are tracked.
No private keys are tracked.
```

## 23\. Security Validation Checklist

Before implementation continues:

|Check|Expected|
|-|-|
|Firebase Auth enabled|Yes|
|Firestore created in production mode|Yes|
|Firestore rules deny unauthenticated access|Yes|
|Real `google-services.json` not committed|Yes|
|No service account private key committed|Yes|
|`.gitignore` updated|Yes|
|Firebase setup evidence captured|Yes|

## 24\. Cost Considerations

Expected Firebase MVP costs should remain low.

Cost controls:

* Low number of test users.
* Low message volume.
* Avoid automated high-frequency writes.
* Avoid excessive Firestore listeners.
* Limit message query size.
* Use pagination.
* Continue using GCP budget alerts.

Potential cost risks:

* Unbounded Firestore reads.
* Large real-time listeners.
* Excessive test event generation.
* Automated loops writing messages or analytics events.

## 25\. Firebase App Check

Firebase App Check is recommended later, but not required for the first MVP setup.

MVP status:

```text
Future hardening
```

Recommended before broader testing:

```text
Enable Firebase App Check for Android
```

## 26\. Google Sign-In Future Setup

Google sign-in will require:

* SHA-1 or SHA-256 certificate fingerprint
* Google provider enabled in Firebase Auth
* Android sign-in integration
* Testing on emulator and physical device

Recommended timeline:

```text
After email/password MVP is working
```

## 27\. Open Questions

Open setup questions:

* Should Firebase be enabled in the existing `majestic-layout-255620` project or a new dedicated project?
* Which Firestore location should be selected?
* Should the initial app use email/password only?
* Should `google-services.json` remain ignored permanently?
* Should Firebase App Check be enabled before external testing?
* Should Google sign-in be part of MVP or Release 2?
* Should the repository remain public while Android Firebase config is added?

## 28\. Recommended Immediate Steps

Recommended next actions:

```text
1. Commit this Firebase setup document.
2. Update .gitignore for Firebase/Android local config.
3. Create firebase/testedcloud-chat/ folder.
4. Create firestore.rules.
5. Create firestore.indexes.json.
6. Enable Firebase in project majestic-layout-255620.
7. Register Android app com.testedcloud.chat.
8. Download google-services.json locally.
9. Confirm google-services.json is not committed.
10. Enable Firebase Authentication email/password.
11. Create Firestore database in production mode.
12. Deploy or configure initial rules.
13. Capture evidence.
```

## 29\. Recommended Commit Sequence

```text
Add TestedCloud Chat Firebase setup guide
Add Firebase and Android local config ignores
Add initial TestedCloud Chat Firestore rules
Add initial TestedCloud Chat Firestore indexes
Add Firebase setup evidence
```

## 30\. Final Positioning

Firebase setup is the first implementation step for TestedCloud Chat.

It establishes:

* identity foundation
* real-time database foundation
* initial access control model
* local configuration handling
* evidence-driven setup process

Once this is complete, the next major step is:

```text
Create Android app skeleton
```

