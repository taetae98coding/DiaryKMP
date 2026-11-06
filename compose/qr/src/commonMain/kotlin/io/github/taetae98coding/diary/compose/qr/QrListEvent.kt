package io.github.taetae98coding.diary.compose.qr

import kotlin.uuid.Uuid

public sealed interface QrListEvent {
    public data class ClickQr(
        val id: Uuid,
    ) : QrListEvent

    public data class SwipeDelete(
        val id: Uuid,
    ) : QrListEvent
}
