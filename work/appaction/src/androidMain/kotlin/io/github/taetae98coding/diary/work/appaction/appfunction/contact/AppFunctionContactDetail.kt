package io.github.taetae98coding.diary.work.appaction.appfunction.contact

import androidx.appfunctions.AppFunctionSerializable
import io.github.taetae98coding.diary.work.appaction.appfunction.item.AppFunctionContact
import io.github.taetae98coding.diary.work.appaction.appfunction.item.AppFunctionTag
import io.github.taetae98coding.diary.work.appaction.appfunction.item.toAppFunctionContact
import io.github.taetae98coding.diary.work.appaction.appfunction.item.toAppFunctionTag
import io.github.taetae98coding.diary.work.appaction.contact.AppActionContact
import io.github.taetae98coding.diary.work.appaction.contact.AppActionContactBirthday
import io.github.taetae98coding.diary.work.appaction.contact.AppActionContactDetail
import io.github.taetae98coding.diary.work.appaction.contact.AppActionContactDetailWithTags
import io.github.taetae98coding.diary.work.appaction.core.AppActionPage

/** The birthday of a contact. */
@AppFunctionSerializable(isDescribedByKDoc = true)
public data class AppFunctionContactBirthday(
    /** Birthday as a date like 1990-12-20, read in the calendar of calendar. A lunar birthday is the lunar year, month and day as is. */
    val date: String,
    /** Calendar of the birthday: solar or lunar. */
    val calendar: String,
)

/** A contact with all its details. */
@AppFunctionSerializable(isDescribedByKDoc = true)
public data class AppFunctionContactDetail(
    /** Id of the contact. */
    val id: String,
    /** Name of the contact. */
    val name: String,
    /** Description of the contact. */
    val description: String,
    /** Height in centimeters. Null when not set. */
    val heightCm: Double?,
    /** Shoe size in whole millimeters. Null when not set. */
    val footSizeMm: Int?,
    /** Birthday of the contact. Null when not set. */
    val birthday: AppFunctionContactBirthday?,
    /** Birth year of the contact. Null when not known. */
    val birthYear: Int?,
    /** Hometown of the contact. */
    val hometown: String,
    /** How the user met the person. */
    val howWeMet: String,
    /** Phone numbers in order. */
    val phoneNumbers: List<String>,
    /** Whether the contact is a favorite. */
    val isFavorite: Boolean,
    /** When the contact was created, in ISO-8601. */
    val createdAt: String,
    /** When the contact was last updated, in ISO-8601. */
    val updatedAt: String,
)

/** One page of contacts. */
@AppFunctionSerializable(isDescribedByKDoc = true)
public data class AppFunctionContactPage(
    /** Contacts in this page, at most 100. */
    val items: List<AppFunctionContact>,
    /** Pass this value as cursor to read the next page. Absent on the last page. */
    val nextCursor: String?,
)

/** A contact with all its details and its linked tags. */
@AppFunctionSerializable(isDescribedByKDoc = true)
public data class AppFunctionContactDetailWithTags(
    /** The contact with all its details. */
    val contact: AppFunctionContactDetail,
    /** Tags linked to the contact. */
    val tags: List<AppFunctionTag>,
)

internal fun AppActionContactDetailWithTags.toAppFunctionContactDetailWithTags(): AppFunctionContactDetailWithTags = AppFunctionContactDetailWithTags(contact = contact.toAppFunctionContactDetail(), tags = tags.map { tag -> tag.toAppFunctionTag() })

internal fun AppActionContactDetail.toAppFunctionContactDetail(): AppFunctionContactDetail =
    AppFunctionContactDetail(
        id = id,
        name = name,
        description = description,
        heightCm = heightCm,
        footSizeMm = footSizeMm,
        birthday = birthday?.toAppFunctionContactBirthday(),
        birthYear = birthYear,
        hometown = hometown,
        howWeMet = howWeMet,
        phoneNumbers = phoneNumbers,
        isFavorite = isFavorite,
        createdAt = createdAt,
        updatedAt = updatedAt,
    )

internal fun AppActionPage<AppActionContact>.toAppFunctionContactPage(): AppFunctionContactPage = AppFunctionContactPage(items = items.map { contact -> contact.toAppFunctionContact() }, nextCursor = nextCursor)

private fun AppActionContactBirthday.toAppFunctionContactBirthday(): AppFunctionContactBirthday = AppFunctionContactBirthday(date = date, calendar = calendar)
