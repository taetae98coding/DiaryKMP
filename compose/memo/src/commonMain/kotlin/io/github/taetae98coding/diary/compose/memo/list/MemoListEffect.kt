package io.github.taetae98coding.diary.compose.memo.list

import kotlin.uuid.Uuid

public sealed interface MemoListEffect {
    public data class Finished(
        val id: Uuid,
    ) : MemoListEffect

    public data class Deleted(
        val id: Uuid,
    ) : MemoListEffect
}
