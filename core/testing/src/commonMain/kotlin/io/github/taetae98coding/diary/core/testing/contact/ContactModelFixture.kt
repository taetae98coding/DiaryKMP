package io.github.taetae98coding.diary.core.testing.contact

import io.github.taetae98coding.diary.core.model.contact.Contact
import io.github.taetae98coding.diary.library.fixture.kotlinUuid
import io.github.taetae98coding.diary.library.fixture.secondPrecisionInstant
import io.kotest.property.Arb
import io.kotest.property.arbitrary.arbitrary

public fun Arb.Companion.contact(
    name: String,
    isFavorite: Boolean,
    isDeleted: Boolean,
): Arb<Contact> =
    arbitrary {
        Contact(
            id = Arb.kotlinUuid().bind(),
            detail = Arb.contactDetail().bind().copy(name = name),
            isFavorite = isFavorite,
            isDeleted = isDeleted,
            updatedAt = Arb.secondPrecisionInstant().bind(),
            createdAt = Arb.secondPrecisionInstant().bind(),
        )
    }
