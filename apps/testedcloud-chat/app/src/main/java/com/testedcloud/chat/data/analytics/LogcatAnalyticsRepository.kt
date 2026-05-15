package com.testedcloud.chat.data.analytics

import android.util.Log

class LogcatAnalyticsRepository : AnalyticsRepository {
    override suspend fun track(event: AnalyticsEvent) {
        runCatching {
            Log.i("TestedCloudAnalytics", event.toString())
        }
    }
}
