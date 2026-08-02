package io.github.taetae98coding.diary.feature.tag.ui.memo.finished

import kotlin.uuid.Uuid

internal sealed interface TagMemoFinishedListEffect {
    data class Restarted(
        val id: Uuid,
    ) : TagMemoFinishedListEffect

    data class Deleted(
        val id: Uuid,
    ) : TagMemoFinishedListEffect
}
