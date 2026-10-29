package io.github.taetae98coding.diary.feature.memo.ui.web

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
import com.navercorp.fixturemonkey.FixtureMonkey
import io.github.taetae98coding.diary.compose.core.dialog.DialogState
import io.github.taetae98coding.diary.compose.core.dialog.rememberDiaryPickerSearchFieldState
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.core.model.web.Web
import io.github.taetae98coding.diary.core.testing.web.web
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlin.uuid.Uuid

internal const val WIKI_WEB_TITLE = "Wiki"
internal const val WIKI_WEB_URL = "https://wiki.example.com"
internal const val DOCS_WEB_TITLE = "Docs"
internal const val DOCS_WEB_URL = "https://developer.android.com"
internal const val DEFAULT_WEB_SELECT_LABEL = "Select web"
internal const val DEFAULT_WEB_PICKER_TITLE = "Select Web"
internal const val DEFAULT_WEB_PICKER_ADD_LABEL = "Add web"
internal const val DEFAULT_WEB_PICKER_SEARCH_PLACEHOLDER = "Search webs"
internal const val DEFAULT_WEB_PICKER_SEARCH_EMPTY_TITLE = "No search results"
internal const val DEFAULT_WEB_DETAIL_ACTION = "Open web detail"

private val fixtureMonkey: FixtureMonkey = diaryFixtureMonkey()

internal fun testWeb(
    title: String,
    url: String = "https://example.com/${title.lowercase()}",
    isDeleted: Boolean = false,
): Web =
    fixtureMonkey.web(isDeleted = isDeleted).let { web ->
        web.copy(detail = web.detail.copy(title = title, url = url))
    }

internal fun screenTestWebViewModel(
    uiState: StateFlow<MemoWebInputUiState> = MutableStateFlow(MemoWebInputUiState()),
    webPagingDataFlow: Flow<PagingData<Web>> = MutableStateFlow(webPagingDataOf(emptyList())),
): MemoWebViewModel {
    val viewModel = mockk<MemoWebViewModel>(relaxed = true)
    every { viewModel.uiState } returns uiState
    every { viewModel.webPagingData } returns webPagingDataFlow
    every { viewModel.selectableWebPagingData } returns webPagingDataFlow
    return viewModel
}

internal const val MEMO_WEB_INPUT_TAG: String = "MemoWebInput"
internal const val KOREAN_WEB_SELECT_LABEL: String = "웹 선택"
internal const val KOREAN_WEB_DETAIL_ACTION: String = "웹 상세 보기"
internal const val KOREAN_WEB_PICKER_TITLE: String = KOREAN_WEB_SELECT_LABEL
internal const val KOREAN_WEB_PICKER_ADD_LABEL: String = "웹 추가"
internal const val KOREAN_WEB_PICKER_SEARCH_PLACEHOLDER: String = "웹 검색"
internal const val KOREAN_WEB_PICKER_SEARCH_EMPTY_TITLE: String = "검색 결과가 없습니다"
internal const val KOREAN_WEB_PICKER_SEARCH_EMPTY_DESCRIPTION: String = "다른 검색어로 찾아보세요"
internal const val DEFAULT_WEB_PICKER_SEARCH_EMPTY_DESCRIPTION: String = "Try a different search query."

internal fun ComposeContentTestRule.setMemoWebInput(
    uiState: MemoWebInputUiState = MemoWebInputUiState(),
    onWebClick: (Uuid) -> Unit = {},
    onAddClick: () -> Unit = {},
) {
    setContent {
        DiaryTheme {
            MemoWebInput(
                uiStateProvider = { uiState },
                onWebClick = onWebClick,
                onAddClick = onAddClick,
                modifier = Modifier.testTag(MEMO_WEB_INPUT_TAG),
            )
        }
    }
}

/**
 * 선택한 웹 항목을 테스트에서 바꿀 수 있도록 상태를 끌어올려 [MemoWebInput]을 배치하고, 선택을 바꾸는 함수를 돌려준다.
 */
