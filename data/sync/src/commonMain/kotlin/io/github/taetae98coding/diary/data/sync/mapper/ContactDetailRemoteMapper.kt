package io.github.taetae98coding.diary.data.sync.mapper

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
        phoneNumberList = phoneNumberList.map { phoneNumber -> phoneNumber.toLocal() },
    )

// 날짜와 달력 구분이 서로 다른 컬럼·필드라 둘이 함께 있어야 한다는 계약을 타입으로 표현할 수 없다.
// 날짜만 있고 구분이 비어 있는 값은 스펙이 정한 기본값인 양력으로 읽어 생일을 잃지 않고 그대로 다룬다.
private fun ContactBirthdayCalendarLocalEntity?.toRemoteOrSolar(): ContactBirthdayCalendarRemoteEntity = this?.toRemote() ?: ContactBirthdayCalendarRemoteEntity.SOLAR

private fun ContactBirthdayCalendarRemoteEntity?.toLocalOrSolar(): ContactBirthdayCalendarLocalEntity = this?.toLocal() ?: ContactBirthdayCalendarLocalEntity.SOLAR
