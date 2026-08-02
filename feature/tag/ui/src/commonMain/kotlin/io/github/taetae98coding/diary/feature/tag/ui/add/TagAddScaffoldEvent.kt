package io.github.taetae98coding.diary.feature.tag.ui.add

import kotlin.uuid.Uuid

internal sealed interface TagAddScaffoldEvent {
    data object ClickNavigateUp : TagAddScaffoldEvent

    data object ClickAdd : TagAddScaffoldEvent

    data object ClickLinkAdd : TagAddScaffoldEvent

    data class ClickLink(
        val id: Uuid,
    ) : TagAddScaffoldEvent
}
