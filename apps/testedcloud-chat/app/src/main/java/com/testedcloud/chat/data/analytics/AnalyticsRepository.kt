package com.testedcloud.chat.data.analytics

interface AnalyticsRepository {
    suspend fun track(event: AnalyticsEvent)
}
