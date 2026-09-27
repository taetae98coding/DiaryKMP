package io.github.taetae98coding.diary.feature.place.ui.detail

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
import io.github.taetae98coding.diary.feature.place.ui.detail.memo.PLACE_DETAIL_MEMO_LIST_TEST_TAG
import io.github.taetae98coding.diary.feature.place.ui.detail.memo.PlaceDetailMemoViewModel
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
class PlaceDetailScreenMemoTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `TC-PLACE-DETAIL-MEMO-FEATURE-009 메모 추가 버튼을 선택하면 MemoAdd 이동을 요청한다`() {
        var navigateToMemoAddCount = 0
        setScreenOnMemoTab(navigateToMemoAdd = { navigateToMemoAddCount += 1 })

        composeRule.onNodeWithContentDescription(DEFAULT_MEMO_ADD_DESCRIPTION).performClick()

        navigateToMemoAddCount shouldBe 1
    }

    @Test
    @Config(qualifiers = "ko")
    fun `TC-PLACE-DETAIL-MEMO-FEATURE-009 한국어 환경 메모 추가 버튼 이름을 표시한다`() {
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
    fun `TC-PLACE-DETAIL-MEMO-FEATURE-016 장소 디테일 탭에서는 메모 추가를 제공하지 않고 메모 탭으로 전환하면 제공한다`() {
        var navigateToMemoAddCount = 0
        setScreen(navigateToMemoAdd = { navigateToMemoAddCount += 1 })

        composeRule.onRoot().performKeyInput { withKeyDown(Key.MetaLeft) { pressKey(Key.A) } }
        navigateToMemoAddCount shouldBe 0
        composeRule.onNodeWithContentDescription(DEFAULT_MEMO_ADD_DESCRIPTION).assertDoesNotExist()

        composeRule.selectPlaceDetailTab(DEFAULT_MEMO_TAB_DESCRIPTION)
        composeRule.onNodeWithContentDescription(DEFAULT_MEMO_ADD_DESCRIPTION).performClick()

        navigateToMemoAddCount shouldBe 1
    }

    @Test
    fun `TC-PLACE-DETAIL-MEMO-FEATURE-008 대상 장소의 상태와 무관하게 메모 추가를 시작할 수 있다`() {
        // 계정과 연결되지 않은 장소는 조회되지 않으므로 화면에서는 조회 전과 같은 상태로 관찰되고, 삭제된 장소는 내용 표시 상태로 관찰된다.
        val uiStateList = listOf(screenContent(), PlaceDetailUiState.Loading)
        val uiStateFlow = MutableStateFlow<PlaceDetailUiState>(uiStateList.first())
        var navigateToMemoAddCount = 0
        composeRule.setPlaceDetailScreen(
            viewModel = screenTestViewModel(uiStateFlow),
            navigateToMemoAdd = { navigateToMemoAddCount += 1 },
        )
        composeRule.selectPlaceDetailTab(DEFAULT_MEMO_TAB_DESCRIPTION)

        uiStateList.forEachIndexed { index, uiState ->
            composeRule.runOnIdle { uiStateFlow.value = uiState }
            composeRule.waitForIdle()

            composeRule.onNodeWithContentDescription(DEFAULT_MEMO_ADD_DESCRIPTION).performClick()

            navigateToMemoAddCount shouldBe index + 1
        }
    }

    @Test
    fun `TC-PLACE-DETAIL-MEMO-FEATURE-004 메모를 선택하면 그 메모의 상세로 이동한다`() {
        val memo = placeMemo(title = SCREEN_MEMO_TITLE)
        val navigatedIdList = mutableListOf<Uuid>()
        setScreenOnMemoTab(
            memoPagingData = placeMemoPagingData(itemList = listOf(MemoListItem.Content(memo = memo))),
            navigateToMemoDetail = navigatedIdList::add,
        )
        waitUntilMemoIsDisplayed(title = memo.detail.title)

        composeRule.onNodeWithText(memo.detail.title).performClick()

        navigatedIdList shouldBe listOf(memo.id)
    }

    @Test
    fun `TC-PLACE-DETAIL-MEMO-FEATURE-005 완료하면 카드가 사라지고 안내와 실행 취소를 표시한다`() {
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
    fun `TC-PLACE-DETAIL-MEMO-FEATURE-005 삭제하면 카드가 사라지고 한국어 안내와 실행 취소를 표시한다`() {
        val memo = swipeMemo(memoTabDescription = KOREAN_MEMO_TAB_DESCRIPTION)

        composeRule.onNodeWithText(memo.detail.title).performTouchInput { swipeLeft() }
        composeRule.waitForIdle()

        verify(exactly = 1) { memoViewModel().delete(id = memo.id) }
        emitMemoEffect(MemoListEffect.Deleted(id = memo.id))

        composeRule.onNodeWithText(KOREAN_DELETED_MESSAGE).assertIsDisplayed()
        composeRule.onNodeWithText(KOREAN_UNDO_ACTION).assertIsDisplayed()
    }

    @Test
    fun `TC-PLACE-DETAIL-MEMO-FEATURE-006 완료 실행 취소는 메모 다시 시작을 요청한다`() {
        val memo = swipeMemo()

        composeRule.onNodeWithText(memo.detail.title).performTouchInput { swipeRight() }
        composeRule.waitForIdle()
        emitMemoEffect(MemoListEffect.Finished(id = memo.id))
        composeRule.onNodeWithText(DEFAULT_UNDO_ACTION).performClick()
        composeRule.waitForIdle()

        verify(exactly = 1) { memoViewModel().restart(id = memo.id) }
    }

    @Test
    fun `TC-PLACE-DETAIL-MEMO-FEATURE-006 삭제 실행 취소는 메모 복구를 요청한다`() {
        val memo = swipeMemo()

        composeRule.onNodeWithText(memo.detail.title).performTouchInput { swipeLeft() }
        composeRule.waitForIdle()
        emitMemoEffect(MemoListEffect.Deleted(id = memo.id))
        composeRule.onNodeWithText(DEFAULT_UNDO_ACTION).performClick()
        composeRule.waitForIdle()

        verify(exactly = 1) { memoViewModel().restore(id = memo.id) }
    }

    @Test
    fun `TC-PLACE-DETAIL-MEMO-FEATURE-022 안내가 보이는 동안 다른 탭으로 바꾸면 안내가 닫히고 되돌릴 수 없다`() {
        val memo = swipeMemo()

        composeRule.onNodeWithText(memo.detail.title).performTouchInput { swipeLeft() }
        composeRule.waitForIdle()
        emitMemoEffect(MemoListEffect.Deleted(id = memo.id))
        composeRule.onNodeWithText(DEFAULT_DELETED_MESSAGE).assertIsDisplayed()

        composeRule.selectPlaceDetailTab(DEFAULT_DETAIL_TAB_DESCRIPTION)
        composeRule.mainClock.advanceTimeBy(TAB_CHANGE_SETTLE_MILLIS)
        composeRule.waitForIdle()

        composeRule.onNodeWithText(DEFAULT_DELETED_MESSAGE).assertDoesNotExist()
        composeRule.onNodeWithText(DEFAULT_UNDO_ACTION).assertDoesNotExist()
        verify(exactly = 0) { memoViewModel().restore(id = any()) }
    }

    @Test
    fun `TC-SYNC-REFRESH-FEATURE-001 메모 탭에서 목록을 당기면 새로고침을 요청한다`() {
        setScreenOnMemoTab(memoPagingData = placeMemoPagingData(itemList = listOf(MemoListItem.Content(memo = placeMemo(title = SCREEN_MEMO_TITLE)))))
        waitUntilMemoIsDisplayed(title = SCREEN_MEMO_TITLE)

        composeRule.onNodeWithTag(PLACE_DETAIL_MEMO_LIST_TEST_TAG).performTouchInput { swipeDown() }
        composeRule.waitForIdle()

        verify(exactly = 1) { requireNotNull(memoSyncViewModelRef).refresh() }
    }

    @Test
    fun `TC-PLACE-DETAIL-MEMO-DATA-006 TC-PLACE-DETAIL-MEMO-FEATURE-017 대상 장소를 조회하지 못해도 메모 목록은 노출 기준대로 표시된다`() {
        composeRule.setPlaceDetailScreen(
            viewModel = screenTestViewModel(MutableStateFlow(PlaceDetailUiState.Loading)),
            memoPagingData = placeMemoPagingData(itemList = listOf(MemoListItem.Content(memo = placeMemo(title = INDEPENDENT_MEMO_TITLE)))),
        )
        composeRule.selectPlaceDetailTab(DEFAULT_MEMO_TAB_DESCRIPTION)

        waitUntilMemoIsDisplayed(title = INDEPENDENT_MEMO_TITLE)
        composeRule.onNodeWithText(INDEPENDENT_MEMO_TITLE).assertIsDisplayed()
        composeRule.onNodeWithContentDescription(DEFAULT_MEMO_ADD_DESCRIPTION).assertExists()
    }

    @Test
    fun `TC-PLACE-DETAIL-DOMAIN-037 수정이나 삭제를 처리하는 중에도 메모 완료와 실행 취소를 요청한다`() {
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
    fun `TC-PLACE-DETAIL-DOMAIN-037 수정이나 삭제를 처리하는 중에도 메모 삭제와 실행 취소를 요청한다`() {
        val memo = inProgressSwipeMemo()

        composeRule.onNodeWithText(memo.detail.title).performTouchInput { swipeLeft() }
        composeRule.waitForIdle()
        emitMemoEffect(MemoListEffect.Deleted(id = memo.id))
        composeRule.onNodeWithText(DEFAULT_UNDO_ACTION).performClick()
        composeRule.waitForIdle()

        verify(exactly = 1) { memoViewModel().delete(id = memo.id) }
        verify(exactly = 1) { memoViewModel().restore(id = memo.id) }
    }

    private fun inProgressSwipeMemo(): Memo {
        val memo = placeMemo(title = SCREEN_MEMO_TITLE)
        composeRule.setPlaceDetailScreen(
            viewModel = screenTestViewModel(MutableStateFlow(screenContent().copy(isUpdateInProgress = true, isDeleteInProgress = true))),
            memoPagingData = placeMemoPagingData(itemList = listOf(MemoListItem.Content(memo = memo))),
        )
        composeRule.selectPlaceDetailTab(DEFAULT_MEMO_TAB_DESCRIPTION)
        waitUntilMemoIsDisplayed(title = memo.detail.title)

        return memo
    }

    private fun swipeMemo(memoTabDescription: String = DEFAULT_MEMO_TAB_DESCRIPTION): Memo {
        val memo = placeMemo(title = SCREEN_MEMO_TITLE)
        setScreenOnMemoTab(
            memoPagingData = placeMemoPagingData(itemList = listOf(MemoListItem.Content(memo = memo))),
            memoTabDescription = memoTabDescription,
        )
        waitUntilMemoIsDisplayed(title = memo.detail.title)

        return memo
    }

    // 목록은 스와이프 결과를 저장소 갱신으로 확인하므로, 사라진 목록과 결과 Effect를 함께 넣는다.
    private fun emitMemoEffect(effect: MemoListEffect) {
        memoPagingDataFlow.value = placeMemoPagingData(itemList = emptyList())
        memoEffectFlow.tryEmit(effect)
        composeRule.waitForIdle()
    }

    private fun memoViewModel(): PlaceDetailMemoViewModel = requireNotNull(memoViewModelRef)

    private fun setScreenOnMemoTab(
        memoPagingData: PagingData<MemoListItem> = placeMemoPagingData(itemList = emptyList()),
        memoTabDescription: String = DEFAULT_MEMO_TAB_DESCRIPTION,
        navigateToMemoAdd: () -> Unit = {},
        navigateToMemoDetail: (Uuid) -> Unit = {},
    ) {
        setScreen(memoPagingData = memoPagingData, navigateToMemoAdd = navigateToMemoAdd, navigateToMemoDetail = navigateToMemoDetail)
        composeRule.selectPlaceDetailTab(memoTabDescription)
    }

    private fun setScreen(
        memoPagingData: PagingData<MemoListItem> = placeMemoPagingData(itemList = emptyList()),
        navigateToMemoAdd: () -> Unit = {},
        navigateToMemoDetail: (Uuid) -> Unit = {},
    ) {
        composeRule.setPlaceDetailScreen(
            viewModel = screenTestViewModel(MutableStateFlow(screenContent())),
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
        const val CONTACT_NAME = "PlaceDetailMemoScreenName"
        const val SCREEN_MEMO_TITLE = "PlaceDetailScreenMemo"
        const val INDEPENDENT_MEMO_TITLE = "PlaceDetailIndependentMemo"
        const val DEFAULT_FINISHED_MESSAGE = "Memo finished."
        const val DEFAULT_DELETED_MESSAGE = "Memo deleted."
        const val TAB_CHANGE_SETTLE_MILLIS = 1_000L
        const val KOREAN_DELETED_MESSAGE = "메모가 삭제되었습니다."
        const val DEFAULT_UNDO_ACTION = "Undo"
        const val KOREAN_UNDO_ACTION = "실행 취소"

        fun screenContent(): PlaceDetailUiState.Content = content(id = FIRST_PLACE_ID, detail = placeDetail(title = CONTACT_NAME))
    }
}
