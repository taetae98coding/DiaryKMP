package io.github.taetae98coding.diary.feature.qr.ui.home

import kotlin.uuid.Uuid

internal sealed interface QrHomeScaffoldEvent {
    data object ClickNavigateUp : QrHomeScaffoldEvent

    data object ClickAdd : QrHomeScaffoldEvent

    data object Refresh : QrHomeScaffoldEvent

    data class DeleteQr(
        val id: Uuid,
    ) : QrHomeScaffoldEvent
}
