package io.github.taetae98coding.diary.core.data.testing.contact

import io.github.taetae98coding.diary.core.database.api.contact.entity.ContactLocalEntity
import io.kotest.property.Arb
import io.kotest.property.arbitrary.map

public fun Arb.Companion.localContact(
    isFavorite: Boolean,
    isDeleted: Boolean,
): Arb<ContactLocalEntity> = Arb.contactLocalEntity().map { contact -> contact.copy(isFavorite = isFavorite, isDeleted = isDeleted) }
