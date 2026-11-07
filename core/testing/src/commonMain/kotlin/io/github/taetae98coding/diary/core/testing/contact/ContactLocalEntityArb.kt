package io.github.taetae98coding.diary.core.testing.contact

import io.github.taetae98coding.diary.core.database.api.contact.entity.CalendarContactBirthdayLocalEntity
import io.github.taetae98coding.diary.core.database.api.contact.entity.ContactBirthdayCalendarLocalEntity
import io.github.taetae98coding.diary.core.database.api.contact.entity.ContactDetailLocalEntity
import io.github.taetae98coding.diary.core.database.api.contact.entity.ContactLocalEntity
import io.github.taetae98coding.diary.core.database.api.contact.entity.ContactPhoneNumberLocalEntity
import io.github.taetae98coding.diary.core.database.api.contact.entity.LunarContactBirthdayLocalEntity
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

public fun Arb.Companion.calendarContactBirthdayLocalEntity(): Arb<CalendarContactBirthdayLocalEntity> =
    arbitrary {
        CalendarContactBirthdayLocalEntity(
            contactId = Arb.kotlinUuid().bind(),
            name = Arb.string().bind(),
            nameQualifier = Arb.string().bind(),
            birthdayDate = Arb.kotlinxLocalDate().bind(),
        )
    }

public fun Arb.Companion.contactDetailLocalEntity(): Arb<ContactDetailLocalEntity> =
    arbitrary {
        ContactDetailLocalEntity(
            name = Arb.string().bind(),
            description = Arb.string().bind(),
            heightCentimeter = Arb.numericDouble().orNull().bind(),
            footSizeMillimeter = Arb.int().orNull().bind(),
            birthday = Arb.kotlinxLocalDate().orNull().bind(),
            birthdayCalendar = Arb.enum<ContactBirthdayCalendarLocalEntity>().orNull().bind(),
            birthYear = Arb.int().orNull().bind(),
            hometown = Arb.string().bind(),
            howWeMet = Arb.string().bind(),
            phoneNumberList = Arb.list(Arb.contactPhoneNumberLocalEntity(), 0..MAX_COLLECTION_SIZE).bind(),
        )
    }

public fun Arb.Companion.contactLocalEntity(): Arb<ContactLocalEntity> =
    arbitrary {
        ContactLocalEntity(
            id = Arb.kotlinUuid().bind(),
            detail = Arb.contactDetailLocalEntity().bind(),
            isFavorite = Arb.boolean().bind(),
            isDeleted = Arb.boolean().bind(),
            updatedAt = Arb.secondPrecisionInstant().bind(),
            createdAt = Arb.secondPrecisionInstant().bind(),
        )
    }

public fun Arb.Companion.contactPhoneNumberLocalEntity(): Arb<ContactPhoneNumberLocalEntity> =
    arbitrary {
        ContactPhoneNumberLocalEntity(
            number = Arb.string().bind(),
        )
    }

public fun Arb.Companion.lunarContactBirthdayLocalEntity(): Arb<LunarContactBirthdayLocalEntity> =
    arbitrary {
        LunarContactBirthdayLocalEntity(
            contactId = Arb.kotlinUuid().bind(),
            name = Arb.string().bind(),
            nameQualifier = Arb.string().bind(),
            birthday = Arb.kotlinxLocalDate().bind(),
        )
    }
