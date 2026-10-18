package io.github.taetae98coding.diary.domain.contact.usecase

import io.github.taetae98coding.diary.core.model.contact.ContactDetail

internal fun ContactDetail.withBirthYearOfBirthday(): ContactDetail = birthday?.let { birthday -> copy(birthYear = birthday.date.year) } ?: this
