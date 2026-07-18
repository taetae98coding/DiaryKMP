package io.github.taetae98coding.diary.logger.core

public object DiaryLogger {
    private val delegateSet = mutableSetOf<DiaryLoggerDelegate>()

    public fun add(delegate: DiaryLoggerDelegate) {
        delegateSet += delegate
    }

    public fun log(log: DiaryLog) {
        delegateSet.forEach { delegate ->
            runCatching { delegate.log(log) }
        }
    }
}
