package io.github.taetae98coding.diary.feature.contact.ui.home

import io.github.taetae98coding.diary.core.model.contact.ContactFilterExistence
import io.github.taetae98coding.diary.core.model.list.ListSort
import kotlin.uuid.Uuid

internal sealed interface ContactHomeScaffoldEvent {
    data object ClickNavigateUp : ContactHomeScaffoldEvent

    data object ClickAdd : ContactHomeScaffoldEvent

    data object ClickSort : ContactHomeScaffoldEvent

    data class SelectSort(
        val sort: ListSort,
    ) : ContactHomeScaffoldEvent

    data object ClickFilter : ContactHomeScaffoldEvent

    data class SetBirthdayExistence(
        val existence: ContactFilterExistence,
    ) : ContactHomeScaffoldEvent

    data class SetBirthYearExistence(
        val existence: ContactFilterExistence,
    ) : ContactHomeScaffoldEvent

    data class SetPhoneNumberExistence(
        val existence: ContactFilterExistence,
    ) : ContactHomeScaffoldEvent

    data object Refresh : ContactHomeScaffoldEvent

    data class ClickContact(
        val id: Uuid,
    ) : ContactHomeScaffoldEvent

    data class DeleteContact(
        val id: Uuid,
    ) : ContactHomeScaffoldEvent
}
