package io.github.taetae98coding.diary.compose.memo

import kotlin.uuid.Uuid

public sealed interface MemoListEvent {
    public data object Refresh : MemoListEvent

    public data class ClickMemo(
        val id: Uuid,
    ) : MemoListEvent

    public data class SwipeFinish(
        val id: Uuid,
    ) : MemoListEvent

    public data class SwipeDelete(
        val id: Uuid,
    ) : MemoListEvent
}
