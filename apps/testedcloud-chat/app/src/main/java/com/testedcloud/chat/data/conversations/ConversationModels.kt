package com.testedcloud.chat.data.conversations

import com.google.firebase.Timestamp

data class Conversation(
    val conversationId: String = "",
    val type: String = "direct",
    val participantIds: List<String> = emptyList(),
    val participantCount: Int = 0,
    val directKey: String = "",
    val participantEmails: Map<String, String> = emptyMap(),
    val participantDisplayNames: Map<String, String> = emptyMap(),
    val createdAt: Timestamp? = null,
    val updatedAt: Timestamp? = null,
    val lastMessageText: String = "",
    val lastMessageAt: Timestamp? = null,
    val lastMessageSenderId: String = "",
    val createdBy: String = "",
    val status: String = "active",
    val deletedForUsers: List<String> = emptyList()
)

data class ChatMessage(
    val messageId: String = "",
    val conversationId: String = "",
    val senderId: String = "",
    val text: String = "",
    val createdAt: Timestamp? = null,
    val updatedAt: Timestamp? = null,
    val status: String = "sent",
    val type: String = "text",
    val deleted: Boolean = false
)
