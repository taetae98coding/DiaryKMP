package io.github.taetae98coding.diary.feature.core.memo.finished

import kotlin.uuid.Uuid

public sealed interface EntityMemoFinishedListEffect {
    public data class Restarted(
        val id: Uuid,
    ) : EntityMemoFinishedListEffect

    public data class Deleted(
        val id: Uuid,
    ) : EntityMemoFinishedListEffect
}
