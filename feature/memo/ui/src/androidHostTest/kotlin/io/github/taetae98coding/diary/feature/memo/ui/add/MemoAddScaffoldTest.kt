package io.github.taetae98coding.diary.feature.memo.ui.add

import androidx.compose.runtime.remember
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasProgressBarRangeInfo
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.paging.compose.collectAsLazyPagingItems
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.feature.memo.ui.form.rememberMemoAddFormState
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
class MemoAddScaffoldTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    @Config(qualifiers = "ko")
    fun `한국어 환경에서 제목은 메모 추가이다`() {
        setMemoAddScaffold()

        composeRule.onNodeWithText(KOREAN_TITLE).assertExists()
    }

    @Test
    fun `기본 환경에서 제목은 Add Memo이다`() {
        setMemoAddScaffold()

        composeRule.onNodeWithText(DEFAULT_TITLE).assertExists()
    }

    @Test
    @Config(qualifiers = "ko")
    fun `한국어 뒤로가기 버튼 이름은 뒤로가기이다`() {
        setMemoAddScaffold()

        composeRule.onNodeWithContentDescription(KOREAN_NAVIGATE_UP_DESCRIPTION).assert(hasClickAction())
    }

    @Test
    fun `기본 뒤로가기 버튼 이름은 Navigate up이다`() {
        setMemoAddScaffold()

        composeRule.onNodeWithContentDescription(DEFAULT_NAVIGATE_UP_DESCRIPTION).assert(hasClickAction())
    }

    @Test
    @Config(qualifiers = "ko")
    fun `한국어 추가 버튼 이름은 메모 추가이다`() {
        setMemoAddScaffold()

        composeRule.onNodeWithContentDescription(KOREAN_ADD_BUTTON_DESCRIPTION).assert(hasClickAction())
    }

    @Test
    fun `기본 추가 버튼 이름은 Add memo이다`() {
        setMemoAddScaffold()

        composeRule.onNodeWithContentDescription(DEFAULT_ADD_BUTTON_DESCRIPTION).assert(hasClickAction())
    }

    @Test
    fun `TC-MEMO-LIST-DETAIL-FEATURE-009 목록과 함께 표시되면 뒤로가기 버튼이 표시되지 않는다`() {
        setMemoAddScaffold(componentVisibleProvider = { MemoAddScaffoldComponentVisible(isNavigateUpButtonVisible = false) })

        composeRule.onNodeWithContentDescription(DEFAULT_NAVIGATE_UP_DESCRIPTION).assertDoesNotExist()
        composeRule.onNodeWithText(DEFAULT_TITLE).assertExists()
    }

    @Test
    fun `TC-MEMO-ADD-FEATURE-008 추가 처리 중 추가 버튼이 진행 표시로 바뀐다`() {
        setMemoAddScaffold(uiStateProvider = { MemoAddUiState(isInProgress = true) })

        composeRule.onNode(hasProgressBarRangeInfo(ProgressBarRangeInfo.Indeterminate)).assertExists()
        composeRule.onNodeWithContentDescription(DEFAULT_ADD_BUTTON_DESCRIPTION).assertDoesNotExist()
        composeRule.onNodeWithText(DEFAULT_TITLE).assertExists()
    }

    @Test
    fun `TC-MEMO-PLACE-CARD-FEATURE-001 본문에 장소 카드를 표시한다`() {
        setMemoAddScaffold()

        composeRule.onNodeWithContentDescription(DEFAULT_MAP_DESCRIPTION).assertExists()
    }

    private fun setMemoAddScaffold(
        uiStateProvider: () -> MemoAddUiState = { MemoAddUiState() },
        onEvent: (MemoAddScaffoldEvent) -> Unit = {},
        componentVisibleProvider: () -> MemoAddScaffoldComponentVisible = { MemoAddScaffoldComponentVisible() },
    ) {
        composeRule.setContent {
            DiaryTheme {
                val tagPagingData = remember { MutableStateFlow(tagPagingDataOf(emptyList())) }

                MemoAddScaffold(
                    state = rememberMemoAddFormState(),
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
        private const val KOREAN_TITLE = "메모 추가"
        private const val DEFAULT_TITLE = "Add Memo"
        private const val KOREAN_NAVIGATE_UP_DESCRIPTION = "뒤로가기"
        private const val DEFAULT_NAVIGATE_UP_DESCRIPTION = "Navigate up"
        private const val KOREAN_ADD_BUTTON_DESCRIPTION = "메모 추가"
        private const val DEFAULT_ADD_BUTTON_DESCRIPTION = "Add memo"
    }
}
