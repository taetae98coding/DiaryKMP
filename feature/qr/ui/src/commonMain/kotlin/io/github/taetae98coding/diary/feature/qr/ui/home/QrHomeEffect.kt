package io.github.taetae98coding.diary.feature.qr.ui.home

import kotlin.uuid.Uuid

internal sealed interface QrHomeEffect {
    data class Deleted(
        val id: Uuid,
    ) : QrHomeEffect
}
