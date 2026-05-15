# Week 2 Status — TestedCloud Chat Core Stabilization

Date: May 2026
Focus: Conversation delete behavior, Firestore rules, Android validation, and data model stabilization

## Summary

Week 2 started with a review of the TestedCloud Chat conversation model and a bug related to deleting conversations.

The initial requirement was clarified:

A user pressing "Delete conversation" should remove the conversation only from that user's view. The conversation should not be physically deleted by the Android client.

During implementation and testing, the design evolved from a simple hidden/deleted flag into a timestamp-based user-scoped delete model.

## Final Design

Final field:

    deletedAtByUser: map<uid, timestamp>

Behavior:

    Delete conversation
    -> Records deletedAtByUser.<currentUserId> = now

    New message after delete timestamp
    -> Conversation reappears for the deleted user

    Messages before delete timestamp
    -> Remain hidden from that user

    Messages after delete timestamp
    -> Are visible

## Firebase Project Clarification

The Android app uses:

    majestic-layout-255620-b88b2

Firestore rules for TestedCloud Chat must be deployed using:

    firebase deploy --only firestore:rules --project majestic-layout-255620-b88b2

TestedCloud Core still uses:

    majestic-layout-255620

## Completed Work

- Reviewed current Firestore conversation model
- Reviewed current Firestore message model
- Identified direct conversation reuse behavior
- Added user-scoped delete behavior
- Replaced deletedForUsers as the primary visibility mechanism
- Added deletedAtByUser timestamp model
- Updated message visibility logic
- Updated conversation visibility logic
- Updated Firestore rules
- Deployed Firestore rules to the correct Firebase project
- Validated Android build
- Validated user-scoped delete behavior manually

## Issues Found and Resolved

### Wrong Firebase Project for Rules

The Android app uses project majestic-layout-255620-b88b2.

Rules initially deployed to majestic-layout-255620 did not affect the app.

Resolution:

    Deploy Firestore rules to majestic-layout-255620-b88b2.

### deletedForUsers Was Insufficient

deletedForUsers only stored that a user deleted a conversation.

It did not store when the delete happened.

Resolution:

    Use deletedAtByUser to store per-user delete timestamps.

### Old Messages Reappeared After Reactivation

When a conversation was reactivated, old messages could reappear.

Resolution:

    Filter messages by deletedAtByUser.<uid>.

### Messages Appeared Briefly and Disappeared

Firestore optimistic local writes showed messages briefly, but server rules rejected the batch.

Resolution:

    Simplified conversation metadata updates during sendMessage to only update last-message fields.

## Validation Result

Result:

    PASS

The current user-scoped delete behavior is now suitable for the MVP.

## Remaining Improvements

- Improve UI feedback when user enters their own email.
- Add confirmation dialog before deleting a conversation.
- Add visual indicator when a conversation reappears due to a new message.
- Add automated Firestore rules tests in the future.
- Add analytics events:
  - conversation_deleted_for_user
  - conversation_reactivated_by_message
  - message_sent
