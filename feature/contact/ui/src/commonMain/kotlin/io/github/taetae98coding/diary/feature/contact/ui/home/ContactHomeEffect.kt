package io.github.taetae98coding.diary.feature.contact.ui.home

import kotlin.uuid.Uuid

internal sealed interface ContactHomeEffect {
    data class Deleted(
        val id: Uuid,
    ) : ContactHomeEffect
}
