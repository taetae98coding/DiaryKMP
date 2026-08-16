package io.github.taetae98coding.diary.feature.contact.ui.add

internal sealed interface ContactAddScaffoldEvent {
    data object ClickNavigateUp : ContactAddScaffoldEvent

    data object ClickAdd : ContactAddScaffoldEvent
}
