package io.github.taetae98coding.diary.domain.contact.usecase

import io.github.taetae98coding.diary.core.model.contact.ContactDetail

internal fun ContactDetail.withTrimmedName(): ContactDetail = copy(name = name.trim())
