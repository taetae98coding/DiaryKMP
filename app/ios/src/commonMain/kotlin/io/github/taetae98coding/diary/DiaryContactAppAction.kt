package io.github.taetae98coding.diary

import io.github.taetae98coding.diary.work.appaction.contact.AppActionContactBirthdayCalendarValue
import io.github.taetae98coding.diary.work.appaction.contact.AppActionContactDetailWithTags
import io.github.taetae98coding.diary.work.appaction.contact.AppActionContactUpdateInput
import io.github.taetae98coding.diary.work.appaction.contact.ContactAppAction
import io.github.taetae98coding.diary.work.appaction.contact.appActionContactBirthdayArgument
import io.github.taetae98coding.diary.work.appaction.contact.appActionContactBirthdayEpochMillis
import io.github.taetae98coding.diary.work.appaction.contact.appActionContactHeightArgument
import io.github.taetae98coding.diary.work.appaction.contact.filterBySearchQuery
import io.github.taetae98coding.diary.work.appaction.core.appActionEpochMillis
import io.github.taetae98coding.diary.work.appaction.core.readNumberedPage

public enum class DiaryAppActionContactBirthdayCalendar {
    SOLAR,
    LUNAR,
}

public data class DiaryAppActionContactPage(
    val items: List<DiaryAppActionContact>,
    val nextPageNumber: Int?,
)

public data class DiaryAppActionContactDetail(
    val id: String,
    val name: String,
    val contactDescription: String,
    val heightCm: Double?,
    val footSizeMm: Int?,
    val birthdayEpochMillis: Long?,
    val birthdayCalendar: DiaryAppActionContactBirthdayCalendar?,
    val birthYear: Int?,
    val hometown: String,
    val howWeMet: String,
    val phoneNumbers: List<String>,
    val isFavorite: Boolean,
    val createdAtEpochMillis: Long,
    val updatedAtEpochMillis: Long,
    val tags: List<DiaryAppActionTag>,
)

public object DiaryContactAppAction {
    public suspend fun list(
        sort: DiaryAppActionSort?,
        pageNumber: Int,
    ): DiaryAppActionContactPage {
        val page = readNumberedPage(pageNumber = pageNumber) { cursor -> contactAppAction().list(sort = sort.toSortValue(), cursor = cursor) }

        return DiaryAppActionContactPage(items = page.items.map { contact -> contact.toDiary() }, nextPageNumber = page.nextPageNumber)
    }

    public suspend fun search(query: String): List<DiaryAppActionContact> =
        readNumberedPage(pageNumber = SEARCH_PAGE_NUMBER) { cursor -> contactAppAction().list(sort = null, cursor = cursor) }
            .items
            .filterBySearchQuery(query = query)
            .map { contact -> contact.toDiary() }

    public suspend fun get(contactId: String): DiaryAppActionContactDetail? = contactAppAction().get(contactId = contactId)?.toDiary()

    public suspend fun getIncludingDeleted(contactId: String): DiaryAppActionContactDetail? = contactAppAction().getIncludingDeleted(contactId = contactId)?.toDiary()

    public suspend fun add(
        name: String,
        contactDescription: String?,
        heightCm: Double?,
        footSizeMm: Int?,
        birthdayEpochMillis: Long?,
        birthdayCalendar: DiaryAppActionContactBirthdayCalendar?,
        birthYear: Int?,
        hometown: String?,
        howWeMet: String?,
        phoneNumbers: List<String>,
        tagIds: List<String>,
    ): String =
        contactAppAction()
            .add(
                name = name,
                description = contactDescription,
                heightCm = appActionContactHeightArgument(heightCm = heightCm),
                footSizeMm = footSizeMm,
                birthday = appActionContactBirthdayArgument(epochMillis = birthdayEpochMillis),
                birthdayCalendar = birthdayCalendar?.toValue(),
                birthYear = birthYear,
                hometown = hometown,
                howWeMet = howWeMet,
                phoneNumbers = phoneNumbers,
                tagIds = tagIds,
            ).id

    public suspend fun update(
        contactId: String,
        name: String?,
        contactDescription: String?,
        heightCm: Double?,
        clearHeight: Boolean?,
        footSizeMm: Int?,
        clearFootSize: Boolean?,
        birthdayEpochMillis: Long?,
        birthdayCalendar: DiaryAppActionContactBirthdayCalendar?,
        clearBirthday: Boolean?,
        birthYear: Int?,
        clearBirthYear: Boolean?,
        hometown: String?,
        howWeMet: String?,
        phoneNumbers: List<String>?,
    ): Int {
        val input =
            AppActionContactUpdateInput(
                contactId = contactId,
                name = name,
                description = contactDescription,
                heightCm = appActionContactHeightArgument(heightCm = heightCm),
                clearHeight = clearHeight,
                footSizeMm = footSizeMm,
                clearFootSize = clearFootSize,
                birthday = appActionContactBirthdayArgument(epochMillis = birthdayEpochMillis),
                birthdayCalendar = birthdayCalendar?.toValue(),
                clearBirthday = clearBirthday,
                birthYear = birthYear,
                clearBirthYear = clearBirthYear,
                hometown = hometown,
                howWeMet = howWeMet,
                phoneNumbers = phoneNumbers,
            )

        return contactAppAction().update(input = input).changedCount
    }

    public suspend fun favorite(contactId: String): Int = contactAppAction().favorite(contactId = contactId).changedCount

    public suspend fun unfavorite(contactId: String): Int = contactAppAction().unfavorite(contactId = contactId).changedCount

    public suspend fun delete(contactId: String): Int = contactAppAction().delete(contactId = contactId).changedCount

    public suspend fun restore(contactId: String): Int = contactAppAction().restore(contactId = contactId).changedCount
}

private fun contactAppAction(): ContactAppAction = diaryAppAction()

private fun DiaryAppActionContactBirthdayCalendar.toValue(): String =
    when (this) {
        DiaryAppActionContactBirthdayCalendar.SOLAR -> AppActionContactBirthdayCalendarValue.SOLAR
        DiaryAppActionContactBirthdayCalendar.LUNAR -> AppActionContactBirthdayCalendarValue.LUNAR
    }

private fun String.toDiaryBirthdayCalendar(): DiaryAppActionContactBirthdayCalendar? =
    when (this) {
        AppActionContactBirthdayCalendarValue.SOLAR -> DiaryAppActionContactBirthdayCalendar.SOLAR
        AppActionContactBirthdayCalendarValue.LUNAR -> DiaryAppActionContactBirthdayCalendar.LUNAR
        else -> null
    }

private fun AppActionContactDetailWithTags.toDiary(): DiaryAppActionContactDetail =
    DiaryAppActionContactDetail(
        id = contact.id,
        name = contact.name,
        contactDescription = contact.description,
        heightCm = contact.heightCm,
        footSizeMm = contact.footSizeMm,
        birthdayEpochMillis = contact.birthday?.let { birthday -> appActionContactBirthdayEpochMillis(date = birthday.date) },
        birthdayCalendar = contact.birthday?.calendar?.toDiaryBirthdayCalendar(),
        birthYear = contact.birthYear,
        hometown = contact.hometown,
        howWeMet = contact.howWeMet,
        phoneNumbers = contact.phoneNumbers,
        isFavorite = contact.isFavorite,
        createdAtEpochMillis = appActionEpochMillis(instant = contact.createdAt),
        updatedAtEpochMillis = appActionEpochMillis(instant = contact.updatedAt),
        tags = tags.map { tag -> tag.toDiary() },
    )
