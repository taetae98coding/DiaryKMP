package io.github.taetae98coding.diary.core.testing.contact

import io.github.taetae98coding.diary.core.model.contact.CalendarContactBirthday
import io.github.taetae98coding.diary.core.model.contact.Contact
import io.github.taetae98coding.diary.core.model.contact.ContactBirthday
import io.github.taetae98coding.diary.core.model.contact.ContactBirthdayCalendar
import io.github.taetae98coding.diary.core.model.contact.ContactDetail
import io.github.taetae98coding.diary.core.model.contact.ContactFilter
import io.github.taetae98coding.diary.core.model.contact.ContactFilterExistence
import io.github.taetae98coding.diary.core.model.contact.ContactPhoneNumber
import io.github.taetae98coding.diary.core.testing.measure.length
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
import io.kotest.property.arbitrary.orNull
import io.kotest.property.arbitrary.string

public fun Arb.Companion.calendarContactBirthday(): Arb<CalendarContactBirthday> =
    arbitrary {
        CalendarContactBirthday(
            contactId = Arb.kotlinUuid().bind(),
            name = Arb.string().bind(),
            nameQualifier = Arb.string().bind(),
            date = Arb.kotlinxLocalDate().bind(),
        )
    }

public fun Arb.Companion.contact(): Arb<Contact> =
    arbitrary {
        Contact(
            id = Arb.kotlinUuid().bind(),
            detail = Arb.contactDetail().bind(),
            isFavorite = Arb.boolean().bind(),
            isDeleted = Arb.boolean().bind(),
            updatedAt = Arb.secondPrecisionInstant().bind(),
            createdAt = Arb.secondPrecisionInstant().bind(),
        )
    }

public fun Arb.Companion.contactBirthday(): Arb<ContactBirthday> =
    arbitrary {
        ContactBirthday(
            date = Arb.kotlinxLocalDate().bind(),
            calendar = Arb.enum<ContactBirthdayCalendar>().bind(),
        )
    }

public fun Arb.Companion.contactDetail(): Arb<ContactDetail> =
    arbitrary {
        ContactDetail(
            name = Arb.string().bind(),
            description = Arb.string().bind(),
            height = Arb.length().orNull().bind(),
            footSize = Arb.length().orNull().bind(),
            birthday = Arb.contactBirthday().orNull().bind(),
            birthYear = Arb.int().orNull().bind(),
            hometown = Arb.string().bind(),
            howWeMet = Arb.string().bind(),
            phoneNumberList = Arb.list(Arb.contactPhoneNumber(), 0..MAX_COLLECTION_SIZE).bind(),
        )
    }

public fun Arb.Companion.contactFilter(): Arb<ContactFilter> =
    arbitrary {
        ContactFilter(
            birthday = Arb.enum<ContactFilterExistence>().bind(),
            birthYear = Arb.enum<ContactFilterExistence>().bind(),
            phoneNumber = Arb.enum<ContactFilterExistence>().bind(),
        )
    }

public fun Arb.Companion.contactPhoneNumber(): Arb<ContactPhoneNumber> =
    arbitrary {
        ContactPhoneNumber(
            number = Arb.string().bind(),
        )
    }
