package io.github.taetae98coding.diary.feature.contact.ui.detail

internal sealed interface ContactDetailEffect {
    data object UpdateSucceeded : ContactDetailEffect

    data object PhoneNumberBlank : ContactDetailEffect

    data object DeleteSucceeded : ContactDetailEffect
}
