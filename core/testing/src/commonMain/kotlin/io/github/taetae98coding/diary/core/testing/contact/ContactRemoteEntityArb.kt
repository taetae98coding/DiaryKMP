package io.github.taetae98coding.diary.core.testing.contact

import io.github.taetae98coding.diary.core.network.api.contact.entity.ContactBirthdayCalendarRemoteEntity
import io.github.taetae98coding.diary.core.network.api.contact.entity.ContactDetailRemoteEntity
import io.github.taetae98coding.diary.core.network.api.contact.entity.ContactPhoneNumberRemoteEntity
import io.github.taetae98coding.diary.core.network.api.contact.entity.ContactRemoteEntity
import io.github.taetae98coding.diary.library.fixture.MAX_COLLECTION_SIZE
import io.github.taetae98coding.diary.library.fixture.kotlinUuid
import io.github.taetae98coding.diary.library.fixture.kotlinxLocalDate
import io.github.taetae98coding.diary.library.fixture.secondPrecisionInstant
import io.kotest.property.Arb
import io.kotest.property.arbitrary.arbitrary
import io.kotest.property.arbitrary.boolean
import io.kotest.property.arbitrary.enum
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.list
import io.kotest.property.arbitrary.numericDouble
import io.kotest.property.arbitrary.orNull
import io.kotest.property.arbitrary.string

public fun Arb.Companion.contactDetailRemoteEntity(): Arb<ContactDetailRemoteEntity> =
    arbitrary {
        ContactDetailRemoteEntity(
            name = Arb.string().bind(),
            description = Arb.string().bind(),
            heightCentimeter = Arb.numericDouble().orNull().bind(),
            footSizeMillimeter = Arb.int().orNull().bind(),
            birthday = Arb.kotlinxLocalDate().orNull().bind(),
            birthdayCalendar = Arb.enum<ContactBirthdayCalendarRemoteEntity>().orNull().bind(),
            birthYear = Arb.int().orNull().bind(),
            hometown = Arb.string().bind(),
            howWeMet = Arb.string().bind(),
            phoneNumberList = Arb.list(Arb.contactPhoneNumberRemoteEntity(), 0..MAX_COLLECTION_SIZE).bind(),
        )
    }

public fun Arb.Companion.contactPhoneNumberRemoteEntity(): Arb<ContactPhoneNumberRemoteEntity> =
    arbitrary {
        ContactPhoneNumberRemoteEntity(
            number = Arb.string().bind(),
        )
    }

public fun Arb.Companion.contactRemoteEntity(): Arb<ContactRemoteEntity> =
    arbitrary {
        ContactRemoteEntity(
            id = Arb.kotlinUuid().bind(),
            detail = Arb.contactDetailRemoteEntity().bind(),
            isFavorite = Arb.boolean().bind(),
            isDeleted = Arb.boolean().bind(),
            updatedAt = Arb.secondPrecisionInstant().bind(),
            createdAt = Arb.secondPrecisionInstant().bind(),
        )
    }
