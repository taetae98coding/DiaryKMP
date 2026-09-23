package io.github.taetae98coding.diary.feature.memo.ui.detail

import androidx.compose.runtime.remember
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasProgressBarRangeInfo
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.paging.compose.collectAsLazyPagingItems
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.core.model.memo.MemoDetail
import io.github.taetae98coding.diary.feature.memo.ui.form.rememberMemoDetailFormState
import io.github.taetae98coding.diary.feature.memo.ui.place.DEFAULT_MAP_DESCRIPTION
import io.github.taetae98coding.diary.feature.memo.ui.tag.tagPagingDataOf
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class MemoDetailScaffoldTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `TC-MEMO-DETAIL-FEATURE-029 메모 내용이 준비되지 않으면 로딩 상태를 표시한다`() {
        setMemoDetailScaffold(uiStateProvider = { MemoDetailUiState.Loading })

        composeRule.onNode(hasProgressBarRangeInfo(ProgressBarRangeInfo.Indeterminate)).assertExists()
        composeRule.onNodeWithText(REMOVED_KOREAN_DEFAULT_TITLE).assertDoesNotExist()
        composeRule.onNodeWithText(REMOVED_DEFAULT_TITLE).assertDoesNotExist()
        composeRule.onAllNodes(hasSetTextAction()).assertCountEquals(0)
        composeRule.onNodeWithContentDescription(DEFAULT_UPDATE_BUTTON_DESCRIPTION).assertDoesNotExist()
        composeRule.onNodeWithContentDescription(DEFAULT_FINISH_BUTTON_DESCRIPTION).assertDoesNotExist()
        composeRule.onNodeWithContentDescription(DEFAULT_DELETE_BUTTON_DESCRIPTION).assertDoesNotExist()
    }

    @Test
    fun `TC-MEMO-DETAIL-FEATURE-001 제목이 조회되면 상단 바에 조회한 제목을 표시한다`() {
        setMemoDetailScaffold(uiStateProvider = { memoDetailUiState(detail = memoDetail(MEMO_TITLE)) })

        composeRule.onNode(hasProgressBarRangeInfo(ProgressBarRangeInfo.Indeterminate)).assertDoesNotExist()
        composeRule.onNodeWithText(MEMO_TITLE).assertExists()
        composeRule.onNodeWithText(REMOVED_DEFAULT_TITLE).assertDoesNotExist()
    }

    @Test
    @Config(qualifiers = "ko")
    fun `한국어 뒤로가기 버튼 이름은 뒤로가기이다`() {
        setMemoDetailScaffold()

        composeRule.onNodeWithContentDescription(KOREAN_NAVIGATE_UP_DESCRIPTION).assert(hasClickAction())
    }

    @Test
    fun `기본 뒤로가기 버튼 이름은 Navigate up이다`() {
        setMemoDetailScaffold()

        composeRule.onNodeWithContentDescription(DEFAULT_NAVIGATE_UP_DESCRIPTION).assert(hasClickAction())
    }

    @Test
    fun `TC-MEMO-LIST-DETAIL-FEATURE-012 목록과 함께 표시되면 뒤로가기 버튼이 표시되지 않는다`() {
        setMemoDetailScaffold(
            uiStateProvider = { memoDetailUiState(detail = memoDetail(MEMO_TITLE)) },
            componentVisibleProvider = { MemoDetailScaffoldComponentVisible(isNavigateUpButtonVisible = false) },
        )

        composeRule.onNodeWithContentDescription(DEFAULT_NAVIGATE_UP_DESCRIPTION).assertDoesNotExist()
        composeRule.onNodeWithText(MEMO_TITLE).assertExists()
    }

    @Test
    fun `TC-MEMO-PLACE-CARD-FEATURE-002 메모 조회가 완료되면 본문에 장소 카드를 표시한다`() {
        setMemoDetailScaffold(uiStateProvider = { memoDetailUiState(detail = memoDetail(MEMO_TITLE)) })

        composeRule.onNodeWithContentDescription(DEFAULT_MAP_DESCRIPTION).assertExists()
    }

    private fun setMemoDetailScaffold(
        uiStateProvider: () -> MemoDetailUiState = { memoDetailUiState() },
        onEvent: (MemoDetailScaffoldEvent) -> Unit = {},
        componentVisibleProvider: () -> MemoDetailScaffoldComponentVisible = { MemoDetailScaffoldComponentVisible() },
    ) {
        composeRule.setContent {
            DiaryTheme {
                val tagPagingData = remember { MutableStateFlow(tagPagingDataOf(emptyList())) }

                MemoDetailScaffold(
                    state = rememberMemoDetailFormState(initialDetail = MemoDetail.EMPTY),
                    tagPagingItems = tagPagingData.collectAsLazyPagingItems(),
                    uiStateProvider = uiStateProvider,
                    onEvent = onEvent,
                    onFormEvent = {},
                    onTagPickerEvent = {},
                    onWebPickerEvent = {},
                    onContactPickerEvent = {},
                    onPlacePickerEvent = {},
                    onGeminiEvent = {},
                    onGeminiDismissRequest = {},
                    componentVisibleProvider = componentVisibleProvider,
                )
            }
        }
    }

    public companion object {
        private const val REMOVED_KOREAN_DEFAULT_TITLE = "메모"
        private const val REMOVED_DEFAULT_TITLE = "Memo"
        private const val KOREAN_NAVIGATE_UP_DESCRIPTION = "뒤로가기"
        private const val DEFAULT_NAVIGATE_UP_DESCRIPTION = "Navigate up"
        private const val MEMO_TITLE = "MemoDetailTitle"
    }
}

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class MemoDetailScaffoldActionTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `기본 환경 완료되지 않은 메모의 완료 버튼 이름은 Finish memo이다`() {
        setMemoDetailScaffold(uiStateProvider = { memoDetailUiState(detail = memoDetail(MEMO_TITLE), isFinished = false) })

        composeRule.onNodeWithContentDescription(DEFAULT_FINISH_BUTTON_DESCRIPTION).assert(hasClickAction())
        composeRule.onNodeWithContentDescription(DEFAULT_RESTART_BUTTON_DESCRIPTION).assertDoesNotExist()
    }

    @Test
    fun `기본 환경 완료된 메모의 완료 버튼 이름은 Restart memo이다`() {
        setMemoDetailScaffold(uiStateProvider = { memoDetailUiState(detail = memoDetail(MEMO_TITLE), isFinished = true) })

        composeRule.onNodeWithContentDescription(DEFAULT_RESTART_BUTTON_DESCRIPTION).assert(hasClickAction())
        composeRule.onNodeWithContentDescription(DEFAULT_FINISH_BUTTON_DESCRIPTION).assertDoesNotExist()
    }

    @Test
    @Config(qualifiers = "ko")
    fun `한국어 완료되지 않은 메모의 완료 버튼 이름은 메모 완료이다`() {
        setMemoDetailScaffold(uiStateProvider = { memoDetailUiState(detail = memoDetail(MEMO_TITLE), isFinished = false) })

        composeRule.onNodeWithContentDescription(KOREAN_FINISH_BUTTON_DESCRIPTION).assert(hasClickAction())
    }

    @Test
    @Config(qualifiers = "ko")
    fun `한국어 완료된 메모의 완료 버튼 이름은 메모 다시 시작이다`() {
        setMemoDetailScaffold(uiStateProvider = { memoDetailUiState(detail = memoDetail(MEMO_TITLE), isFinished = true) })

        composeRule.onNodeWithContentDescription(KOREAN_RESTART_BUTTON_DESCRIPTION).assert(hasClickAction())
    }

    @Test
    fun `기본 환경 삭제 버튼 이름은 Delete memo이다`() {
        setMemoDetailScaffold(uiStateProvider = { memoDetailUiState(detail = memoDetail(MEMO_TITLE), isFinished = false) })

        composeRule.onNodeWithContentDescription(DEFAULT_DELETE_BUTTON_DESCRIPTION).assert(hasClickAction())
    }

    @Test
    @Config(qualifiers = "ko")
    fun `한국어 삭제 버튼 이름은 메모 삭제이다`() {
        setMemoDetailScaffold(uiStateProvider = { memoDetailUiState(detail = memoDetail(MEMO_TITLE), isFinished = false) })

        composeRule.onNodeWithContentDescription(KOREAN_DELETE_BUTTON_DESCRIPTION).assert(hasClickAction())
    }

    private fun setMemoDetailScaffold(uiStateProvider: () -> MemoDetailUiState = { memoDetailUiState() }) {
        composeRule.setContent {
            DiaryTheme {
                val tagPagingData = remember { MutableStateFlow(tagPagingDataOf(emptyList())) }

                MemoDetailScaffold(
                    state = rememberMemoDetailFormState(initialDetail = MemoDetail.EMPTY),
                    tagPagingItems = tagPagingData.collectAsLazyPagingItems(),
                    uiStateProvider = uiStateProvider,
                    onEvent = {},
                    onFormEvent = {},
                    onTagPickerEvent = {},
                    onWebPickerEvent = {},
                    onContactPickerEvent = {},
                    onPlacePickerEvent = {},
                    onGeminiEvent = {},
                    onGeminiDismissRequest = {},
                )
            }
        }
    }

    public companion object {
        private const val KOREAN_FINISH_BUTTON_DESCRIPTION = "메모 완료"
        private const val KOREAN_RESTART_BUTTON_DESCRIPTION = "메모 다시 시작"
        private const val KOREAN_DELETE_BUTTON_DESCRIPTION = "메모 삭제"
    }
}

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class MemoDetailScaffoldInProgressTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `TC-MEMO-DETAIL-FEATURE-028 완료를 처리하는 동안 완료 버튼이 진행 표시로 바뀐다`() {
        setMemoDetailScaffold(uiStateProvider = { memoDetailUiState(detail = memoDetail(MEMO_TITLE), isFinishInProgress = true) })

        composeRule.onNode(hasProgressBarRangeInfo(ProgressBarRangeInfo.Indeterminate)).assertExists()
        composeRule.onNodeWithContentDescription(DEFAULT_FINISH_BUTTON_DESCRIPTION).assertDoesNotExist()
        composeRule.onNodeWithContentDescription(DEFAULT_DELETE_BUTTON_DESCRIPTION).assert(hasClickAction())
    }

    @Test
    fun `TC-MEMO-DETAIL-FEATURE-028 삭제를 처리하는 동안 삭제 버튼이 진행 표시로 바뀐다`() {
        setMemoDetailScaffold(uiStateProvider = { memoDetailUiState(detail = memoDetail(MEMO_TITLE), isDeleteInProgress = true) })

        composeRule.onNode(hasProgressBarRangeInfo(ProgressBarRangeInfo.Indeterminate)).assertExists()
        composeRule.onNodeWithContentDescription(DEFAULT_DELETE_BUTTON_DESCRIPTION).assertDoesNotExist()
        composeRule.onNodeWithContentDescription(DEFAULT_FINISH_BUTTON_DESCRIPTION).assertIsOff()
    }

    private fun setMemoDetailScaffold(uiStateProvider: () -> MemoDetailUiState = { memoDetailUiState() }) {
        composeRule.setContent {
            DiaryTheme {
                val tagPagingData = remember { MutableStateFlow(tagPagingDataOf(emptyList())) }

                MemoDetailScaffold(
                    state = rememberMemoDetailFormState(initialDetail = MemoDetail.EMPTY),
                    tagPagingItems = tagPagingData.collectAsLazyPagingItems(),
                    uiStateProvider = uiStateProvider,
                    onEvent = {},
                    onFormEvent = {},
                    onTagPickerEvent = {},
                    onWebPickerEvent = {},
                    onContactPickerEvent = {},
                    onPlacePickerEvent = {},
                    onGeminiEvent = {},
                    onGeminiDismissRequest = {},
                )
            }
        }
    }
}
