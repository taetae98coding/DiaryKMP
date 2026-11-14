package io.github.taetae98coding.diary.feature.contact.ui.add

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.compose.collectAsLazyPagingItems
import io.github.taetae98coding.diary.compose.core.effect.RequestFocusEffect
import io.github.taetae98coding.diary.core.model.contact.ContactBirthday
import io.github.taetae98coding.diary.core.model.contact.ContactBirthdayCalendar
import io.github.taetae98coding.diary.core.model.contact.ContactDetail
import io.github.taetae98coding.diary.core.model.contact.ContactPhoneNumber
import io.github.taetae98coding.diary.feature.contact.api.ContactAddNavKey
import io.github.taetae98coding.diary.feature.contact.ui.form.handleContactFormEvent
import io.github.taetae98coding.diary.feature.contact.ui.form.rememberContactAddFormState
import io.github.taetae98coding.diary.feature.core.tag.EntityAddTagViewModel
import io.github.taetae98coding.diary.feature.core.tag.EntityTagAddedResultEffect
import io.github.taetae98coding.diary.feature.core.tag.handleEntityTagPickerEvent
import kotlin.uuid.Uuid

@Composable
internal fun ContactAddScreen(
    navigateUp: () -> Unit,
    navigateToTagAdd: () -> Unit,
    navigateToTagDetail: (Uuid) -> Unit,
    tagAddRequestKey: Uuid,
    addedResultRequestKey: Uuid?,
    initialDetail: ContactDetail,
    componentVisibleProvider: () -> ContactAddScaffoldComponentVisible,
    addViewModel: ContactAddViewModel,
    tagViewModel: EntityAddTagViewModel,
    modifier: Modifier = Modifier,
) {
    val state = rememberContactAddFormState(initialDetail = initialDetail)
    val uiState by addViewModel.uiState.collectAsStateWithLifecycle()
    val tagUiState by tagViewModel.uiState.collectAsStateWithLifecycle()
    val tagPagingItems = tagViewModel.tagPagingData.collectAsLazyPagingItems()
    val selectableTagPagingItems = tagViewModel.selectableTagPagingData.collectAsLazyPagingItems()

    RequestFocusEffect(focusRequester = state.nameState.focusRequester)
    EntityTagAddedResultEffect(
        requestKey = tagAddRequestKey,
        onTagAdded = tagViewModel::add,
    )
    ContactAddScreenEffect(
        addedResultRequestKey = addedResultRequestKey,
        effect = addViewModel.effect,
        state = state,
    )

    ContactAddScaffold(
        onEvent = { event ->
            when (event) {
                is ContactAddScaffoldEvent.ClickNavigateUp -> navigateUp()

                is ContactAddScaffoldEvent.ClickAdd ->
                    addViewModel.add(
                        detail = state.detail,
                        tagIdSet = tagViewModel.selectionUiState.value.tagIdSet,
                    )
            }
        },
        onFormEvent = { event ->
            handleContactFormEvent(
                event = event,
                state = state,
                selectableTagPagingItems = selectableTagPagingItems,
                navigateToTagAdd = navigateToTagAdd,
                navigateToTagDetail = navigateToTagDetail,
            )
        },
        onTagPickerEvent = { event -> handleEntityTagPickerEvent(event = event, tagViewModel = tagViewModel, navigateToTagAdd = navigateToTagAdd) },
        modifier = modifier,
        state = state,
        tagPagingItems = tagPagingItems,
        uiStateProvider = { uiState },
        tagUiStateProvider = { tagUiState },
        componentVisibleProvider = componentVisibleProvider,
    )
}

internal fun ContactAddNavKey.initialDetail(): ContactDetail =
    ContactDetail.EMPTY.copy(
        name = initialName,
        description = initialDescription,
        birthday = initialBirthday?.let { date -> ContactBirthday(date = date, calendar = ContactBirthdayCalendar.SOLAR) },
        birthYear = initialBirthday?.year,
        phoneNumberList = initialPhoneNumberList.map { number -> ContactPhoneNumber(number = number) },
    )
