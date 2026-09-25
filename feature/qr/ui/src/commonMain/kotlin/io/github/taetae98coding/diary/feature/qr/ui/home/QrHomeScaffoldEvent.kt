package io.github.taetae98coding.diary.feature.qr.ui.home

internal sealed interface QrHomeScaffoldEvent {
    data object ClickNavigateUp : QrHomeScaffoldEvent

    data object ClickScan : QrHomeScaffoldEvent
}
