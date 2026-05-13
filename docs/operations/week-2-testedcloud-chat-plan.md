# Week 2 Plan — TestedCloud Chat Core Stabilization

Date range: May 18 – May 24, 2026  
Focus: TestedCloud Chat conversation model, requester visibility, Firestore rules, and analytics preparation

## Objective

Stabilize TestedCloud Chat before connecting it more deeply into the TestedCloud analytics and AI roadmap.

## Main Goal

Fix conversation handling and prepare the app for production-style event tracking.

## Current Context

TestedCloud Chat already has:

- Firebase setup
- Firebase Authentication
- Firestore integration
- Basic direct conversations
- Message UI
- Conversation polish
- Firebase setup validation evidence
- Firestore rules deployment evidence

Known issue:

- Some older conversations may not align with the current user/conversation identifier model.
- Starting a conversation with an existing user can reopen an older conversation depending on how participants were originally modeled.
- A user-specific hide/cleanup feature is needed.

## Target Design Direction

Use a user-specific visibility field:

    hiddenForUsers: ["uid_1", "uid_2"]

This allows one user to hide a conversation without deleting it for the other participant.

## Week 2 Checklist

### Day 1 — Firestore Model Review

- [ ] Review current conversation document structure
- [ ] Review current message document structure
- [ ] Review participant fields
- [ ] Review requester/current user UID logic
- [ ] Identify old/new conversation mismatch
- [ ] Document current data model

### Day 2 — Hide Conversation Design

- [ ] Define `hiddenForUsers` field
- [ ] Define hide behavior
- [ ] Define visibility query behavior
- [ ] Define edge cases
- [ ] Update data model documentation

### Day 3 — Implementation

- [ ] Add hide conversation action
- [ ] Update Firestore document with requester UID
- [ ] Filter hidden conversations from requester view
- [ ] Preserve visibility for other participant
- [ ] Avoid deleting shared message history

### Day 4 — Firestore Rules Review

- [ ] Review current Firestore rules
- [ ] Confirm users can only update allowed conversation fields
- [ ] Prevent users from hiding conversations for other users
- [ ] Validate requester-only logic
- [ ] Deploy rules if needed

### Day 5 — Validation Evidence

- [ ] Test hide conversation as requester
- [ ] Confirm other participant still sees conversation
- [ ] Confirm hidden conversation does not appear for requester
- [ ] Capture screenshots
- [ ] Save validation evidence

### Day 6 — Analytics Preparation

- [ ] Define `conversation_hidden` event
- [ ] Define `conversation_created` event
- [ ] Define `message_sent` event
- [ ] Prepare analytics event schema
- [ ] Document future Pub/Sub integration

### Day 7 — Weekly Closure

- [ ] Update TestedCloud Chat docs
- [ ] Update Week 2 evidence
- [ ] Push stable commit
- [ ] Confirm clean Git status
- [ ] Prepare Week 3 analytics pipeline plan

## Week 2 Exit Criteria

- [ ] Conversation model reviewed
- [ ] `hiddenForUsers` design documented
- [ ] Hide conversation feature implemented
- [ ] Requester visibility validated
- [ ] Firestore rules reviewed
- [ ] Evidence captured
- [ ] GitHub updated
