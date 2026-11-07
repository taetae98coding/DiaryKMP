package io.github.taetae98coding.diary.logger.crashlytics.impl

import com.google.firebase.crashlytics.FirebaseCrashlytics
import io.github.taetae98coding.diary.logger.crashlytics.api.CrashlyticsLog

private const val ISSUE_KEY_CLASS_NAME = "DiaryIssue"
private const val UNKNOWN_LINE_NUMBER = -1

internal actual fun recordException(log: CrashlyticsLog) {
    FirebaseCrashlytics.getInstance().apply {
        log(log.message)
        recordException(log.toRecordedThrowable())
    }
}

private fun CrashlyticsLog.toRecordedThrowable(): Throwable =
    if (issueKey.isEmpty()) {
        throwable
    } else {
        // 받은 오류의 스택을 건드리지 않도록 같은 내용의 오류를 새로 만들어 스택만 issueKey로 바꾼다.
        IssueKeyException(name = throwable::class.java.name, reason = throwable.message.orEmpty()).apply {
            stackTrace = arrayOf(StackTraceElement(ISSUE_KEY_CLASS_NAME, issueKey, null, UNKNOWN_LINE_NUMBER))
        }
    }

private class IssueKeyException(
    name: String,
    reason: String,
) : Exception("$name: $reason")
