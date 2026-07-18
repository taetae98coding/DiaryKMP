package io.github.taetae98coding.diary.logger.console.impl

import android.util.Log

internal actual fun printLog(
    tag: String,
    message: String,
    throwable: Throwable?,
) {
    if (throwable == null) {
        Log.d(tag, message)
    } else {
        Log.d(tag, message, throwable)
    }
}
