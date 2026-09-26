package io.github.taetae98coding.diary.feature.qr.ui.add

internal sealed interface QrAddScaffoldEvent {
    data object ClickNavigateUp : QrAddScaffoldEvent

    data object ClickScan : QrAddScaffoldEvent

    data object ClickAdd : QrAddScaffoldEvent
}
