package io.github.taetae98coding.diary.data.contact.mapper

import io.github.taetae98coding.diary.core.model.contact.ContactFilterExistence

internal fun ContactFilterExistence.toLocal(): Boolean? =
    when (this) {
        ContactFilterExistence.ALL -> null
        ContactFilterExistence.EXIST -> true
        ContactFilterExistence.NOT_EXIST -> false
    }
