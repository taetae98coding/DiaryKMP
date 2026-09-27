package io.github.taetae98coding.diary.logger.analytics.impl

import com.google.firebase.Firebase
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.analytics
import com.google.firebase.analytics.logEvent

internal actual fun logScreenView(screenName: String) {
    Firebase.analytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW) {
        param(FirebaseAnalytics.Param.SCREEN_NAME, screenName)
    }
}

internal actual fun logEvent(
    name: String,
    parameters: Map<String, Any>,
) {
    Firebase.analytics.logEvent(name) {
        parameters.forEach { (key, value) ->
            when (value) {
                is String -> param(key, value)
                is Int -> param(key, value.toLong())
                is Long -> param(key, value)
                is Double -> param(key, value)
                else -> param(key, value.toString())
            }
        }
    }
}
