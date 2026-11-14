package io.github.taetae98coding.diary.work.appaction.contact

import io.github.taetae98coding.diary.core.model.contact.Contact
import io.github.taetae98coding.diary.core.model.contact.ContactBirthday
import io.github.taetae98coding.diary.core.model.contact.ContactBirthdayCalendar
import io.github.taetae98coding.diary.core.model.contact.ContactDetail
import io.github.taetae98coding.diary.core.model.contact.ContactFilter
import io.github.taetae98coding.diary.core.model.list.ListSort
import io.github.taetae98coding.diary.domain.contact.usecase.AddContactUseCase
import io.github.taetae98coding.diary.domain.contact.usecase.DeleteContactUseCase
import io.github.taetae98coding.diary.domain.contact.usecase.FavoriteContactUseCase
import io.github.taetae98coding.diary.domain.contact.usecase.FindContactUseCase
import io.github.taetae98coding.diary.domain.contact.usecase.PageContactUseCase
import io.github.taetae98coding.diary.domain.contact.usecase.RestoreContactUseCase
import io.github.taetae98coding.diary.domain.contact.usecase.UnfavoriteContactUseCase
import io.github.taetae98coding.diary.domain.contact.usecase.UpdateContactUseCase
import io.github.taetae98coding.diary.domain.tag.EntityTagKind
import io.github.taetae98coding.diary.domain.tag.usecase.GetEntityTagUseCase
import io.github.taetae98coding.diary.work.appaction.core.AppActionChangedCount
import io.github.taetae98coding.diary.work.appaction.core.AppActionId
import io.github.taetae98coding.diary.work.appaction.core.AppActionPage
import io.github.taetae98coding.diary.work.appaction.core.changeActive
import io.github.taetae98coding.diary.work.appaction.core.invalidArgument
import io.github.taetae98coding.diary.work.appaction.core.map
import io.github.taetae98coding.diary.work.appaction.core.readActive
import io.github.taetae98coding.diary.work.appaction.core.readFirst
import io.github.taetae98coding.diary.work.appaction.core.readPage
import io.github.taetae98coding.diary.work.appaction.core.runAppAction
import io.github.taetae98coding.diary.work.appaction.core.toAppActionChangedCount
import io.github.taetae98coding.diary.work.appaction.core.toAppActionId
import io.github.taetae98coding.diary.work.appaction.core.toLocalDateArgumentOrNull
import io.github.taetae98coding.diary.work.appaction.core.toPageOffset
import io.github.taetae98coding.diary.work.appaction.core.toSortArgument
import io.github.taetae98coding.diary.work.appaction.core.toUuidArgument
import io.github.taetae98coding.diary.work.appaction.core.toUuidSetArgument
import io.github.taetae98coding.diary.work.appaction.tag.toAppActionTag
import org.koin.core.annotation.Factory

private const val CONTACT_ID = "contactId"

internal val contactSortList: List<ListSort> = listOf(ListSort.NAME, ListSort.RECENTLY_UPDATED)

