package io.github.taetae98coding.diary.core.testing.contact

import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.database.api.contact.entity.ContactBirthdayCalendarLocalEntity
import io.github.taetae98coding.diary.core.database.api.contact.entity.ContactDetailLocalEntity
import io.github.taetae98coding.diary.core.database.api.contact.entity.ContactLocalEntity
import io.github.taetae98coding.diary.core.database.api.contact.entity.ContactPhoneNumberLocalEntity
import io.github.taetae98coding.diary.core.model.contact.Contact
import io.github.taetae98coding.diary.core.model.contact.ContactBirthday
import io.github.taetae98coding.diary.core.model.contact.ContactBirthdayCalendar
import io.github.taetae98coding.diary.core.model.contact.ContactDetail
import io.github.taetae98coding.diary.core.model.contact.ContactPhoneNumber
import io.github.taetae98coding.diary.core.model.measure.Length.Companion.centimeter
import io.github.taetae98coding.diary.core.model.measure.Length.Companion.millimeter
import io.github.taetae98coding.diary.core.network.api.contact.entity.ContactBirthdayCalendarRemoteEntity
import io.github.taetae98coding.diary.core.network.api.contact.entity.ContactDetailRemoteEntity
import io.github.taetae98coding.diary.core.network.api.contact.entity.ContactPhoneNumberRemoteEntity
import io.github.taetae98coding.diary.core.testing.isDeletedCaseList
import kotlinx.datetime.LocalDate
import kotlin.uuid.Uuid

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

// 키와 신발 사이즈와 생일은 없을 수 있고 없는 값을 0으로 대신하지 않으므로 있는 조건과 없는 조건을 함께 확인한다.
public val contactMeasureCaseList: List<Boolean> = listOf(true, false)

// 같은 연락처를 세 계층으로 표현한 값이다. 이름과 수치가 같으므로 어느 방향의 변환이든 이 짝으로 기대값을 삼을 수 있다.
public data class ContactDetailCase(
    val domain: ContactDetail,
    val local: ContactDetailLocalEntity,
    val remote: ContactDetailRemoteEntity,
)

// 전화번호 목록은 비어 있을 수 있고 같은 번호도 합치지 않으므로 순서와 중복을 함께 확인한다.
public fun FixtureMonkey.contactPhoneNumberCaseList(): List<List<String>> =
    listOf(
        emptyList(),
        listOf(phoneNumber()),
        phoneNumber().let { duplicated -> listOf(duplicated, phoneNumber(), duplicated) },
    )

public fun FixtureMonkey.contactDetailCaseList(): List<ContactDetailCase> =
    contactBirthdayCalendarCaseList.flatMap { calendarCase ->
        contactPhoneNumberCaseList().flatMap { numberList ->
            contactMeasureCaseList.map { hasMeasure ->
                contactDetailCase(numberList = numberList, calendarCase = calendarCase, hasMeasure = hasMeasure)
            }
        }
    }

// 날짜와 달력 구분이 서로 다른 컬럼이라 날짜만 있고 구분이 비어 있는 로컬 값이 남아 있을 수 있다.
// 모델과 원격은 이 조건을 만들지 않으므로 local이 출발점인 방향에만 더한다. 기준을 양력 짝으로 잡아 기대값이 스펙의 기본값과 같다.
public fun FixtureMonkey.contactDetailCaseWithoutBirthdayCalendar(): ContactDetailCase {
    val solarCase = contactDetailCase(numberList = emptyList(), calendarCase = contactBirthdayCalendarCaseList.first(), hasMeasure = true)

    return solarCase.copy(local = solarCase.local.copy(birthdayCalendar = null))
}

