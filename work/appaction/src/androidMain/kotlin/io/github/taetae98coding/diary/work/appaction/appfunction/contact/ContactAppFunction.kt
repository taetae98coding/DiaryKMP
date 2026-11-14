package io.github.taetae98coding.diary.work.appaction.appfunction.contact

import io.github.taetae98coding.diary.work.appaction.appfunction.core.AppFunctionChangedCount
import io.github.taetae98coding.diary.work.appaction.appfunction.core.AppFunctionId
import io.github.taetae98coding.diary.work.appaction.appfunction.core.appFunctionCall
import io.github.taetae98coding.diary.work.appaction.appfunction.core.toAppFunctionChangedCount
import io.github.taetae98coding.diary.work.appaction.appfunction.core.toAppFunctionId
import io.github.taetae98coding.diary.work.appaction.contact.AppActionContactUpdateInput
import io.github.taetae98coding.diary.work.appaction.contact.ContactAppAction

internal class ContactAppFunction(
    private val contactAppAction: ContactAppAction,
) {
    suspend fun list(
        sort: String?,
        cursor: String?,
    ): AppFunctionContactPage = appFunctionCall { contactAppAction.list(sort = sort, cursor = cursor).toAppFunctionContactPage() }

    suspend fun get(contactId: String): AppFunctionContactDetailWithTags? = appFunctionCall { contactAppAction.get(contactId = contactId)?.toAppFunctionContactDetailWithTags() }

    suspend fun add(
        name: String,
        description: String?,
        heightCm: String?,
        footSizeMm: Int?,
        birthday: String?,
        birthdayCalendar: String?,
        birthYear: Int?,
        hometown: String?,
        howWeMet: String?,
        phoneNumbers: List<String>,
        tagIds: List<String>,
    ): AppFunctionId =
        appFunctionCall {
            contactAppAction
                .add(
                    name = name,
                    description = description,
                    heightCm = heightCm,
                    footSizeMm = footSizeMm,
                    birthday = birthday,
                    birthdayCalendar = birthdayCalendar,
                    birthYear = birthYear,
                    hometown = hometown,
                    howWeMet = howWeMet,
                    phoneNumbers = phoneNumbers,
                    tagIds = tagIds,
                ).toAppFunctionId()
        }

    suspend fun update(input: AppActionContactUpdateInput): AppFunctionChangedCount = appFunctionCall { contactAppAction.update(input = input).toAppFunctionChangedCount() }

    suspend fun favorite(contactId: String): AppFunctionChangedCount = appFunctionCall { contactAppAction.favorite(contactId = contactId).toAppFunctionChangedCount() }

    suspend fun unfavorite(contactId: String): AppFunctionChangedCount = appFunctionCall { contactAppAction.unfavorite(contactId = contactId).toAppFunctionChangedCount() }

    suspend fun delete(contactId: String): AppFunctionChangedCount = appFunctionCall { contactAppAction.delete(contactId = contactId).toAppFunctionChangedCount() }

    suspend fun restore(contactId: String): AppFunctionChangedCount = appFunctionCall { contactAppAction.restore(contactId = contactId).toAppFunctionChangedCount() }
}
