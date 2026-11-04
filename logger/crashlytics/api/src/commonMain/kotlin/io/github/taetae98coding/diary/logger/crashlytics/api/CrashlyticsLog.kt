package io.github.taetae98coding.diary.logger.crashlytics.api

import io.github.taetae98coding.diary.logger.core.DiaryLog

// Crashlytics는 오류를 스택 트레이스로 묶으므로, 같은 자리에서 만든 오류를 대상별로 나누려면 issueKey를 스택 대신 쓴다. 비어 있으면 오류의 스택으로 묶는다.
public data class CrashlyticsLog(
    val message: String,
    val throwable: Throwable,
    val issueKey: String = "",
) : DiaryLog
