package io.github.taetae98coding.diary.core.data.testing.contact

import io.github.taetae98coding.diary.core.database.api.contact.entity.ContactBirthdayCalendarLocalEntity
import io.github.taetae98coding.diary.core.database.api.contact.entity.ContactDetailLocalEntity
import io.github.taetae98coding.diary.core.database.api.contact.entity.ContactLocalEntity
import io.github.taetae98coding.diary.core.database.api.contact.entity.ContactPhoneNumberLocalEntity
import io.github.taetae98coding.diary.core.model.contact.ContactBirthdayCalendar
import io.github.taetae98coding.diary.core.model.contact.ContactDetail
import io.github.taetae98coding.diary.core.network.api.contact.entity.ContactBirthdayCalendarRemoteEntity
import io.github.taetae98coding.diary.core.network.api.contact.entity.ContactDetailRemoteEntity
import io.github.taetae98coding.diary.core.network.api.contact.entity.ContactPhoneNumberRemoteEntity
import io.github.taetae98coding.diary.core.testing.contact.contactDetailSource
import io.github.taetae98coding.diary.core.testing.contact.contactMeasureCaseList
import io.github.taetae98coding.diary.core.testing.contact.contactPhoneNumberCaseList
import io.github.taetae98coding.diary.core.testing.contact.toContactDetail
import io.github.taetae98coding.diary.core.testing.favoriteAndDeletedCaseList
import io.github.taetae98coding.diary.library.fixture.kotlinUuid
import io.github.taetae98coding.diary.library.fixture.secondPrecisionInstant
import io.kotest.property.Arb
import io.kotest.property.arbitrary.arbitrary
import io.kotest.property.arbitrary.next

// 달력 구분은 계층마다 별도 타입이라 매퍼가 아니라 이 짝이 대응을 소유한다.
public data class ContactBirthdayCalendarCase(
    val domain: ContactBirthdayCalendar,
    val local: ContactBirthdayCalendarLocalEntity,
    val remote: ContactBirthdayCalendarRemoteEntity,
)

// null은 생일 자체가 없는 조건이다.
public val contactBirthdayCalendarCaseList: List<ContactBirthdayCalendarCase?> =
    listOf(
        ContactBirthdayCalendarCase(ContactBirthdayCalendar.SOLAR, ContactBirthdayCalendarLocalEntity.SOLAR, ContactBirthdayCalendarRemoteEntity.SOLAR),
        ContactBirthdayCalendarCase(ContactBirthdayCalendar.LUNAR, ContactBirthdayCalendarLocalEntity.LUNAR, ContactBirthdayCalendarRemoteEntity.LUNAR),
        null,
    )

// 같은 연락처를 세 계층으로 표현한 값이다. 이름과 수치가 같으므로 어느 방향의 변환이든 이 짝으로 기대값을 삼을 수 있다.
public data class ContactDetailCase(
    val domain: ContactDetail,
    val local: ContactDetailLocalEntity,
    val remote: ContactDetailRemoteEntity,
)

public fun contactDetailCaseList(): List<ContactDetailCase> =
    contactBirthdayCalendarCaseList.flatMap { calendarCase ->
        contactPhoneNumberCaseList().flatMap { numberList ->
            contactMeasureCaseList.map { hasMeasure ->
                contactDetailCase(numberList = numberList, calendarCase = calendarCase, hasMeasure = hasMeasure)
            }
        }
    }

// 날짜와 달력 구분이 서로 다른 컬럼이라 날짜만 있고 구분이 비어 있는 로컬 값이 남아 있을 수 있다.
// 모델과 원격은 이 조건을 만들지 않으므로 local이 출발점인 방향에만 더한다. 기준을 양력 짝으로 잡아 기대값이 스펙의 기본값과 같다.
public fun contactDetailCaseWithoutBirthdayCalendar(): ContactDetailCase {
    val solarCase = contactDetailCase(numberList = emptyList(), calendarCase = contactBirthdayCalendarCaseList.first(), hasMeasure = true)

    return solarCase.copy(local = solarCase.local.copy(birthdayCalendar = null))
}

public fun contactDetailCase(
    numberList: List<String>,
    calendarCase: ContactBirthdayCalendarCase?,
    hasMeasure: Boolean,
): ContactDetailCase {
    val source = contactDetailSource(numberList = numberList, hasBirthday = calendarCase != null, hasMeasure = hasMeasure)

    return ContactDetailCase(
        domain = source.toContactDetail(birthdayCalendar = calendarCase?.domain),
        local =
            ContactDetailLocalEntity(
                name = source.name,
                description = source.description,
                heightCentimeter = source.heightCentimeter,
                footSizeMillimeter = source.footSizeMillimeter,
                birthday = source.birthday,
                birthdayCalendar = calendarCase?.local,
                birthYear = source.birthYear,
                hometown = source.hometown,
                howWeMet = source.howWeMet,
                phoneNumberList = source.numberList.map { number -> ContactPhoneNumberLocalEntity(number = number) },
            ),
        remote =
            ContactDetailRemoteEntity(
                name = source.name,
                description = source.description,
                heightCentimeter = source.heightCentimeter,
                footSizeMillimeter = source.footSizeMillimeter,
                birthday = source.birthday,
                birthdayCalendar = calendarCase?.remote,
                birthYear = source.birthYear,
                hometown = source.hometown,
                howWeMet = source.howWeMet,
                phoneNumberList = source.numberList.map { number -> ContactPhoneNumberRemoteEntity(number = number) },
            ),
    )
}

public fun localContactCaseList(): List<ContactLocalEntity> =
    contactPhoneNumberCaseList().flatMap { numberList ->
        contactMeasureCaseList.flatMap { hasMeasure ->
            favoriteAndDeletedCaseList.map { (isFavorite, isDeleted) ->
                Arb.localContact(numberList = numberList, hasMeasure = hasMeasure, isFavorite = isFavorite, isDeleted = isDeleted).next()
            }
        }
    }

public fun Arb.Companion.localContact(
    numberList: List<String>,
    hasMeasure: Boolean,
    isFavorite: Boolean,
    isDeleted: Boolean,
): Arb<ContactLocalEntity> =
    arbitrary {
        ContactLocalEntity(
            id = Arb.kotlinUuid().bind(),
            detail =
                contactDetailCase(
                    numberList = numberList,
                    calendarCase = if (hasMeasure) contactBirthdayCalendarCaseList.random() else null,
                    hasMeasure = hasMeasure,
                ).local,
            isFavorite = isFavorite,
            isDeleted = isDeleted,
            updatedAt = Arb.secondPrecisionInstant().bind(),
            createdAt = Arb.secondPrecisionInstant().bind(),
        )
    }
