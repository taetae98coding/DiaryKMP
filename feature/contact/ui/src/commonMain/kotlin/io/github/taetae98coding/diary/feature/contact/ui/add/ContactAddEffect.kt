package io.github.taetae98coding.diary.feature.contact.ui.add

import kotlin.uuid.Uuid

internal sealed interface ContactAddEffect {
    data class AddSucceeded(
        val id: Uuid,
    ) : ContactAddEffect

    data object NameBlank : ContactAddEffect

    data object PhoneNumberBlank : ContactAddEffect
}
