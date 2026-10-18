package io.github.taetae98coding.diary.feature.qr.ui.detail

import io.github.taetae98coding.diary.domain.qr.content.QrAction

internal sealed interface QrDetailScaffoldEvent {
    data object ClickNavigateUp : QrDetailScaffoldEvent

    data class ClickAction(
        val action: QrAction,
    ) : QrDetailScaffoldEvent

    data object ClickUndo : QrDetailScaffoldEvent

    data object ClickScan : QrDetailScaffoldEvent

    data object ClickDelete : QrDetailScaffoldEvent

    data object ClickUpdate : QrDetailScaffoldEvent
}
