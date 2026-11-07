package io.github.taetae98coding.diary.compose.qr

import kotlin.uuid.Uuid

public sealed interface QrListEffect {
    public data class Deleted(
        val id: Uuid,
    ) : QrListEffect
}
