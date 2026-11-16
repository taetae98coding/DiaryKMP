package io.github.taetae98coding.diary.work.mcp.tool.contact

import io.github.taetae98coding.diary.core.model.contact.Contact
import io.github.taetae98coding.diary.core.model.contact.ContactBirthday
import io.github.taetae98coding.diary.core.model.contact.ContactBirthdayCalendar
import io.github.taetae98coding.diary.core.model.contact.ContactDetail
import io.github.taetae98coding.diary.core.model.contact.ContactPhoneNumber
import io.github.taetae98coding.diary.core.model.list.ListSort
import io.github.taetae98coding.diary.domain.contact.usecase.AddContactUseCase
import io.github.taetae98coding.diary.domain.contact.usecase.FindContactUseCase
import io.github.taetae98coding.diary.domain.contact.usecase.UpdateContactUseCase
import io.github.taetae98coding.diary.work.appaction.contact.ContactAppAction
import io.github.taetae98coding.diary.work.appaction.core.readActive
import io.github.taetae98coding.diary.work.appaction.memo.MemoListAppAction
import io.github.taetae98coding.diary.work.appaction.tag.AppActionEntityTagKind
import io.github.taetae98coding.diary.work.appaction.tag.EntityTagAppAction
import io.github.taetae98coding.diary.work.mcp.tool.McpTool
import io.github.taetae98coding.diary.work.mcp.tool.McpToolArgumentException
import io.github.taetae98coding.diary.work.mcp.tool.McpToolArguments
import io.github.taetae98coding.diary.work.mcp.tool.McpToolKind
import io.github.taetae98coding.diary.work.mcp.tool.McpToolParameter
import io.github.taetae98coding.diary.work.mcp.tool.McpToolParameterType
import io.github.taetae98coding.diary.work.mcp.tool.McpToolSet
import io.github.taetae98coding.diary.work.mcp.tool.appActionCall
import io.github.taetae98coding.diary.work.mcp.tool.cursorText
import io.github.taetae98coding.diary.work.mcp.tool.idText
import io.github.taetae98coding.diary.work.mcp.tool.json.changedCountResult
import io.github.taetae98coding.diary.work.mcp.tool.json.idResult
import io.github.taetae98coding.diary.work.mcp.tool.json.sortParameter
import io.github.taetae98coding.diary.work.mcp.tool.json.toJson
import io.github.taetae98coding.diary.work.mcp.tool.localDateOrNull
import io.github.taetae98coding.diary.work.mcp.tool.memo.memoSortList
import io.github.taetae98coding.diary.work.mcp.tool.paging.mcpPageCursorParameter
import io.github.taetae98coding.diary.work.mcp.tool.paging.toJson
import io.github.taetae98coding.diary.work.mcp.tool.sortText
import io.github.taetae98coding.diary.work.mcp.tool.tag.entityTagToolList
import io.github.taetae98coding.diary.work.mcp.tool.tag.tagIdsParameter
import io.github.taetae98coding.diary.work.mcp.tool.uuid
import io.github.taetae98coding.diary.work.mcp.tool.uuidSet
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import org.koin.core.annotation.Factory

private const val CONTACT_ID = "contactId"
private const val DESCRIPTION = "description"
private const val HOMETOWN = "hometown"
private const val HOW_WE_MET = "howWeMet"

internal val contactSortList: List<ListSort> = listOf(ListSort.NAME, ListSort.RECENTLY_UPDATED)

private val contactIdParameter: McpToolParameter =
    McpToolParameter(name = CONTACT_ID, type = McpToolParameterType.STRING, description = "Id of the contact.", isRequired = true)

