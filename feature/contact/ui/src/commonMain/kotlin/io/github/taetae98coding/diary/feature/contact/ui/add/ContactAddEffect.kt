package io.github.taetae98coding.diary.feature.contact.ui.add

internal sealed interface ContactAddEffect {
    data object AddSucceeded : ContactAddEffect

    data object NameBlank : ContactAddEffect

    data object PhoneNumberBlank : ContactAddEffect
}
