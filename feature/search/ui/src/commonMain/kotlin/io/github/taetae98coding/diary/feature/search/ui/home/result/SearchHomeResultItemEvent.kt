package io.github.taetae98coding.diary.feature.search.ui.home.result

import kotlin.uuid.Uuid

internal sealed interface SearchHomeResultItemEvent {
    data class Click(
        val id: Uuid,
    ) : SearchHomeResultItemEvent

    data class SwipeDelete(
        val id: Uuid,
    ) : SearchHomeResultItemEvent
}
