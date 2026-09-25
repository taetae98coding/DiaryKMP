package io.github.taetae98coding.diary.feature.web.ui.detail

import androidx.compose.ui.input.key.Key
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performKeyInput
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.pressKey
import androidx.compose.ui.test.swipeDown
import androidx.compose.ui.test.swipeLeft
import androidx.compose.ui.test.swipeRight
import androidx.compose.ui.test.withKeyDown
import androidx.paging.PagingData
import io.github.taetae98coding.diary.compose.memo.list.MemoListEffect
import io.github.taetae98coding.diary.compose.memo.list.MemoListItem
import io.github.taetae98coding.diary.core.model.memo.Memo
import io.github.taetae98coding.diary.feature.web.ui.detail.memo.WEB_DETAIL_MEMO_LIST_TEST_TAG
import io.github.taetae98coding.diary.feature.web.ui.detail.memo.WebDetailMemoViewModel
import io.kotest.matchers.shouldBe
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.uuid.Uuid

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class WebDetailScreenMemoTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `TC-WEB-DETAIL-MEMO-FEATURE-009 메모 추가 버튼을 선택하면 MemoAdd 이동을 요청한다`() {
        var navigateToMemoAddCount = 0
        setScreenOnMemoTab(navigateToMemoAdd = { navigateToMemoAddCount += 1 })

        composeRule.onNodeWithContentDescription(DEFAULT_MEMO_ADD_DESCRIPTION).performClick()

        navigateToMemoAddCount shouldBe 1
    }

    @Test
    @Config(qualifiers = "ko")
    fun `TC-WEB-DETAIL-MEMO-FEATURE-009 한국어 환경 메모 추가 버튼 이름을 표시한다`() {
        setScreenOnMemoTab(memoTabDescription = KOREAN_MEMO_TAB_DESCRIPTION)

        composeRule.onNodeWithContentDescription(KOREAN_MEMO_ADD_DESCRIPTION).assertExists()
    }

    @Test
    fun `메모 탭에서 Cmd A 단축키를 입력하면 메모 추가 화면 전환 행동을 한 번 전달한다`() {
        var navigateToMemoAddCount = 0
        setScreenOnMemoTab(navigateToMemoAdd = { navigateToMemoAddCount += 1 })

        composeRule.onRoot().performKeyInput { withKeyDown(Key.MetaLeft) { pressKey(Key.A) } }

        navigateToMemoAddCount shouldBe 1
    }

    @Test
    fun `TC-WEB-DETAIL-MEMO-FEATURE-016 웹 항목 디테일 탭에서는 메모 추가를 제공하지 않고 메모 탭으로 전환하면 제공한다`() {
        var navigateToMemoAddCount = 0
        setScreen(navigateToMemoAdd = { navigateToMemoAddCount += 1 })

        composeRule.onRoot().performKeyInput { withKeyDown(Key.MetaLeft) { pressKey(Key.A) } }
        navigateToMemoAddCount shouldBe 0
        composeRule.onNodeWithContentDescription(DEFAULT_MEMO_ADD_DESCRIPTION).assertDoesNotExist()

        composeRule.selectWebDetailTab(DEFAULT_MEMO_TAB_DESCRIPTION)
        composeRule.onNodeWithContentDescription(DEFAULT_MEMO_ADD_DESCRIPTION).performClick()

        navigateToMemoAddCount shouldBe 1
    }

    @Test
    fun `TC-WEB-DETAIL-MEMO-FEATURE-008 대상 웹 항목의 상태와 무관하게 메모 추가를 시작할 수 있다`() {
        // 계정과 연결되지 않은 웹 항목는 조회되지 않으므로 화면에서는 조회 전과 같은 상태로 관찰되고, 삭제된 웹 항목는 내용 표시 상태로 관찰된다.
        val uiStateList = listOf(content(), WebDetailUiState.Loading)
        val uiStateFlow = MutableStateFlow<WebDetailUiState>(uiStateList.first())
        var navigateToMemoAddCount = 0
        composeRule.setWebDetailMemoScreen(
            viewModel = memoScreenWebViewModel(uiStateFlow),
            navigateToMemoAdd = { navigateToMemoAddCount += 1 },
        )
        composeRule.selectWebDetailTab(DEFAULT_MEMO_TAB_DESCRIPTION)

        uiStateList.forEachIndexed { index, uiState ->
            composeRule.runOnIdle { uiStateFlow.value = uiState }
            composeRule.waitForIdle()

            composeRule.onNodeWithContentDescription(DEFAULT_MEMO_ADD_DESCRIPTION).performClick()

            navigateToMemoAddCount shouldBe index + 1
        }
    }

    @Test
    fun `TC-WEB-DETAIL-MEMO-FEATURE-004 메모를 선택하면 그 메모의 상세로 이동한다`() {
        val memo = webMemo(title = SCREEN_MEMO_TITLE)
        val navigatedIdList = mutableListOf<Uuid>()
        setScreenOnMemoTab(
            memoPagingData = webMemoPagingData(itemList = listOf(MemoListItem.Content(memo = memo))),
            navigateToMemoDetail = navigatedIdList::add,
        )
        waitUntilMemoIsDisplayed(title = memo.detail.title)

        composeRule.onNodeWithText(memo.detail.title).performClick()

        navigatedIdList shouldBe listOf(memo.id)
    }

    @Test
    fun `TC-WEB-DETAIL-MEMO-FEATURE-005 완료하면 카드가 사라지고 안내와 실행 취소를 표시한다`() {
        val memo = swipeMemo()

        composeRule.onNodeWithText(memo.detail.title).performTouchInput { swipeRight() }
        composeRule.waitForIdle()

        verify(exactly = 1) { memoViewModel().finish(id = memo.id) }
        emitMemoEffect(MemoListEffect.Finished(id = memo.id))

        composeRule.onNodeWithText(text = memo.detail.title, useUnmergedTree = true).assertIsNotDisplayed()
        composeRule.onNodeWithText(DEFAULT_FINISHED_MESSAGE).assertIsDisplayed()
        composeRule.onNodeWithText(DEFAULT_UNDO_ACTION).assertIsDisplayed()
    }

    @Test
    @Config(qualifiers = "ko")
    fun `TC-WEB-DETAIL-MEMO-FEATURE-005 삭제하면 카드가 사라지고 한국어 안내와 실행 취소를 표시한다`() {
        val memo = swipeMemo(memoTabDescription = KOREAN_MEMO_TAB_DESCRIPTION)

        composeRule.onNodeWithText(memo.detail.title).performTouchInput { swipeLeft() }
        composeRule.waitForIdle()

        verify(exactly = 1) { memoViewModel().delete(id = memo.id) }
        emitMemoEffect(MemoListEffect.Deleted(id = memo.id))

        composeRule.onNodeWithText(KOREAN_DELETED_MESSAGE).assertIsDisplayed()
        composeRule.onNodeWithText(KOREAN_UNDO_ACTION).assertIsDisplayed()
    }

    @Test
    fun `TC-WEB-DETAIL-MEMO-FEATURE-006 완료 실행 취소는 메모 다시 시작을 요청한다`() {
        val memo = swipeMemo()

        composeRule.onNodeWithText(memo.detail.title).performTouchInput { swipeRight() }
        composeRule.waitForIdle()
        emitMemoEffect(MemoListEffect.Finished(id = memo.id))
        composeRule.onNodeWithText(DEFAULT_UNDO_ACTION).performClick()
        composeRule.waitForIdle()

        verify(exactly = 1) { memoViewModel().restart(id = memo.id) }
    }

    @Test
    fun `TC-WEB-DETAIL-MEMO-FEATURE-006 삭제 실행 취소는 메모 복구를 요청한다`() {
        val memo = swipeMemo()

        composeRule.onNodeWithText(memo.detail.title).performTouchInput { swipeLeft() }
        composeRule.waitForIdle()
        emitMemoEffect(MemoListEffect.Deleted(id = memo.id))
        composeRule.onNodeWithText(DEFAULT_UNDO_ACTION).performClick()
        composeRule.waitForIdle()

        verify(exactly = 1) { memoViewModel().restore(id = memo.id) }
    }

    @Test
    fun `TC-SYNC-REFRESH-FEATURE-001 메모 탭에서 목록을 당기면 새로고침을 요청한다`() {
        setScreenOnMemoTab(memoPagingData = webMemoPagingData(itemList = listOf(MemoListItem.Content(memo = webMemo(title = SCREEN_MEMO_TITLE)))))
        waitUntilMemoIsDisplayed(title = SCREEN_MEMO_TITLE)

        composeRule.onNodeWithTag(WEB_DETAIL_MEMO_LIST_TEST_TAG).performTouchInput { swipeDown() }
        composeRule.waitForIdle()

        verify(exactly = 1) { requireNotNull(memoSyncViewModelRef).refresh() }
    }

    @Test
    fun `TC-WEB-DETAIL-MEMO-DATA-006 TC-WEB-DETAIL-MEMO-FEATURE-017 대상 웹 항목를 조회하지 못해도 메모 목록은 노출 기준대로 표시된다`() {
        composeRule.setWebDetailMemoScreen(
            viewModel = memoScreenWebViewModel(MutableStateFlow(WebDetailUiState.Loading)),
            memoPagingData = webMemoPagingData(itemList = listOf(MemoListItem.Content(memo = webMemo(title = INDEPENDENT_MEMO_TITLE)))),
        )
        composeRule.selectWebDetailTab(DEFAULT_MEMO_TAB_DESCRIPTION)

        waitUntilMemoIsDisplayed(title = INDEPENDENT_MEMO_TITLE)
        composeRule.onNodeWithText(INDEPENDENT_MEMO_TITLE).assertIsDisplayed()
        composeRule.onNodeWithContentDescription(DEFAULT_MEMO_ADD_DESCRIPTION).assertExists()
    }

    @Test
    fun `TC-WEB-DETAIL-DOMAIN-047 수정 삭제 웹 페이지 불러오기를 처리하는 중에도 메모 완료와 실행 취소를 요청한다`() {
        val memo = inProgressSwipeMemo()

        composeRule.onNodeWithText(memo.detail.title).performTouchInput { swipeRight() }
        composeRule.waitForIdle()
        emitMemoEffect(MemoListEffect.Finished(id = memo.id))
        composeRule.onNodeWithText(DEFAULT_UNDO_ACTION).performClick()
        composeRule.waitForIdle()

        verify(exactly = 1) { memoViewModel().finish(id = memo.id) }
        verify(exactly = 1) { memoViewModel().restart(id = memo.id) }
    }

    @Test
    fun `TC-WEB-DETAIL-DOMAIN-047 수정 삭제 웹 페이지 불러오기를 처리하는 중에도 메모 삭제와 실행 취소를 요청한다`() {
        val memo = inProgressSwipeMemo()

        composeRule.onNodeWithText(memo.detail.title).performTouchInput { swipeLeft() }
        composeRule.waitForIdle()
        emitMemoEffect(MemoListEffect.Deleted(id = memo.id))
        composeRule.onNodeWithText(DEFAULT_UNDO_ACTION).performClick()
        composeRule.waitForIdle()

        verify(exactly = 1) { memoViewModel().delete(id = memo.id) }
        verify(exactly = 1) { memoViewModel().restore(id = memo.id) }
    }

    // 웹 페이지 불러오기는 화면 fixture가 불러오는 중 상태로 둔다.
    private fun inProgressSwipeMemo(): Memo {
        val memo = webMemo(title = SCREEN_MEMO_TITLE)
        composeRule.setWebDetailMemoScreen(
            viewModel = memoScreenWebViewModel(MutableStateFlow(content().copy(isUpdateInProgress = true, isDeleteInProgress = true))),
            memoPagingData = webMemoPagingData(itemList = listOf(MemoListItem.Content(memo = memo))),
        )
        composeRule.selectWebDetailTab(DEFAULT_MEMO_TAB_DESCRIPTION)
        waitUntilMemoIsDisplayed(title = memo.detail.title)

        return memo
    }

    private fun swipeMemo(memoTabDescription: String = DEFAULT_MEMO_TAB_DESCRIPTION): Memo {
        val memo = webMemo(title = SCREEN_MEMO_TITLE)
        setScreenOnMemoTab(
            memoPagingData = webMemoPagingData(itemList = listOf(MemoListItem.Content(memo = memo))),
            memoTabDescription = memoTabDescription,
        )
        waitUntilMemoIsDisplayed(title = memo.detail.title)

        return memo
    }

    // 목록은 스와이프 결과를 저장소 갱신으로 확인하므로, 사라진 목록과 결과 Effect를 함께 넣는다.
    private fun emitMemoEffect(effect: MemoListEffect) {
        memoPagingDataFlow.value = webMemoPagingData(itemList = emptyList())
        memoEffectFlow.tryEmit(effect)
        composeRule.waitForIdle()
    }

    private fun memoViewModel(): WebDetailMemoViewModel = requireNotNull(memoViewModelRef)

    private fun setScreenOnMemoTab(
        memoPagingData: PagingData<MemoListItem> = webMemoPagingData(itemList = emptyList()),
        memoTabDescription: String = DEFAULT_MEMO_TAB_DESCRIPTION,
        navigateToMemoAdd: () -> Unit = {},
        navigateToMemoDetail: (Uuid) -> Unit = {},
    ) {
        setScreen(memoPagingData = memoPagingData, navigateToMemoAdd = navigateToMemoAdd, navigateToMemoDetail = navigateToMemoDetail)
        composeRule.selectWebDetailTab(memoTabDescription)
    }

    private fun setScreen(
        memoPagingData: PagingData<MemoListItem> = webMemoPagingData(itemList = emptyList()),
        navigateToMemoAdd: () -> Unit = {},
        navigateToMemoDetail: (Uuid) -> Unit = {},
    ) {
        composeRule.setWebDetailMemoScreen(
            viewModel = memoScreenWebViewModel(MutableStateFlow(content())),
            memoPagingData = memoPagingData,
            navigateToMemoAdd = navigateToMemoAdd,
            navigateToMemoDetail = navigateToMemoDetail,
        )
    }

    private fun waitUntilMemoIsDisplayed(title: String) {
        composeRule.waitUntil(timeoutMillis = PAGING_ITEMS_TIMEOUT_MILLIS) {
            composeRule.onAllNodesWithText(title).fetchSemanticsNodes().isNotEmpty()
        }
    }

    private companion object {
        const val PAGING_ITEMS_TIMEOUT_MILLIS = 5_000L
        const val CONTACT_NAME = "WebDetailMemoScreenName"
        const val SCREEN_MEMO_TITLE = "WebDetailScreenMemo"
        const val INDEPENDENT_MEMO_TITLE = "WebDetailIndependentMemo"
        const val DEFAULT_FINISHED_MESSAGE = "Memo finished."
        const val KOREAN_DELETED_MESSAGE = "메모가 삭제되었습니다."
        const val DEFAULT_UNDO_ACTION = "Undo"
        const val KOREAN_UNDO_ACTION = "실행 취소"

        fun content(): WebDetailUiState.Content = testContentUiState(detail = testWebDetail(title = CONTACT_NAME))
    }
}
