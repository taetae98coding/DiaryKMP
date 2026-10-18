package io.github.taetae98coding.diary.feature.memo.ui.contact

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.paging.PagingData
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import io.github.taetae98coding.diary.compose.core.dialog.DiaryPagingPickerDialog
import io.github.taetae98coding.diary.compose.core.dialog.DiaryPagingPickerText
import io.github.taetae98coding.diary.compose.core.dialog.DiaryPickerSearchFieldState
import io.github.taetae98coding.diary.compose.core.dialog.rememberDiaryPickerSearchFieldState
import io.github.taetae98coding.diary.compose.core.preview.ScreenPreview
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.core.model.contact.Contact
import io.github.taetae98coding.diary.feature.memo.ui.Res
import io.github.taetae98coding.diary.feature.memo.ui.memo_contact_add_action
import io.github.taetae98coding.diary.feature.memo.ui.memo_contact_picker_add_label
import io.github.taetae98coding.diary.feature.memo.ui.memo_contact_picker_search_empty_description
import io.github.taetae98coding.diary.feature.memo.ui.memo_contact_picker_search_empty_title
import io.github.taetae98coding.diary.feature.memo.ui.memo_contact_picker_search_placeholder
import io.github.taetae98coding.diary.feature.memo.ui.memo_contact_picker_title
import io.github.taetae98coding.diary.feature.memo.ui.previewContact
import kotlinx.coroutines.flow.flowOf
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun MemoContactPickerDialog(
    onDismissRequest: () -> Unit,
    onEvent: (MemoContactPickerEvent) -> Unit,
    modifier: Modifier = Modifier,
    searchFieldState: DiaryPickerSearchFieldState = rememberDiaryPickerSearchFieldState(),
    contactPagingItems: LazyPagingItems<Contact> = remember { flowOf(PagingData.empty<Contact>()) }.collectAsLazyPagingItems(),
    uiStateProvider: () -> MemoContactInputUiState = { MemoContactInputUiState() },
) {
    DiaryPagingPickerDialog(
        text =
            DiaryPagingPickerText(
                title = stringResource(Res.string.memo_contact_picker_title),
                searchPlaceholder = stringResource(Res.string.memo_contact_picker_search_placeholder),
                searchEmptyTitle = stringResource(Res.string.memo_contact_picker_search_empty_title),
                searchEmptyDescription = stringResource(Res.string.memo_contact_picker_search_empty_description),
                addLabel = stringResource(Res.string.memo_contact_picker_add_label),
                addActionLabel = stringResource(Res.string.memo_contact_add_action),
            ),
        onAddClick = { onEvent(MemoContactPickerEvent.ClickAdd) },
        onDismissRequest = onDismissRequest,
        itemKey = { contact -> contact.id },
        modifier = modifier,
        searchFieldState = searchFieldState,
        pagingItems = contactPagingItems,
        isSearchFocusRequested = true,
    ) { contact, itemModifier ->
        MemoContactPickerRow(
            onEvent = onEvent,
            modifier = itemModifier,
            contact = contact,
            isSelected = contact != null && uiStateProvider().selectedContactList.any { selectedContact -> selectedContact.id == contact.id },
        )
    }
}

@ScreenPreview
@Composable
private fun MemoContactPickerDialogPreview() {
    val contactList = remember { listOf(previewContact(name = "김철수", phoneNumber = "010-1234-5678")) }
    val contactPagingData = remember(contactList) { flowOf(PagingData.from(contactList)) }

    DiaryTheme {
        MemoContactPickerDialog(
            onDismissRequest = {},
            onEvent = {},
            contactPagingItems = contactPagingData.collectAsLazyPagingItems(),
            uiStateProvider = { MemoContactInputUiState(selectedContactList = contactList) },
        )
    }
}