internal fun ComposeContentTestRule.setMemoWebInputWithSelection(): (List<Web>) -> Unit {
    var uiState by mutableStateOf(MemoWebInputUiState())

    setContent {
        DiaryTheme {
            MemoWebInput(
                uiStateProvider = { uiState },
                onWebClick = {},
                onAddClick = {},
                modifier = Modifier.testTag(MEMO_WEB_INPUT_TAG),
            )
        }
    }

    return { selectedWebList ->
        uiState = uiState.copy(selectedWebList = selectedWebList)
        waitForIdle()
    }
}

internal fun ComposeContentTestRule.setMemoWebPickerDialog(
    webList: List<Web> = emptyList(),
    uiState: MemoWebInputUiState = MemoWebInputUiState(),
    webPagingData: PagingData<Web> = webPagingDataOf(webList),
    webPagingDataFlow: Flow<PagingData<Web>> = MutableStateFlow(webPagingData),
    query: String = "",
    onDismissRequest: () -> Unit = {},
    onWebSelect: (Uuid) -> Unit = {},
    onWebUnselect: (Uuid) -> Unit = {},
    onWebAdd: () -> Unit = {},
) {
    setContent {
        DiaryTheme {
            MemoWebPickerDialog(
                searchFieldState = rememberDiaryPickerSearchFieldState(initialText = query),
                webPagingItems = remember(webPagingDataFlow) { webPagingDataFlow }.collectAsLazyPagingItems(),
                uiStateProvider = { uiState },
                onDismissRequest = onDismissRequest,
                onEvent = { event ->
                    when (event) {
                        is MemoWebPickerEvent.Select -> onWebSelect(event.id)
                        is MemoWebPickerEvent.Unselect -> onWebUnselect(event.id)
                        is MemoWebPickerEvent.ClickAdd -> onWebAdd()
                        is MemoWebPickerEvent.ChangeQuery -> Unit
                    }
                },
            )
        }
    }
}

internal fun ComposeContentTestRule.setMemoWebPickerDialogHost(
    dialogState: DialogState = DialogState(isVisible = true),
    webList: List<Web> = emptyList(),
    webPagingDataFlow: Flow<PagingData<Web>> = MutableStateFlow(webPagingDataOf(webList)),
    uiState: MemoWebInputUiState = MemoWebInputUiState(),
    onQueryChange: (String) -> Unit = {},
    onWebSelect: (Uuid) -> Unit = {},
    onWebAdd: () -> Unit = {},
) {
    setContent {
        DiaryTheme {
            MemoWebPickerDialogHost(
                dialogState = dialogState,
                onEvent = { event ->
                    when (event) {
                        is MemoWebPickerEvent.Select -> onWebSelect(event.id)
                        is MemoWebPickerEvent.ClickAdd -> onWebAdd()
                        is MemoWebPickerEvent.ChangeQuery -> onQueryChange(event.query)
                        is MemoWebPickerEvent.Unselect -> Unit
                    }
                },
                webPagingItems = remember(webPagingDataFlow) { webPagingDataFlow }.collectAsLazyPagingItems(),
                uiStateProvider = { uiState },
            )
        }
    }
}

internal fun ComposeContentTestRule.webDialogNodeWithText(text: String): SemanticsNodeInteraction = onNode(hasText(text) and hasAnyAncestor(isDialog()))

internal fun ComposeContentTestRule.webDialogSearchField(): SemanticsNodeInteraction = onNode(hasSetTextAction() and hasAnyAncestor(isDialog()))

internal fun ComposeContentTestRule.memoWebInputHeight(): Dp = onNodeWithTag(MEMO_WEB_INPUT_TAG).getUnclippedBoundsInRoot().height

internal fun hasWebClickLabel(label: String): SemanticsMatcher =
    SemanticsMatcher("has click label $label") { node ->
        node.config.getOrNull(SemanticsActions.OnClick)?.label == label
    }
