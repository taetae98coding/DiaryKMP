package io.github.taetae98coding.diary.work.sync.mapper

import io.github.taetae98coding.diary.core.database.api.contact.entity.ContactBirthdayCalendarLocalEntity
import io.github.taetae98coding.diary.core.database.api.contact.entity.ContactDetailLocalEntity
import io.github.taetae98coding.diary.core.network.api.contact.entity.ContactBirthdayCalendarRemoteEntity
import io.github.taetae98coding.diary.core.network.api.contact.entity.ContactDetailRemoteEntity

internal fun ContactDetailLocalEntity.toRemote(): ContactDetailRemoteEntity =
    ContactDetailRemoteEntity(
        name = name,
        description = description,
        heightCentimeter = heightCentimeter,
        footSizeMillimeter = footSizeMillimeter,
        birthday = birthday,
        birthdayCalendar = birthday?.let { birthdayCalendar.toRemoteOrSolar() },
        birthYear = birthday?.year ?: birthYear,
        hometown = hometown,
        phoneNumberList = phoneNumberList.map { phoneNumber -> phoneNumber.toRemote() },
    )

internal fun ContactDetailRemoteEntity.toLocal(): ContactDetailLocalEntity =
    ContactDetailLocalEntity(
        name = name,
        description = description,
        heightCentimeter = heightCentimeter,
        footSizeMillimeter = footSizeMillimeter,
        birthday = birthday,
        birthdayCalendar = birthday?.let { birthdayCalendar.toLocalOrSolar() },
        birthYear = birthYear,
        hometown = hometown,
        phoneNumberList = phoneNumberList.map { phoneNumber -> phoneNumber.toLocal() },
    )

// 날짜와 달력 구분, 태어난 연도가 서로 다른 컬럼·필드라 함께 맞아야 한다는 계약을 타입으로 표현할 수 없어, 구분이 비어 있으면 기본값으로, 연도는 생일의 연도로 읽는다.
private fun ContactBirthdayCalendarLocalEntity?.toRemoteOrSolar(): ContactBirthdayCalendarRemoteEntity = this?.toRemote() ?: ContactBirthdayCalendarRemoteEntity.SOLAR

private fun ContactBirthdayCalendarRemoteEntity?.toLocalOrSolar(): ContactBirthdayCalendarLocalEntity = this?.toLocal() ?: ContactBirthdayCalendarLocalEntity.SOLAR