@Factory
public class ContactAppAction internal constructor(
    private val pageContactUseCase: PageContactUseCase,
    private val findContactUseCase: FindContactUseCase,
    private val getEntityTagUseCase: GetEntityTagUseCase,
    private val addContactUseCase: AddContactUseCase,
    private val updateContactUseCase: UpdateContactUseCase,
    private val favoriteContactUseCase: FavoriteContactUseCase,
    private val unfavoriteContactUseCase: UnfavoriteContactUseCase,
    private val deleteContactUseCase: DeleteContactUseCase,
    private val restoreContactUseCase: RestoreContactUseCase,
) {
    public suspend fun list(
        sort: String?,
        cursor: String?,
    ): AppActionPage<AppActionContact> =
        runAppAction {
            pageContactUseCase
                .readPage(
                    parameter = PageContactUseCase.Parameter(sort = sort.toSortArgument(contactSortList), filter = ContactFilter()),
                    offset = cursor.toPageOffset(),
                ).map { contact -> contact.toAppActionContact() }
        }

    public suspend fun get(contactId: String): AppActionContactDetailWithTags? = runAppAction { detail(contactId = contactId, includesDeleted = false) }

    public suspend fun getIncludingDeleted(contactId: String): AppActionContactDetailWithTags? = runAppAction { detail(contactId = contactId, includesDeleted = true) }

    private suspend fun detail(
        contactId: String,
        includesDeleted: Boolean,
    ): AppActionContactDetailWithTags? {
        val id = contactId.toUuidArgument(name = CONTACT_ID)
        val contact = findContactUseCase.readFirst(parameter = id)?.takeIf { contact -> includesDeleted || !contact.isDeleted } ?: return null

        return AppActionContactDetailWithTags(
            contact = contact.toAppActionContactDetail(),
            tags = getEntityTagUseCase.readFirst(parameter = GetEntityTagUseCase.Parameter(kind = EntityTagKind.CONTACT, entityId = id)).map { tag -> tag.toAppActionTag() },
        )
    }

    public suspend fun add(
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
    ): AppActionId =
        runAppAction {
            val birthdayDate = birthday.toLocalDateArgumentOrNull(name = CONTACT_BIRTHDAY)
            val calendar = birthdayCalendar.toContactBirthdayCalendarArgumentOrNull()

            if (birthdayDate == null && calendar != null) invalidArgument(message = "'$CONTACT_BIRTHDAY_CALENDAR' needs '$CONTACT_BIRTHDAY'.")

            val detail =
                ContactDetail(
                    name = name,
                    description = description.orEmpty(),
                    height = heightCm.toContactHeightArgumentOrNull(),
                    footSize = footSizeMm.toContactFootSizeArgumentOrNull(),
                    birthday = birthdayDate?.let { date -> ContactBirthday(date = date, calendar = calendar ?: ContactBirthdayCalendar.SOLAR) },
                    birthYear = birthYear.toContactBirthYearArgumentOrNull(),
                    hometown = hometown.orEmpty(),
                    howWeMet = howWeMet.orEmpty(),
                    phoneNumberList = phoneNumbers.toContactPhoneNumberList(),
                )

            addContactUseCase(parameter = AddContactUseCase.Parameter(detail = detail, tagIdSet = tagIds.toUuidSetArgument(name = "tagIds"))).getOrThrow().toAppActionId()
        }

    public suspend fun update(input: AppActionContactUpdateInput): AppActionChangedCount =
        runAppAction {
            val id = input.contactId.toUuidArgument(name = CONTACT_ID)
            val current = findContactUseCase.readActive(id = id, isDeleted = Contact::isDeleted)?.detail ?: return@runAppAction 0.toAppActionChangedCount()

            updateContactUseCase(parameter = UpdateContactUseCase.Parameter(id = id, detail = input.toContactDetail(current = current))).getOrThrow().toAppActionChangedCount()
        }

    public suspend fun favorite(contactId: String): AppActionChangedCount =
        runAppAction {
            val id = contactId.toUuidArgument(name = CONTACT_ID)

            findContactUseCase.changeActive(id = id, isDeleted = Contact::isDeleted) { favoriteContactUseCase(parameter = id) }
        }

    public suspend fun unfavorite(contactId: String): AppActionChangedCount =
        runAppAction {
            val id = contactId.toUuidArgument(name = CONTACT_ID)

            findContactUseCase.changeActive(id = id, isDeleted = Contact::isDeleted) { unfavoriteContactUseCase(parameter = id) }
        }

    public suspend fun delete(contactId: String): AppActionChangedCount =
        runAppAction {
            val id = contactId.toUuidArgument(name = CONTACT_ID)

            findContactUseCase.changeActive(id = id, isDeleted = Contact::isDeleted) { deleteContactUseCase(parameter = id) }
        }

    public suspend fun restore(contactId: String): AppActionChangedCount = runAppAction { restoreContactUseCase(parameter = contactId.toUuidArgument(name = CONTACT_ID)).getOrThrow().toAppActionChangedCount() }
}
