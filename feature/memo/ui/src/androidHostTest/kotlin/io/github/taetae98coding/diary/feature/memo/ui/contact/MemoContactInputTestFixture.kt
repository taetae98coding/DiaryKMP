package io.github.taetae98coding.diary.feature.memo.ui.contact

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isDialog
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.height
import androidx.paging.PagingData
import androidx.paging.compose.collectAsLazyPagingItems
import io.github.taetae98coding.diary.compose.core.dialog.DialogState
import io.github.taetae98coding.diary.compose.core.dialog.rememberDiaryPickerSearchFieldState
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.core.model.contact.Contact
import io.github.taetae98coding.diary.core.model.contact.ContactDetail
import io.github.taetae98coding.diary.core.model.contact.ContactPhoneNumber
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlin.time.Instant
import kotlin.uuid.Uuid

internal const val FIRST_CONTACT_NAME = "Kim"
internal const val FIRST_CONTACT_PHONE_NUMBER = "010-1234-5678"
internal const val SECOND_CONTACT_NAME = "Lee"
internal const val SECOND_CONTACT_PHONE_NUMBER = "010-9876-5432"

internal const val DEFAULT_CONTACT_SELECT_LABEL = "Select contact"
internal const val DEFAULT_CONTACT_PICKER_TITLE = "Select Contact"
internal const val DEFAULT_CONTACT_PICKER_ADD_LABEL = "Add contact"
internal const val DEFAULT_CONTACT_PICKER_SEARCH_PLACEHOLDER = "Search contacts"
internal const val DEFAULT_CONTACT_PICKER_SEARCH_EMPTY_TITLE = "No search results"
internal const val DEFAULT_CONTACT_PICKER_SEARCH_EMPTY_DESCRIPTION = "Try a different search query."
internal const val DEFAULT_CONTACT_DETAIL_ACTION = "Open contact detail"

internal const val KOREAN_CONTACT_SELECT_LABEL: String = "연락처 선택"
internal const val KOREAN_CONTACT_DETAIL_ACTION: String = "연락처 상세 보기"
internal const val KOREAN_CONTACT_PICKER_TITLE: String = KOREAN_CONTACT_SELECT_LABEL
internal const val KOREAN_CONTACT_PICKER_ADD_LABEL: String = "연락처 추가"
internal const val KOREAN_CONTACT_PICKER_SEARCH_PLACEHOLDER: String = "연락처 검색"
internal const val KOREAN_CONTACT_PICKER_SEARCH_EMPTY_TITLE: String = "검색 결과가 없습니다"
internal const val KOREAN_CONTACT_PICKER_SEARCH_EMPTY_DESCRIPTION: String = "다른 검색어로 찾아보세요"

internal const val MEMO_CONTACT_INPUT_TAG: String = "MemoContactInput"

internal fun testContact(
    name: String,
    phoneNumber: String? = null,
    isDeleted: Boolean = false,
    isFavorite: Boolean = false,
): Contact =
    Contact(
        id = Uuid.random(),
        detail =
            ContactDetail(
                name = name,
                description = "",
                height = null,
                footSize = null,
                birthday = null,
                hometown = "",
                phoneNumberList = phoneNumber?.let { number -> listOf(ContactPhoneNumber(number = number)) }.orEmpty(),
            ),
        isFavorite = isFavorite,
        isDeleted = isDeleted,
        updatedAt = Instant.DISTANT_PAST,
        createdAt = Instant.DISTANT_PAST,
    )

internal fun screenTestContactViewModel(
    uiState: StateFlow<MemoContactInputUiState> = MutableStateFlow(MemoContactInputUiState()),
    contactPagingDataFlow: Flow<PagingData<Contact>> = MutableStateFlow(contactPagingDataOf(emptyList())),
): MemoContactViewModel {
    val viewModel = mockk<MemoContactViewModel>(relaxed = true)
    every { viewModel.uiState } returns uiState
    every { viewModel.contactPagingData } returns contactPagingDataFlow
    return viewModel
}

internal fun ComposeContentTestRule.setMemoContactInput(
    uiState: MemoContactInputUiState = MemoContactInputUiState(),
    onContactClick: (Uuid) -> Unit = {},
    onAddClick: () -> Unit = {},
) {
    setContent {
        DiaryTheme {
            MemoContactInput(
                uiStateProvider = { uiState },
                onContactClick = onContactClick,
                onAddClick = onAddClick,
                modifier = Modifier.testTag(MEMO_CONTACT_INPUT_TAG),
            )
        }
    }
}

