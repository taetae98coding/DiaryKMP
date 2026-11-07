package io.github.taetae98coding.diary.feature.timetable.ui

import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

internal fun <T, R> StateFlow<T>.mapState(transform: (T) -> R): StateFlow<R> =
    object : StateFlow<R> {
        override val value: R get() = transform(this@mapState.value)
        override val replayCache: List<R> get() = listOf(value)

        override suspend fun collect(collector: FlowCollector<R>): Nothing {
            this@mapState.map(transform).distinctUntilChanged().collect(collector)
            awaitCancellation()
        }
    }
