package io.github.taetae98coding.diary.logger.analytics.impl

internal actual fun logScreenView(screenName: String) = Unit

internal actual fun logEvent(
    name: String,
    parameters: Map<String, Any>,
) = Unit
