package io.github.taetae98coding.diary.feature.search.ui.home.memo

import kotlin.uuid.Uuid

internal sealed interface SearchHomeMemoEffect {
    data class Finished(
        val id: Uuid,
    ) : SearchHomeMemoEffect

    data class Restarted(
        val id: Uuid,
    ) : SearchHomeMemoEffect

    data class Deleted(
        val id: Uuid,
    ) : SearchHomeMemoEffect
}
