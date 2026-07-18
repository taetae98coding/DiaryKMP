package io.github.taetae98coding.diary.logger.crashlytics.impl

import com.google.firebase.crashlytics.FirebaseCrashlytics

internal actual fun recordException(
    message: String,
    throwable: Throwable,
) {
    FirebaseCrashlytics.getInstance().apply {
        log(message)
        recordException(throwable)
    }
}
