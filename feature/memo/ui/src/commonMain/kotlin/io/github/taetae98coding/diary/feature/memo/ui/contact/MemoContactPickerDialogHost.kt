package io.github.taetae98coding.diary.feature.memo.ui.contact

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.paging.PagingData
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import io.github.taetae98coding.diary.compose.core.dialog.DialogState
import io.github.taetae98coding.diary.compose.core.dialog.rememberDialogState
import io.github.taetae98coding.diary.compose.core.dialog.rememberDiaryPickerSearchFieldState
import io.github.taetae98coding.diary.compose.core.effect.DiarySearchQueryEffect
import io.github.taetae98coding.diary.compose.core.preview.ScreenPreview
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.core.model.contact.Contact
import io.github.taetae98coding.diary.feature.memo.ui.previewContact
import kotlinx.coroutines.flow.flowOf

@Composable
internal fun MemoContactPickerDialogHost(
    dialogState: DialogState,
    onEvent: (MemoContactPickerEvent) -> Unit,
    contactPagingItems: LazyPagingItems<Contact> = remember { flowOf(PagingData.empty<Contact>()) }.collectAsLazyPagingItems(),
    uiStateProvider: () -> MemoContactInputUiState = { MemoContactInputUiState() },
) {
    if (!dialogState.isVisible) return

    val searchFieldState = rememberDiaryPickerSearchFieldState()

    val hide = {
        onEvent(MemoContactPickerEvent.ChangeQuery(query = ""))
        dialogState.hide()
    }

    DiarySearchQueryEffect(
        queryState = searchFieldState.textFieldState,
        onQueryChange = { query -> onEvent(MemoContactPickerEvent.ChangeQuery(query = query)) },
    )

    MemoContactPickerDialog(
        onDismissRequest = hide,
        onEvent = { event ->
            if (event is MemoContactPickerEvent.ClickAdd) hide()
            onEvent(event)
        },
        searchFieldState = searchFieldState,
        contactPagingItems = contactPagingItems,
        uiStateProvider = uiStateProvider,
    )
}

@ScreenPreview
@Composable
private fun MemoContactPickerDialogHostPreview() {
    val contactList = remember { listOf(previewContact(name = "김철수", phoneNumber = "010-1234-5678")) }
    val contactPagingData = remember(contactList) { flowOf(PagingData.from(contactList)) }

    DiaryTheme {
        MemoContactPickerDialogHost(
            dialogState = rememberDialogState().apply { show() },
            onEvent = {},
            contactPagingItems = contactPagingData.collectAsLazyPagingItems(),
            uiStateProvider = { MemoContactInputUiState(selectedContactList = contactList) },
        )
    }
}
