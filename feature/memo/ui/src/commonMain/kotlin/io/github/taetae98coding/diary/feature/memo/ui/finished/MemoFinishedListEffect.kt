package io.github.taetae98coding.diary.feature.memo.ui.finished

import kotlin.uuid.Uuid

internal sealed interface MemoFinishedListEffect {
    data class Restarted(
        val id: Uuid,
    ) : MemoFinishedListEffect

    data class Deleted(
        val id: Uuid,
    ) : MemoFinishedListEffect
}
