package io.github.taetae98coding.diary.feature.tag.ui.detail.qr

internal sealed interface TagDetailQrContentEvent {
    data object Refresh : TagDetailQrContentEvent
}
