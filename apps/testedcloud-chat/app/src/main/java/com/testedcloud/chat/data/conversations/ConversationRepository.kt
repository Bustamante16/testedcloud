package com.testedcloud.chat.data.conversations

import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class ConversationRepository(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    private fun directKeyFor(userA: String, userB: String): String {
        return listOf(userA, userB).sorted().joinToString("_")
    }

    private fun readStringList(value: Any?): List<String> {
        return (value as? List<*>)
            ?.filterIsInstance<String>()
            ?: emptyList()
    }

    private fun readStringMap(value: Any?): Map<String, String> {
        return (value as? Map<*, *>)
            ?.mapNotNull { (key, mapValue) ->
                val stringKey = key as? String
                val stringValue = mapValue as? String

                if (stringKey != null && stringValue != null) {
                    stringKey to stringValue
                } else {
                    null
                }
            }
            ?.toMap()
            ?: emptyMap()
    }

    fun observeConversations(currentUserId: String): Flow<List<Conversation>> = callbackFlow {
        val listener = db.collection("conversations")
            .whereArrayContains("participantIds", currentUserId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    return@addSnapshotListener
                }

                val conversations = snapshot?.documents
                    ?.map { doc ->
                        Conversation(
                            conversationId = doc.id,
                            type = doc.getString("type") ?: "direct",
                            participantIds = readStringList(doc.get("participantIds")),
                            participantCount = (doc.getLong("participantCount") ?: 0L).toInt(),
                            directKey = doc.getString("directKey") ?: "",
                            participantEmails = readStringMap(doc.get("participantEmails")),
                            participantDisplayNames = readStringMap(doc.get("participantDisplayNames")),
                            createdAt = doc.getTimestamp("createdAt"),
                            updatedAt = doc.getTimestamp("updatedAt"),
                            lastMessageText = doc.getString("lastMessageText") ?: "",
                            lastMessageAt = doc.getTimestamp("lastMessageAt"),
                            lastMessageSenderId = doc.getString("lastMessageSenderId") ?: "",
                            createdBy = doc.getString("createdBy") ?: "",
                            status = doc.getString("status") ?: "active"
                        )
                    }
                    ?.sortedByDescending { conversation ->
                        conversation.updatedAt?.seconds ?: 0L
                    }
                    ?: emptyList()

                trySend(conversations)
            }

        awaitClose { listener.remove() }
    }

    fun observeMessages(conversationId: String): Flow<List<ChatMessage>> = callbackFlow {
        val listener = db.collection("conversations")
            .document(conversationId)
            .collection("messages")
            .orderBy("createdAt")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    return@addSnapshotListener
                }

                val messages = snapshot?.documents
                    ?.map { doc ->
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
                    }
                    ?: emptyList()

                trySend(messages)
            }

        awaitClose { listener.remove() }
    }

    suspend fun findUserIdByEmail(email: String): String {
        val cleanEmail = email.trim().lowercase()

        require(cleanEmail.isNotBlank()) { "Email is required" }

        val result = try {
            db.collection("users")
                .whereEqualTo("email", cleanEmail)
                .limit(1)
                .get()
                .await()
        } catch (e: Exception) {
            throw Exception("User email lookup failed for $cleanEmail: ${e.message}", e)
        }

        require(!result.isEmpty) { "No user found with email: $cleanEmail" }

        return result.documents.first().id
    }

    suspend fun createDirectConversationByEmail(
        currentUserId: String,
        otherUserEmail: String
    ): String {
        val otherUserId = findUserIdByEmail(otherUserEmail)

        return createDirectConversation(
            currentUserId = currentUserId,
            otherUserId = otherUserId
        )
    }

    suspend fun createDirectConversation(
        currentUserId: String,
        otherUserId: String
    ): String {
        require(currentUserId.isNotBlank()) { "Current user ID is required" }
        require(otherUserId.isNotBlank()) { "Other user ID is required" }
        require(currentUserId != otherUserId) { "Cannot create conversation with yourself" }

        val directKey = directKeyFor(currentUserId, otherUserId)

        val existingForCurrentUser = try {
            db.collection("conversations")
                .whereArrayContains("participantIds", currentUserId)
                .get()
                .await()
        } catch (e: Exception) {
            throw Exception("Existing conversation lookup failed: ${e.message}", e)
        }

        val participantSet = setOf(currentUserId, otherUserId)

        val existingConversation = existingForCurrentUser.documents.firstOrNull { doc ->
            val type = doc.getString("type") ?: ""
            val existingDirectKey = doc.getString("directKey") ?: ""
            val participants = readStringList(doc.get("participantIds")).toSet()

            type == "direct" &&
                (existingDirectKey == directKey || participants == participantSet)
        }

        if (existingConversation != null) {
            return existingConversation.id
        }

        val now = Timestamp.now()

        val currentUserDoc = try {
            db.collection("users")
                .document(currentUserId)
                .get()
                .await()
        } catch (e: Exception) {
            throw Exception("Current user profile read failed: ${e.message}", e)
        }

        val otherUserDoc = try {
            db.collection("users")
                .document(otherUserId)
                .get()
                .await()
        } catch (e: Exception) {
            throw Exception("Other user profile read failed: ${e.message}", e)
        }

        val currentEmail = currentUserDoc.getString("email") ?: currentUserId
        val otherEmail = otherUserDoc.getString("email") ?: otherUserId

        val currentDisplayName = currentUserDoc.getString("displayName") ?: currentEmail
        val otherDisplayName = otherUserDoc.getString("displayName") ?: otherEmail

        val conversationData = mapOf(
            "type" to "direct",
            "participantIds" to listOf(currentUserId, otherUserId),
            "participantCount" to 2,
            "directKey" to directKey,
            "participantEmails" to mapOf(
                currentUserId to currentEmail,
                otherUserId to otherEmail
            ),
            "participantDisplayNames" to mapOf(
                currentUserId to currentDisplayName,
                otherUserId to otherDisplayName
            ),
            "createdAt" to now,
            "updatedAt" to now,
            "lastMessageText" to "",
            "lastMessageAt" to null,
            "lastMessageSenderId" to "",
            "createdBy" to currentUserId,
            "status" to "active"
        )

        val docRef = try {
            db.collection("conversations")
                .add(conversationData)
                .await()
        } catch (e: Exception) {
            throw Exception("Conversation create failed: ${e.message}", e)
        }

        return docRef.id
    }

    suspend fun sendMessage(
        conversationId: String,
        senderId: String,
        text: String
    ) {
        val cleanText = text.trim()

        require(conversationId.isNotBlank()) { "Conversation ID is required" }
        require(senderId.isNotBlank()) { "Sender ID is required" }
        require(cleanText.isNotBlank()) { "Message cannot be empty" }
        require(cleanText.length <= 1000) { "Message is too long" }

        val now = Timestamp.now()

        val conversationRef = db.collection("conversations").document(conversationId)
        val messageRef = conversationRef.collection("messages").document()

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

        try {
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
        } catch (e: Exception) {
            throw Exception("Message send failed: ${e.message}", e)
        }
    }
}
