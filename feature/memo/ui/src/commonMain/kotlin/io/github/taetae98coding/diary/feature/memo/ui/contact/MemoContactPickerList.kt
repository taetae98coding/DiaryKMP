package io.github.taetae98coding.diary.feature.memo.ui.contact

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.paging.PagingData
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import io.github.taetae98coding.diary.compose.core.preview.ComponentPreview
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.core.model.contact.Contact
import io.github.taetae98coding.diary.feature.memo.ui.previewContact
import kotlinx.coroutines.flow.flowOf

internal const val MEMO_CONTACT_PICKER_LIST_TEST_TAG: String = "MemoContactPickerList"

@Composable
internal fun MemoContactPickerList(
    onEvent: (MemoContactPickerEvent) -> Unit,
    modifier: Modifier = Modifier,
    contactPagingItems: LazyPagingItems<Contact> = remember { flowOf(PagingData.empty<Contact>()) }.collectAsLazyPagingItems(),
    uiStateProvider: () -> MemoContactInputUiState = { MemoContactInputUiState() },
) {
    LazyColumn(modifier = modifier.testTag(MEMO_CONTACT_PICKER_LIST_TEST_TAG)) {
        items(
            count = contactPagingItems.itemCount,
            key = contactPagingItems.itemKey { contact -> contact.id },
        ) { index ->
            val contact = contactPagingItems[index]
            val uiState = uiStateProvider()

            MemoContactPickerRow(
                onEvent = onEvent,
                contact = contact,
                isSelected = contact != null && uiState.selectedContactList.any { selectedContact -> selectedContact.id == contact.id },
                modifier =
                    Modifier
                        .animateItem()
                        .fillMaxWidth(),
            )
        }
    }
}

@ComponentPreview
@Composable
private fun MemoContactPickerListPreview() {
    val contactList = remember { listOf(previewContact(name = "김철수", phoneNumber = "010-1234-5678")) }
    val contactPagingData = remember(contactList) { flowOf(PagingData.from(contactList)) }

    DiaryTheme {
        Surface {
            MemoContactPickerList(
                onEvent = {},
                contactPagingItems = contactPagingData.collectAsLazyPagingItems(),
                uiStateProvider = { MemoContactInputUiState(selectedContactList = contactList) },
            )
        }
    }
}
