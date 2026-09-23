package io.github.taetae98coding.diary.feature.contact.ui.detail

internal sealed interface ContactDetailScaffoldEvent {
    data object ClickNavigateUp : ContactDetailScaffoldEvent

    data object ClickUpdate : ContactDetailScaffoldEvent

    data object ClickFavorite : ContactDetailScaffoldEvent

    data object ClickDelete : ContactDetailScaffoldEvent
}
