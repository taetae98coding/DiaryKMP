package io.github.taetae98coding.diary.core.model.contact

public data class ContactFilter(
    val birthday: ContactFilterExistence = ContactFilterExistence.ALL,
    val birthYear: ContactFilterExistence = ContactFilterExistence.ALL,
    val phoneNumber: ContactFilterExistence = ContactFilterExistence.ALL,
)
