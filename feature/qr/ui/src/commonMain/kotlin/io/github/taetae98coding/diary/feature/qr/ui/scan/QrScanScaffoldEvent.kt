package io.github.taetae98coding.diary.feature.qr.ui.scan

internal sealed interface QrScanScaffoldEvent {
    data object ClickNavigateUp : QrScanScaffoldEvent
}