public fun FixtureMonkey.contactDetailCase(
    numberList: List<String>,
    calendarCase: ContactBirthdayCalendarCase?,
    hasMeasure: Boolean,
): ContactDetailCase {
    val name = "name-${giveMeOne<String>()}"
    val description = "description-${giveMeOne<String>()}"
    // 키는 센티미터와 밀리미터를 오가며 저장하므로, 왕복에서 부동소수점 오차가 끼지 않도록 소수점 아래 한 자리 값으로 만든다.
    val heightCentimeter = if (hasMeasure) randomInt(bound = 2000) / 10.0 else null
    val footSizeMillimeter = if (hasMeasure) randomInt(bound = 500) else null
    val birthday = calendarCase?.let { date() }

    return ContactDetailCase(
        domain =
            ContactDetail(
                name = name,
                description = description,
                height = heightCentimeter?.centimeter,
                footSize = footSizeMillimeter?.millimeter,
                birthday = calendarCase?.let { case -> ContactBirthday(date = requireNotNull(birthday), calendar = case.domain) },
                phoneNumberList = numberList.map { number -> ContactPhoneNumber(number = number) },
            ),
        local =
            ContactDetailLocalEntity(
                name = name,
                description = description,
                heightCentimeter = heightCentimeter,
                footSizeMillimeter = footSizeMillimeter,
                birthday = birthday,
                birthdayCalendar = calendarCase?.local,
                phoneNumberList = numberList.map { number -> ContactPhoneNumberLocalEntity(number = number) },
            ),
        remote =
            ContactDetailRemoteEntity(
                name = name,
                description = description,
                heightCentimeter = heightCentimeter,
                footSizeMillimeter = footSizeMillimeter,
                birthday = birthday,
                birthdayCalendar = calendarCase?.remote,
                phoneNumberList = numberList.map { number -> ContactPhoneNumberRemoteEntity(number = number) },
            ),
    )
}

public fun FixtureMonkey.contactCaseList(): List<Contact> =
    contactPhoneNumberCaseList().flatMap { numberList ->
        contactMeasureCaseList.flatMap { hasMeasure ->
            isDeletedCaseList.map { isDeleted ->
                contact(numberList = numberList, hasMeasure = hasMeasure, isDeleted = isDeleted)
            }
        }
    }

public fun FixtureMonkey.localContactCaseList(): List<ContactLocalEntity> =
    contactPhoneNumberCaseList().flatMap { numberList ->
        contactMeasureCaseList.flatMap { hasMeasure ->
            isDeletedCaseList.map { isDeleted ->
                localContact(numberList = numberList, hasMeasure = hasMeasure, isDeleted = isDeleted)
            }
        }
    }

public fun FixtureMonkey.contact(
    numberList: List<String>,
    hasMeasure: Boolean,
    isDeleted: Boolean,
): Contact =
    Contact(
        id = giveMeOne<Uuid>(),
        detail =
            contactDetailCase(
                numberList = numberList,
                calendarCase = if (hasMeasure) contactBirthdayCalendarCaseList.random() else null,
                hasMeasure = hasMeasure,
            ).domain,
        isDeleted = isDeleted,
        updatedAt = giveMeOne(),
        createdAt = giveMeOne(),
    )

public fun FixtureMonkey.localContact(
    numberList: List<String>,
    hasMeasure: Boolean,
    isDeleted: Boolean,
): ContactLocalEntity =
    ContactLocalEntity(
        id = giveMeOne<Uuid>(),
        detail =
            contactDetailCase(
                numberList = numberList,
                calendarCase = if (hasMeasure) contactBirthdayCalendarCaseList.random() else null,
                hasMeasure = hasMeasure,
            ).local,
        isDeleted = isDeleted,
        updatedAt = giveMeOne(),
        createdAt = giveMeOne(),
    )

private fun FixtureMonkey.phoneNumber(): String = "number-${giveMeOne<String>()}"

private fun FixtureMonkey.date(): LocalDate =
    LocalDate(
        year = 1900 + randomInt(bound = 200),
        month = 1 + randomInt(bound = 12),
        day = 1 + randomInt(bound = 28),
    )

private fun FixtureMonkey.randomInt(bound: Int): Int = (giveMeOne<Int>().toUInt() % bound.toUInt()).toInt()
