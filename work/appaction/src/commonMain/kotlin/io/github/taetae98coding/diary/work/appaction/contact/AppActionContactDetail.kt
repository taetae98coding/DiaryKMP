package io.github.taetae98coding.diary.work.appaction.contact

import io.github.taetae98coding.diary.core.model.contact.Contact
import io.github.taetae98coding.diary.core.model.contact.ContactBirthday
import io.github.taetae98coding.diary.work.appaction.tag.AppActionTag

public data class AppActionContactDetail(
    val id: String,
    val name: String,
    val description: String,
    val heightCm: Double?,
    val footSizeMm: Int?,
    val birthday: AppActionContactBirthday?,
    val birthYear: Int?,
    val hometown: String,
    val howWeMet: String,
    val phoneNumbers: List<String>,
    val isFavorite: Boolean,
    val createdAt: String,
    val updatedAt: String,
)

public data class AppActionContactBirthday(
    val date: String,
    val calendar: String,
)

public data class AppActionContactDetailWithTags(
    val contact: AppActionContactDetail,
    val tags: List<AppActionTag>,
)

internal fun Contact.toAppActionContactDetail(): AppActionContactDetail =
    AppActionContactDetail(
        id = id.toString(),
        name = detail.name,
        description = detail.description,
        heightCm = detail.height?.inCentimeter,
        footSizeMm = detail.footSize?.inWholeMillimeter,
        birthday = detail.birthday?.toAppActionContactBirthday(),
        birthYear = detail.birthYear,
        hometown = detail.hometown,
        howWeMet = detail.howWeMet,
        phoneNumbers = detail.phoneNumberList.map { phoneNumber -> phoneNumber.number },
        isFavorite = isFavorite,
        createdAt = createdAt.toString(),
        updatedAt = updatedAt.toString(),
    )

private fun ContactBirthday.toAppActionContactBirthday(): AppActionContactBirthday = AppActionContactBirthday(date = date.toString(), calendar = calendar.toBirthdayCalendarValue())
