package io.github.taetae98coding.diary.feature.tag.ui.finished

import io.github.taetae98coding.diary.core.model.list.ListSort
import kotlin.uuid.Uuid

internal sealed interface TagFinishedListScaffoldEvent {
    data object ClickNavigateUp : TagFinishedListScaffoldEvent

    data object ClickSort : TagFinishedListScaffoldEvent

    data class SelectSort(
        val sort: ListSort,
    ) : TagFinishedListScaffoldEvent

    data object Refresh : TagFinishedListScaffoldEvent

    data class ClickTag(
        val id: Uuid,
    ) : TagFinishedListScaffoldEvent
}
