package io.github.taetae98coding.diary.data.contact.mapper

import io.github.taetae98coding.diary.core.database.api.contact.entity.ContactBirthdayCalendarLocalEntity
import io.github.taetae98coding.diary.core.database.api.contact.entity.ContactDetailLocalEntity
import io.github.taetae98coding.diary.core.model.contact.ContactBirthday
import io.github.taetae98coding.diary.core.model.contact.ContactBirthdayCalendar
import io.github.taetae98coding.diary.core.model.contact.ContactDetail
import io.github.taetae98coding.diary.core.model.measure.Length.Companion.centimeter
import io.github.taetae98coding.diary.core.model.measure.Length.Companion.millimeter

internal fun ContactDetail.toLocal(): ContactDetailLocalEntity =
    ContactDetailLocalEntity(
        name = name,
        description = description,
        heightCentimeter = height?.inCentimeter,
        footSizeMillimeter = footSize?.inWholeMillimeter,
        birthday = birthday?.date,
        birthdayCalendar = birthday?.calendar?.toLocal(),
        phoneNumberList = phoneNumberList.map { phoneNumber -> phoneNumber.toLocal() },
    )

internal fun ContactDetailLocalEntity.toDomain(): ContactDetail =
    ContactDetail(
        name = name,
        description = description,
        height = heightCentimeter?.centimeter,
        footSize = footSizeMillimeter?.millimeter,
        birthday = birthday?.let { date -> ContactBirthday(date = date, calendar = birthdayCalendar.toDomainOrSolar()) },
        phoneNumberList = phoneNumberList.map { phoneNumber -> phoneNumber.toDomain() },
    )

// 날짜와 달력 구분이 서로 다른 컬럼·필드라 둘이 함께 있어야 한다는 계약을 타입으로 표현할 수 없다.
// 날짜만 있고 구분이 비어 있는 값은 스펙이 정한 기본값인 양력으로 읽어 생일을 잃지 않고 그대로 다룬다.
private fun ContactBirthdayCalendarLocalEntity?.toDomainOrSolar(): ContactBirthdayCalendar = this?.toDomain() ?: ContactBirthdayCalendar.SOLAR
