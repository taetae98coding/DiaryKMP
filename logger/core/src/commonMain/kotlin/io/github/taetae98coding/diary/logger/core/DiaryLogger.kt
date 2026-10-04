@file:OptIn(ExperimentalAtomicApi::class)

package io.github.taetae98coding.diary.logger.core

import kotlin.concurrent.atomics.AtomicReference
import kotlin.concurrent.atomics.ExperimentalAtomicApi
import kotlin.concurrent.atomics.update

public object DiaryLogger {
    // 등록과 전달이 서로 다른 스레드에서 겹쳐도 순회 중인 집합이 바뀌지 않도록 등록할 때마다 새 집합으로 교체한다.
    private val delegateSet = AtomicReference<Set<DiaryLoggerDelegate>>(emptySet())

    public fun add(delegate: DiaryLoggerDelegate) {
        delegateSet.update { set -> set + delegate }
    }

    public fun log(log: DiaryLog) {
        delegateSet.load().forEach { delegate ->
            runCatching { delegate.log(log) }
        }
    }
}
