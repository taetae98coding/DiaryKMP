package io.github.taetae98coding.diary.feature.memo.ui.contact

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.paging.PagingData
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import io.github.taetae98coding.diary.compose.core.animation.DiaryCrossfade
import io.github.taetae98coding.diary.compose.core.dialog.DiaryPickerAddButton
import io.github.taetae98coding.diary.compose.core.dialog.DiaryPickerDialog
import io.github.taetae98coding.diary.compose.core.dialog.DiaryPickerEmptyBox
import io.github.taetae98coding.diary.compose.core.dialog.DiaryPickerSearchField
import io.github.taetae98coding.diary.compose.core.paging.isLoadedEmpty
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
    queryState: TextFieldState = rememberTextFieldState(),
    contactPagingItems: LazyPagingItems<Contact> = remember { flowOf(PagingData.empty<Contact>()) }.collectAsLazyPagingItems(),
    uiStateProvider: () -> MemoContactInputUiState = { MemoContactInputUiState() },
) {
    DiaryPickerDialog(
        title = stringResource(Res.string.memo_contact_picker_title),
        onDismissRequest = onDismissRequest,
        modifier = modifier,
    ) {
        val isSearchEmpty by remember(queryState, contactPagingItems) {
            derivedStateOf { queryState.text.isNotBlank() && contactPagingItems.isLoadedEmpty() }
        }

        Column(modifier = Modifier.fillMaxWidth()) {
            DiaryPickerSearchField(
                placeholder = stringResource(Res.string.memo_contact_picker_search_placeholder),
                modifier = Modifier.fillMaxWidth(),
                state = queryState,
            )
            Spacer(modifier = Modifier.height(DiaryTheme.dimens.componentSpacing))
            DiaryCrossfade(
                targetState = isSearchEmpty,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .weight(weight = 1F, fill = false)
                        .height(DiaryTheme.dimens.pickerListHeight),
            ) { isEmpty ->
                if (isEmpty) {
                    DiaryPickerEmptyBox(
                        title = stringResource(Res.string.memo_contact_picker_search_empty_title),
                        description = stringResource(Res.string.memo_contact_picker_search_empty_description),
                    )
                } else {
                    MemoContactPickerList(
                        onEvent = onEvent,
                        contactPagingItems = contactPagingItems,
                        uiStateProvider = uiStateProvider,
                        modifier = Modifier.fillMaxSize(),
                    )
                }
            }
            DiaryPickerAddButton(
                onClick = { onEvent(MemoContactPickerEvent.ClickAdd) },
                label = stringResource(Res.string.memo_contact_picker_add_label),
                actionLabel = stringResource(Res.string.memo_contact_add_action),
                modifier = Modifier.fillMaxWidth(),
            )
        }
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