/**
 * 선택한 연락처를 테스트에서 바꿀 수 있도록 상태를 끌어올려 [MemoContactInput]을 배치하고, 선택을 바꾸는 함수를 돌려준다.
 */
internal fun ComposeContentTestRule.setMemoContactInputWithSelection(): (List<Contact>) -> Unit {
    var uiState by mutableStateOf(MemoContactInputUiState())

    setContent {
        DiaryTheme {
            MemoContactInput(
                uiStateProvider = { uiState },
                onContactClick = {},
                onAddClick = {},
                modifier = Modifier.testTag(MEMO_CONTACT_INPUT_TAG),
            )
        }
    }

    return { selectedContactList ->
        uiState = uiState.copy(selectedContactList = selectedContactList)
        waitForIdle()
    }
}

internal fun ComposeContentTestRule.setMemoContactPickerDialog(
    contactList: List<Contact> = emptyList(),
    uiState: MemoContactInputUiState = MemoContactInputUiState(),
    contactPagingData: PagingData<Contact> = contactPagingDataOf(contactList),
    contactPagingDataFlow: Flow<PagingData<Contact>> = MutableStateFlow(contactPagingData),
    query: String = "",
    onDismissRequest: () -> Unit = {},
    onContactSelect: (Uuid) -> Unit = {},
    onContactUnselect: (Uuid) -> Unit = {},
    onContactAdd: () -> Unit = {},
) {
    setContent {
        DiaryTheme {
            MemoContactPickerDialog(
                searchFieldState = rememberDiaryPickerSearchFieldState(initialText = query),
                contactPagingItems = remember(contactPagingDataFlow) { contactPagingDataFlow }.collectAsLazyPagingItems(),
                uiStateProvider = { uiState },
                onDismissRequest = onDismissRequest,
                onEvent = { event ->
                    when (event) {
                        is MemoContactPickerEvent.Select -> onContactSelect(event.id)
                        is MemoContactPickerEvent.Unselect -> onContactUnselect(event.id)
                        is MemoContactPickerEvent.ClickAdd -> onContactAdd()
                        is MemoContactPickerEvent.ChangeQuery -> Unit
                    }
                },
            )
        }
    }
}

internal fun ComposeContentTestRule.setMemoContactPickerDialogHost(
    dialogState: DialogState = DialogState(isVisible = true),
    contactList: List<Contact> = emptyList(),
    contactPagingDataFlow: Flow<PagingData<Contact>> = MutableStateFlow(contactPagingDataOf(contactList)),
    uiState: MemoContactInputUiState = MemoContactInputUiState(),
    onQueryChange: (String) -> Unit = {},
    onContactSelect: (Uuid) -> Unit = {},
    onContactAdd: () -> Unit = {},
) {
    setContent {
        DiaryTheme {
            MemoContactPickerDialogHost(
                dialogState = dialogState,
                onEvent = { event ->
                    when (event) {
                        is MemoContactPickerEvent.Select -> onContactSelect(event.id)
                        is MemoContactPickerEvent.ClickAdd -> onContactAdd()
                        is MemoContactPickerEvent.ChangeQuery -> onQueryChange(event.query)
                        is MemoContactPickerEvent.Unselect -> Unit
                    }
                },
                contactPagingItems = remember(contactPagingDataFlow) { contactPagingDataFlow }.collectAsLazyPagingItems(),
                uiStateProvider = { uiState },
            )
        }
    }
}

internal fun ComposeContentTestRule.contactDialogNodeWithText(text: String): SemanticsNodeInteraction = onNode(hasText(text) and hasAnyAncestor(isDialog()))

internal fun ComposeContentTestRule.contactDialogSearchField(): SemanticsNodeInteraction = onNode(hasSetTextAction() and hasAnyAncestor(isDialog()))

internal fun ComposeContentTestRule.memoContactInputHeight(): Dp = onNodeWithTag(MEMO_CONTACT_INPUT_TAG).getUnclippedBoundsInRoot().height

internal fun hasContactClickLabel(label: String): SemanticsMatcher =
    SemanticsMatcher("has click label $label") { node ->
        node.config.getOrNull(SemanticsActions.OnClick)?.label == label
    }
