package io.github.taetae98coding.diary.feature.qr.ui.detail

internal sealed interface QrDetailScaffoldEvent {
    data object ClickNavigateUp : QrDetailScaffoldEvent

    data object ClickUndo : QrDetailScaffoldEvent

    data object ClickScan : QrDetailScaffoldEvent

    data object ClickDelete : QrDetailScaffoldEvent

    data object ClickUpdate : QrDetailScaffoldEvent
}
