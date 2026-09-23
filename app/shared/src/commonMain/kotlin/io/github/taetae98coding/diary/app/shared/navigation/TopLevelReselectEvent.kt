package io.github.taetae98coding.diary.app.shared.navigation

import androidx.compose.runtime.Stable
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow

@Stable
internal class TopLevelReselectEvent {
    private val flowMap =
        TopLevelNavigation.entries.associateWith {
            MutableSharedFlow<Unit>(extraBufferCapacity = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)
        }

    fun flowOf(topLevelNavigation: TopLevelNavigation): Flow<Unit> = flowMap.getValue(topLevelNavigation)

    fun send(topLevelNavigation: TopLevelNavigation) {
        flowMap.getValue(topLevelNavigation).tryEmit(Unit)
    }
}
