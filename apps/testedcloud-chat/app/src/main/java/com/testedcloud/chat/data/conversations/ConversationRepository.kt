package com.testedcloud.chat.data.conversations

import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class ConversationRepository(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    fun observeConversations(currentUserId: String): Flow<List<Conversation>> = callbackFlow {
        val listener = db.collection("conversations")
            .whereArrayContains("participantIds", currentUserId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    return@addSnapshotListener
                }

                val conversations = snapshot?.documents?.map { doc ->
                    Conversation(
                        conversationId = doc.id,
                        type = doc.getString("type") ?: "direct",
                        participantIds = doc.get("participantIds") as? List<String> ?: emptyList(),
                        participantCount = (doc.getLong("participantCount") ?: 0L).toInt(),
                        createdAt = doc.getTimestamp("createdAt"),
                        updatedAt = doc.getTimestamp("updatedAt"),
                        lastMessageText = doc.getString("lastMessageText") ?: "",
                        lastMessageAt = doc.getTimestamp("lastMessageAt"),
                        lastMessageSenderId = doc.getString("lastMessageSenderId") ?: "",
                        createdBy = doc.getString("createdBy") ?: "",
                        status = doc.getString("status") ?: "active"
                    )
                }?.sortedByDescending { conversation ->
                    conversation.updatedAt?.seconds ?: 0L
                } ?: emptyList()

                trySend(conversations)
            }

        awaitClose { listener.remove() }
    }

    fun observeMessages(conversationId: String): Flow<List<ChatMessage>> = callbackFlow {
        val listener = db.collection("conversations")
            .document(conversationId)
            .collection("messages")
            .orderBy("createdAt", Query.Direction.ASCENDING)
            .limit(100)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    return@addSnapshotListener
                }

                val messages = snapshot?.documents?.map { doc ->
                    ChatMessage(
                        messageId = doc.id,
                        conversationId = doc.getString("conversationId") ?: conversationId,
                        senderId = doc.getString("senderId") ?: "",
                        text = doc.getString("text") ?: "",
                        createdAt = doc.getTimestamp("createdAt"),
                        updatedAt = doc.getTimestamp("updatedAt"),
                        status = doc.getString("status") ?: "sent",
                        type = doc.getString("type") ?: "text",
                        deleted = doc.getBoolean("deleted") ?: false
                    )
                } ?: emptyList()

                trySend(messages)
            }

        awaitClose { listener.remove() }
    }

    suspend fun createDirectConversation(
        currentUserId: String,
        otherUserId: String
    ): String {
        require(currentUserId.isNotBlank()) { "Current user ID is required" }
        require(otherUserId.isNotBlank()) { "Other user ID is required" }
        require(currentUserId != otherUserId) { "Cannot create conversation with yourself" }

        val now = Timestamp.now()

        val conversationData = mapOf(
            "type" to "direct",
            "participantIds" to listOf(currentUserId, otherUserId),
            "participantCount" to 2,
            "createdAt" to now,
            "updatedAt" to now,
            "lastMessageText" to "",
            "lastMessageAt" to null,
            "lastMessageSenderId" to "",
            "createdBy" to currentUserId,
            "status" to "active"
        )

        val docRef = db.collection("conversations").add(conversationData).await()

        docRef.update("conversationId", docRef.id).await()

        return docRef.id
    }

    suspend fun sendMessage(
        conversationId: String,
        senderId: String,
        text: String
    ) {
        val cleanText = text.trim()
        require(cleanText.isNotBlank()) { "Message cannot be empty" }
        require(cleanText.length <= 1000) { "Message is too long" }

        val now = Timestamp.now()

        val messageRef = db.collection("conversations")
            .document(conversationId)
            .collection("messages")
            .document()

        val messageData = mapOf(
            "messageId" to messageRef.id,
            "conversationId" to conversationId,
            "senderId" to senderId,
            "text" to cleanText,
            "createdAt" to now,
            "updatedAt" to null,
            "status" to "sent",
            "type" to "text",
            "deleted" to false
        )

        val conversationRef = db.collection("conversations").document(conversationId)

        db.runBatch { batch ->
            batch.set(messageRef, messageData)
            batch.update(
                conversationRef,
                mapOf(
                    "updatedAt" to now,
                    "lastMessageText" to cleanText,
                    "lastMessageAt" to now,
                    "lastMessageSenderId" to senderId
                )
            )
        }.await()
    }
}
