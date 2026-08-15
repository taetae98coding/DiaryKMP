package io.github.taetae98coding.diary.feature.web.ui.add

internal sealed interface WebAddScaffoldEvent {
    data object ClickNavigateUp : WebAddScaffoldEvent

    data object ClickAdd : WebAddScaffoldEvent
}
