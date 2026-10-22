package io.github.taetae98coding.diary.feature.web.ui.detail

import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.paging.PagingData
import io.github.taetae98coding.diary.compose.memo.list.MemoListItem
import io.github.taetae98coding.diary.feature.web.ui.resetAndroidUiDispatcher
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class WebDetailScreenMemoFinishedListTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Before
    fun setUp() {
        resetAndroidUiDispatcher()
    }

    @Test
    fun `TC-b5610b9b-0b4b-413c-9236-896c51c8bccb 완료된 메모 확인을 선택하면 완료된 메모 목록 이동을 요청한다`() {
        var navigateCount = 0
        setScreenOnMemoTab(navigateToMemoFinishedList = { navigateCount += 1 })

        composeRule.onNodeWithText(DEFAULT_FINISHED_LIST_LABEL).assertHasClickAction().performClick()

        navigateCount shouldBe 1
    }

    @Test
    @Config(qualifiers = "ko")
    fun `TC-b5610b9b-0b4b-413c-9236-896c51c8bccb 한국어 환경 완료된 메모 확인 라벨을 표시한다`() {
        setScreenOnMemoTab(memoTabDescription = KOREAN_MEMO_TAB_DESCRIPTION)

        composeRule.onNodeWithText(KOREAN_FINISHED_LIST_LABEL).assertHasClickAction()
    }

    @Test
    fun `TC-a3adbba1-4bd2-4707-be06-4b7686bdf698 연결된 메모가 표시되어 있어도 완료된 메모 확인을 실행할 수 있다`() {
        val memo = webMemo(title = MEMO_TITLE)
        var navigateCount = 0
        setScreenOnMemoTab(
            memoPagingData = webMemoPagingData(itemList = listOf(MemoListItem.Content(memo = memo))),
            navigateToMemoFinishedList = { navigateCount += 1 },
        )
        composeRule.waitUntil(timeoutMillis = PAGING_ITEMS_TIMEOUT_MILLIS) {
            composeRule.onAllNodesWithText(memo.detail.title).fetchSemanticsNodes().isNotEmpty()
        }

        composeRule.onNodeWithText(DEFAULT_FINISHED_LIST_LABEL).performClick()

        navigateCount shouldBe 1
    }

    @Test
    fun `TC-a3adbba1-4bd2-4707-be06-4b7686bdf698 TC-fa086b84-94ef-4d78-a1b4-2dd04e8a8211 빈 상태와 대상 조회 상태와 무관하게 완료된 메모 확인을 실행할 수 있다`() {
        val uiStateList = listOf(testContentUiState(), WebDetailUiState.Loading)
        val uiStateFlow = MutableStateFlow<WebDetailUiState>(uiStateList.first())
        var navigateCount = 0
        setScreenOnMemoTab(uiStateFlow = uiStateFlow, navigateToMemoFinishedList = { navigateCount += 1 })

        uiStateList.forEachIndexed { index, uiState ->
            composeRule.runOnIdle { uiStateFlow.value = uiState }
            composeRule.waitForIdle()

            composeRule.onNodeWithText(DEFAULT_FINISHED_LIST_LABEL).performClick()

            navigateCount shouldBe index + 1
        }
    }

    @Test
    fun `TC-c9d6fb2a-74d7-40b3-8ed6-dbde044c2e39 수정 폼 탭에서는 완료된 메모 확인을 제공하지 않고 메모 탭으로 전환하면 제공한다`() {
        var navigateCount = 0
        val uiStateFlow = MutableStateFlow<WebDetailUiState>(testContentUiState())
        composeRule.setWebDetailMemoScreen(viewModel = memoScreenWebViewModel(uiStateFlow), navigateToMemoFinishedList = { navigateCount += 1 })
        composeRule.selectWebDetailTab(DEFAULT_FORM_TAB_DESCRIPTION)

        composeRule.onNodeWithText(DEFAULT_FINISHED_LIST_LABEL).assertDoesNotExist()

        composeRule.selectWebDetailTab(DEFAULT_MEMO_TAB_DESCRIPTION)
        composeRule.onNodeWithText(DEFAULT_FINISHED_LIST_LABEL).performClick()

        navigateCount shouldBe 1
    }

    @Test
    fun `TC-c9d6fb2a-74d7-40b3-8ed6-dbde044c2e39 웹 페이지 탭에서는 완료된 메모 확인을 제공하지 않고 메모 탭으로 전환하면 제공한다`() {
        var navigateCount = 0
        val uiStateFlow = MutableStateFlow<WebDetailUiState>(testContentUiState())
        composeRule.setWebDetailMemoScreen(viewModel = memoScreenWebViewModel(uiStateFlow), navigateToMemoFinishedList = { navigateCount += 1 })
        composeRule.selectWebDetailTab(DEFAULT_PAGE_TAB_DESCRIPTION)

        composeRule.onNodeWithText(DEFAULT_FINISHED_LIST_LABEL).assertDoesNotExist()

        composeRule.selectWebDetailTab(DEFAULT_MEMO_TAB_DESCRIPTION)
        composeRule.onNodeWithText(DEFAULT_FINISHED_LIST_LABEL).performClick()

        navigateCount shouldBe 1
    }

    private fun setScreenOnMemoTab(
        memoPagingData: PagingData<MemoListItem> = webMemoPagingData(itemList = emptyList()),
        memoTabDescription: String = DEFAULT_MEMO_TAB_DESCRIPTION,
        uiStateFlow: MutableStateFlow<WebDetailUiState> = MutableStateFlow(testContentUiState()),
        navigateToMemoFinishedList: () -> Unit = {},
    ) {
        composeRule.setWebDetailMemoScreen(
            viewModel = memoScreenWebViewModel(uiStateFlow),
            memoPagingData = memoPagingData,
            navigateToMemoFinishedList = navigateToMemoFinishedList,
        )
        composeRule.selectWebDetailTab(memoTabDescription)
    }

    private companion object {
        const val PAGING_ITEMS_TIMEOUT_MILLIS = 5_000L
        const val MEMO_TITLE = "WebDetailFinishedEntryMemo"
        const val DEFAULT_FINISHED_LIST_LABEL = "Finished memos"
        const val KOREAN_FINISHED_LIST_LABEL = "완료된 메모"
    }
}
