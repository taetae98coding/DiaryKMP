package io.github.taetae98coding.diary.feature.tag.ui.detail.qr

import kotlin.uuid.Uuid

internal sealed interface TagDetailQrContentEvent {
    data class ClickQr(
        val id: Uuid,
    ) : TagDetailQrContentEvent

    data object Refresh : TagDetailQrContentEvent

    data class DeleteQr(
        val id: Uuid,
    ) : TagDetailQrContentEvent
}
