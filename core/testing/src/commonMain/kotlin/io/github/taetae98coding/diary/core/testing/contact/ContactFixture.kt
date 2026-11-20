package io.github.taetae98coding.diary.core.testing.contact

import io.github.taetae98coding.diary.core.model.contact.Contact
import io.github.taetae98coding.diary.core.model.contact.ContactBirthday
import io.github.taetae98coding.diary.core.model.contact.ContactBirthdayCalendar
import io.github.taetae98coding.diary.core.model.contact.ContactDetail
import io.github.taetae98coding.diary.core.model.contact.ContactPhoneNumber
import io.github.taetae98coding.diary.core.model.measure.Length.Companion.centimeter
import io.github.taetae98coding.diary.core.model.measure.Length.Companion.millimeter
import io.github.taetae98coding.diary.core.testing.favoriteAndDeletedCaseList
import io.github.taetae98coding.diary.library.fixture.kotlinUuid
import io.github.taetae98coding.diary.library.fixture.kotlinxLocalDate
import io.github.taetae98coding.diary.library.fixture.secondPrecisionInstant
import io.kotest.property.Arb
import io.kotest.property.arbitrary.arbitrary
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.next
import io.kotest.property.arbitrary.string
import kotlinx.datetime.LocalDate

// 키와 신발 사이즈와 생일은 없을 수 있고 없는 값을 0으로 대신하지 않으므로 있는 조건과 없는 조건을 함께 확인한다.
public val contactMeasureCaseList: List<Boolean> = listOf(true, false)

// 연락처 내용을 어느 계층의 타입에도 묶이지 않은 값으로 둔 것이다.
// 모델 픽스처와 `:core:data-testing`의 엔티티 픽스처가 같은 값에서 각 계층의 표현을 만들어야, 변환을 거치지 않고도 서로의 기대값이 된다.
public data class ContactDetailSource(
    val name: String,
    val description: String,
    val heightCentimeter: Double?,
    val footSizeMillimeter: Int?,
    val birthday: LocalDate?,
    val birthYear: Int?,
    val hometown: String,
    val howWeMet: String,
    val numberList: List<String>,
)

// 전화번호 목록은 비어 있을 수 있고 같은 번호도 합치지 않으므로 순서와 중복을 함께 확인한다.
public fun contactPhoneNumberCaseList(): List<List<String>> =
    listOf(
        emptyList(),
        listOf(phoneNumber()),
        phoneNumber().let { duplicated -> listOf(duplicated, phoneNumber(), duplicated) },
    )

public fun contactDetailSource(
    numberList: List<String>,
    hasBirthday: Boolean,
    hasMeasure: Boolean,
): ContactDetailSource {
    val birthday = if (hasBirthday) Arb.kotlinxLocalDate().next() else null

    return ContactDetailSource(
        name = "name-${Arb.string().next()}",
        description = "description-${Arb.string().next()}",
        // 키는 센티미터와 밀리미터를 오가며 저장하므로, 왕복에서 부동소수점 오차가 끼지 않도록 소수점 아래 한 자리 값으로 만든다.
        heightCentimeter = if (hasMeasure) randomInt(bound = 2000) / 10.0 else null,
        footSizeMillimeter = if (hasMeasure) randomInt(bound = 500) else null,
        birthday = birthday,
        // 생일이 있으면 태어난 연도는 그 생일의 연도이고, 생일이 없으면 다른 선택 값과 함께 태어난 연도만 있는 경우를 만든다.
        birthYear = birthday?.year ?: if (hasMeasure) randomInt(bound = 3000) else null,
        hometown = if (hasMeasure) "hometown-${Arb.string().next()}" else "",
        howWeMet = if (hasMeasure) "how-we-met-${Arb.string().next()}" else "",
        numberList = numberList,
    )
}

// 달력 구분은 생일이 있는 값에만 넘긴다.
public fun ContactDetailSource.toContactDetail(birthdayCalendar: ContactBirthdayCalendar?): ContactDetail =
    ContactDetail(
        name = name,
        description = description,
        height = heightCentimeter?.centimeter,
        footSize = footSizeMillimeter?.millimeter,
        birthday = birthdayCalendar?.let { calendar -> ContactBirthday(date = requireNotNull(birthday), calendar = calendar) },
        birthYear = birthYear,
        hometown = hometown,
        howWeMet = howWeMet,
        phoneNumberList = numberList.map { number -> ContactPhoneNumber(number = number) },
    )

public fun contactCaseList(): List<Contact> =
    contactPhoneNumberCaseList().flatMap { numberList ->
        contactMeasureCaseList.flatMap { hasMeasure ->
            favoriteAndDeletedCaseList.map { (isFavorite, isDeleted) ->
                Arb.contact(numberList = numberList, hasMeasure = hasMeasure, isFavorite = isFavorite, isDeleted = isDeleted).next()
            }
        }
    }

public fun Arb.Companion.contact(
    numberList: List<String>,
    hasMeasure: Boolean,
    isFavorite: Boolean,
    isDeleted: Boolean,
): Arb<Contact> =
    arbitrary {
        // null은 생일 자체가 없는 조건이다.
        val birthdayCalendar = if (hasMeasure) (ContactBirthdayCalendar.entries + null).random() else null

        Contact(
            id = Arb.kotlinUuid().bind(),
            detail =
                contactDetailSource(
                    numberList = numberList,
                    hasBirthday = birthdayCalendar != null,
                    hasMeasure = hasMeasure,
                ).toContactDetail(birthdayCalendar = birthdayCalendar),
            isFavorite = isFavorite,
            isDeleted = isDeleted,
            updatedAt = Arb.secondPrecisionInstant().bind(),
            createdAt = Arb.secondPrecisionInstant().bind(),
        )
    }

private fun phoneNumber(): String = "number-${Arb.string().next()}"

private fun randomInt(bound: Int): Int = Arb.int(0 until bound).next()
