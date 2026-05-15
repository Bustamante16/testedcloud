package com.testedcloud.chat.data.analytics

import java.time.Instant
import java.util.UUID

data class AnalyticsEvent(
    val eventId: String = UUID.randomUUID().toString(),
    val eventType: String,
    val source: String = "testedcloud-chat-android",
    val origin: String = "firebase",
    val userId: String,
    val conversationId: String? = null,
    val messageId: String? = null,
    val createdAt: String = Instant.now().toString(),
    val metadata: Map<String, Any?> = emptyMap()
)