@Factory(binds = [McpToolSet::class])
internal class ContactMcpToolSet(
    private val contactAppAction: ContactAppAction,
    private val memoListAppAction: MemoListAppAction,
    private val entityTagAppAction: EntityTagAppAction,
    private val findContactUseCase: FindContactUseCase,
    private val addContactUseCase: AddContactUseCase,
    private val updateContactUseCase: UpdateContactUseCase,
) : McpToolSet {
    override val toolList: List<McpTool> =
        listOf(
            McpTool(
                name = "contact_list",
                description = "Lists contacts that are not deleted. Favorite contacts always come first, and the sort applies within favorites and within the rest.",
                parameterList = listOf(sortParameter(contactSortList), mcpPageCursorParameter),
                kind = McpToolKind.READ,
            ) { arguments ->
                appActionCall { contactAppAction.list(sort = arguments.sortText(contactSortList), cursor = arguments.cursorText()) }.toJson { contact -> contact.toJson() }
            },
            McpTool(
                name = "contact_get",
                description = "Gets one contact with all its details and linked tags. Returns null when the contact does not exist or is deleted.",
                parameterList = listOf(contactIdParameter),
                kind = McpToolKind.READ,
            ) { arguments -> appActionCall { contactAppAction.get(contactId = arguments.idText(CONTACT_ID)) }?.toJson() ?: JsonNull },
            McpTool(
                name = "contact_memo_list",
                description = "Lists unfinished memos linked to a contact that are not deleted.",
                parameterList = listOf(contactIdParameter, sortParameter(memoSortList), mcpPageCursorParameter),
                kind = McpToolKind.READ,
            ) { arguments ->
                appActionCall {
                    memoListAppAction.contactList(contactId = arguments.idText(CONTACT_ID), sort = arguments.sortText(memoSortList), cursor = arguments.cursorText())
                }.toJson { memo -> memo.toJson() }
            },
            McpTool(
                name = "contact_memo_finished_list",
                description = "Lists finished memos linked to a contact that are not deleted.",
                parameterList = listOf(contactIdParameter, sortParameter(memoSortList), mcpPageCursorParameter),
                kind = McpToolKind.READ,
            ) { arguments ->
                appActionCall {
                    memoListAppAction.contactFinishedList(contactId = arguments.idText(CONTACT_ID), sort = arguments.sortText(memoSortList), cursor = arguments.cursorText())
                }.toJson { memo -> memo.toJson() }
            },
            McpTool(
                name = "contact_add",
                description = "Adds a contact. The name must not be blank and no phone number may be blank. The new contact is not a favorite.",
                parameterList =
                    listOf(
                        McpToolParameter(name = "name", type = McpToolParameterType.STRING, description = "Name of the contact.", isRequired = true),
                        McpToolParameter(name = DESCRIPTION, type = McpToolParameterType.STRING, description = "Description of the contact."),
                        contactHeightParameter,
                        contactFootSizeParameter,
                        contactBirthdayParameter,
                        contactBirthdayCalendarParameter,
                        contactBirthYearParameter,
                        McpToolParameter(name = HOMETOWN, type = McpToolParameterType.STRING, description = "Hometown as one line of free text."),
                        McpToolParameter(name = HOW_WE_MET, type = McpToolParameterType.STRING, description = "How the user met the person, as one line of free text."),
                        contactPhoneNumbersParameter,
                        tagIdsParameter,
                    ),
                kind = McpToolKind.WRITE,
            ) { arguments -> addContact(arguments) },
            McpTool(
                name = "contact_update",
                description =
                    "Updates the details of a contact. Omitted fields keep their current value. A blank name keeps the current name. " +
                        "Changing only the birthday date keeps its calendar. Removing the birthday also removes the birth year unless a birth year is given. " +
                        "Favorite is changed with contact_favorite and contact_unfavorite. Tags are changed with contact_tag_add and contact_tag_remove.",
                parameterList =
                    listOf(
                        contactIdParameter,
                        McpToolParameter(name = "name", type = McpToolParameterType.STRING, description = "New name."),
                        McpToolParameter(name = DESCRIPTION, type = McpToolParameterType.STRING, description = "New description."),
                        contactHeightParameter,
                        McpToolParameter(name = "clearHeight", type = McpToolParameterType.BOOLEAN, description = "Set true to remove the height."),
                        contactFootSizeParameter,
                        McpToolParameter(name = "clearFootSize", type = McpToolParameterType.BOOLEAN, description = "Set true to remove the shoe size."),
                        contactBirthdayParameter,
                        contactBirthdayCalendarParameter,
                        McpToolParameter(name = "clearBirthday", type = McpToolParameterType.BOOLEAN, description = "Set true to remove the birthday."),
                        contactBirthYearParameter,
                        McpToolParameter(name = "clearBirthYear", type = McpToolParameterType.BOOLEAN, description = "Set true to remove the birth year of a contact without a birthday."),
                        McpToolParameter(name = HOMETOWN, type = McpToolParameterType.STRING, description = "New hometown."),
                        McpToolParameter(name = HOW_WE_MET, type = McpToolParameterType.STRING, description = "New text about how the user met the person."),
                        McpToolParameter(
                            name = CONTACT_PHONE_NUMBERS,
                            type = McpToolParameterType.STRING_LIST,
                            description = "Replaces all phone numbers, in order. Pass an empty list to remove them all. Each number must not be blank.",
                        ),
                    ),
                kind = McpToolKind.WRITE,
            ) { arguments -> updateContact(arguments) },
            McpTool(
                name = "contact_favorite",
                description = "Marks a contact as a favorite.",
                parameterList = listOf(contactIdParameter),
                kind = McpToolKind.WRITE,
            ) { arguments -> appActionCall { contactAppAction.favorite(contactId = arguments.idText(CONTACT_ID)) }.toJson() },
            McpTool(
                name = "contact_unfavorite",
                description = "Removes the favorite mark from a contact.",
                parameterList = listOf(contactIdParameter),
                kind = McpToolKind.WRITE,
            ) { arguments -> appActionCall { contactAppAction.unfavorite(contactId = arguments.idText(CONTACT_ID)) }.toJson() },
            McpTool(
                name = "contact_delete",
                description = "Deletes a contact. Use contact_restore with the same id to undo.",
                parameterList = listOf(contactIdParameter),
                kind = McpToolKind.DESTRUCTIVE,
            ) { arguments -> appActionCall { contactAppAction.delete(contactId = arguments.idText(CONTACT_ID)) }.toJson() },
            McpTool(
                name = "contact_restore",
                description = "Restores a deleted contact.",
                parameterList = listOf(contactIdParameter),
                kind = McpToolKind.WRITE,
            ) { arguments -> appActionCall { contactAppAction.restore(contactId = arguments.idText(CONTACT_ID)) }.toJson() },
        ) +
            entityTagToolList(
                toolNamePrefix = "contact",
                entityName = "a contact",
                kind = AppActionEntityTagKind.CONTACT,
                entityIdParameter = contactIdParameter,
                entityTagAppAction = entityTagAppAction,
            )

    private suspend fun addContact(arguments: McpToolArguments): JsonElement {
        val birthdayDate = arguments.localDateOrNull(CONTACT_BIRTHDAY)
        val calendar = arguments.contactBirthdayCalendarOrNull()

        if (birthdayDate == null && calendar != null) {
            throw McpToolArgumentException(message = "'$CONTACT_BIRTHDAY_CALENDAR' needs '$CONTACT_BIRTHDAY'.")
        }

        val detail =
            ContactDetail(
                name = arguments.string("name"),
                description = arguments.stringOrNull(DESCRIPTION).orEmpty(),
                height = arguments.contactHeightOrNull(),
                footSize = arguments.contactFootSizeOrNull(),
                birthday = birthdayDate?.let { date -> ContactBirthday(date = date, calendar = calendar ?: ContactBirthdayCalendar.SOLAR) },
                birthYear = arguments.contactBirthYearOrNull(),
                hometown = arguments.stringOrNull(HOMETOWN).orEmpty(),
                howWeMet = arguments.stringOrNull(HOW_WE_MET).orEmpty(),
                phoneNumberList = arguments.stringList(CONTACT_PHONE_NUMBERS).map { number -> ContactPhoneNumber(number = number) },
            )

        return idResult(addContactUseCase(parameter = AddContactUseCase.Parameter(detail = detail, tagIdSet = arguments.uuidSet("tagIds"))).getOrThrow())
    }

    private suspend fun updateContact(arguments: McpToolArguments): JsonElement {
        val contactId = arguments.uuid(CONTACT_ID)
        val current = findContactUseCase.readActive(id = contactId, isDeleted = Contact::isDeleted)?.detail ?: return changedCountResult(count = 0)
        val detail = arguments.updatedDetail(current = current)

        return changedCountResult(updateContactUseCase(parameter = UpdateContactUseCase.Parameter(id = contactId, detail = detail)).getOrThrow())
    }

    private fun McpToolArguments.updatedDetail(current: ContactDetail): ContactDetail {
        val birthday = updatedBirthday(current = current.birthday)

        return ContactDetail(
            name = stringOrNull("name") ?: current.name,
            description = stringOrNull(DESCRIPTION) ?: current.description,
            height = updatedHeight(current = current),
            footSize = updatedFootSize(current = current),
            birthday = birthday,
            birthYear = updatedBirthYear(current = current, birthday = birthday),
            hometown = stringOrNull(HOMETOWN) ?: current.hometown,
            howWeMet = stringOrNull(HOW_WE_MET) ?: current.howWeMet,
            phoneNumberList = updatedPhoneNumberList(current = current),
        )
    }

    private fun McpToolArguments.updatedHeight(current: ContactDetail) =
        when {
            booleanOrNull("clearHeight") == true -> null
            has(CONTACT_HEIGHT) -> contactHeightOrNull()
            else -> current.height
        }

    private fun McpToolArguments.updatedFootSize(current: ContactDetail) =
        when {
            booleanOrNull("clearFootSize") == true -> null
            has(CONTACT_FOOT_SIZE) -> contactFootSizeOrNull()
            else -> current.footSize
        }

    private fun McpToolArguments.updatedBirthYear(
        current: ContactDetail,
        birthday: ContactBirthday?,
    ) = when {
        booleanOrNull("clearBirthYear") == true -> null
        has(CONTACT_BIRTH_YEAR) -> contactBirthYearOrNull()
        current.birthday != null && birthday == null -> null
        else -> current.birthYear
    }

    private fun McpToolArguments.updatedPhoneNumberList(current: ContactDetail): List<ContactPhoneNumber> =
        if (has(CONTACT_PHONE_NUMBERS)) {
            stringList(CONTACT_PHONE_NUMBERS).map { number -> ContactPhoneNumber(number = number) }
        } else {
            current.phoneNumberList
        }

    private fun McpToolArguments.updatedBirthday(current: ContactBirthday?): ContactBirthday? {
        if (booleanOrNull("clearBirthday") == true) return null

        val date = localDateOrNull(CONTACT_BIRTHDAY)
        val calendar = contactBirthdayCalendarOrNull()

        return if (date == null && calendar == null) {
            current
        } else {
            ContactBirthday(
                date = date ?: current?.date ?: throw McpToolArgumentException(message = "'$CONTACT_BIRTHDAY_CALENDAR' needs '$CONTACT_BIRTHDAY' when the contact has no birthday."),
                calendar = calendar ?: current?.calendar ?: ContactBirthdayCalendar.SOLAR,
            )
        }
    }
}
