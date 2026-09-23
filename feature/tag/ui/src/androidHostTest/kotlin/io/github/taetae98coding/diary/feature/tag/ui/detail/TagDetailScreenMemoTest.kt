package io.github.taetae98coding.diary.feature.tag.ui.detail

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performKeyInput
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeDown
import androidx.compose.ui.test.swipeLeft
import androidx.compose.ui.test.swipeRight
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.paging.PagingData
import io.github.taetae98coding.diary.compose.memo.MEMO_DATE_HEADER_TEST_TAG
import io.github.taetae98coding.diary.compose.memo.list.MemoListEffect
import io.github.taetae98coding.diary.compose.memo.list.MemoListItem
import io.github.taetae98coding.diary.compose.memo.list.MemoListState
import io.github.taetae98coding.diary.compose.memo.list.UpdateMemoListTodayEffect
import io.github.taetae98coding.diary.core.model.memo.Memo
import io.github.taetae98coding.diary.core.model.tag.TagScope
import io.github.taetae98coding.diary.feature.tag.ui.allDayMemoDateTime
import io.github.taetae98coding.diary.feature.tag.ui.detail.memo.TAG_DETAIL_MEMO_LIST_TEST_TAG
import io.github.taetae98coding.diary.feature.tag.ui.detail.memo.TagDetailMemoViewModel
import io.github.taetae98coding.diary.feature.tag.ui.tagMemo
import io.github.taetae98coding.diary.feature.tag.ui.tagMemoPagingData
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.time.Clock
import kotlin.uuid.Uuid
import java.util.TimeZone as JavaTimeZone

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class TagDetailScreenMemoTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `TC-TAG-DETAIL-MEMO-FEATURE-014 메모 추가 버튼을 선택하면 MemoAdd 이동을 요청한다`() {
        var navigateToMemoAddCount = 0
        setTagDetailScreenOnMemoTab(navigateToMemoAdd = { navigateToMemoAddCount += 1 })

        composeRule.onNodeWithContentDescription(DEFAULT_ADD_DESCRIPTION).performClick()

        navigateToMemoAddCount shouldBe 1
    }

    @Test
    fun `TC-TAG-DETAIL-MEMO-FEATURE-025 완료된 메모 확인 버튼을 선택하면 완료된 메모 목록으로 이동한다`() {
        var navigateToMemoFinishedListCount = 0
        setTagDetailScreenOnMemoTab(navigateToMemoFinishedList = { navigateToMemoFinishedListCount += 1 })

        composeRule.onNodeWithText(DEFAULT_FINISHED_LIST_LABEL).performClick()

        navigateToMemoFinishedListCount shouldBe 1
    }

    @Test
    fun `메모 탭에서 Cmd A 단축키를 입력하면 메모 추가 화면 전환 행동을 한 번 전달한다`() {
        var navigateToMemoAddCount = 0
        setTagDetailScreenOnMemoTab(navigateToMemoAdd = { navigateToMemoAddCount += 1 })

        composeRule.onRoot().performAddShortcut()

        navigateToMemoAddCount shouldBe 1
    }

    @Test
    fun `TC-TAG-DETAIL-MEMO-FEATURE-027 태그 디테일 탭에서는 Cmd A 단축키를 입력해도 메모 추가 화면으로 이동하지 않는다`() {
        var navigateToMemoAddCount = 0
        setTagDetailScreen(navigateToMemoAdd = { navigateToMemoAddCount += 1 })

        composeRule.onRoot().performAddShortcut()

        navigateToMemoAddCount shouldBe 0
        composeRule.onNodeWithContentDescription(DEFAULT_ADD_DESCRIPTION).assertDoesNotExist()
        composeRule.onNodeWithText(DEFAULT_FINISHED_LIST_LABEL).assertDoesNotExist()
    }

    @Test
    fun `TC-TAG-DETAIL-MEMO-FEATURE-019 대상 태그의 상태와 무관하게 메모 추가를 시작할 수 있다`() {
        // 계정과 연결되지 않은 태그와 삭제된 태그는 조회되지 않으므로 화면에서는 조회 전과 같은 상태로 관찰된다.
        val uiStateList =
            listOf(
                tagDetailUiState(detail = tagDetail(TAG_TITLE)),
                tagDetailUiState(detail = tagDetail(TAG_TITLE), isFinished = true),
                TagDetailUiState.Loading,
            )
        val uiStateFlow = MutableStateFlow<TagDetailUiState>(uiStateList.first())
        var navigateToMemoAddCount = 0

        composeRule.setTagDetailScreen(
            viewModel = screenTestViewModel(uiStateFlow),
            navigateToMemoAdd = { navigateToMemoAddCount += 1 },
        )
        composeRule.selectTagDetailTab(DEFAULT_MEMO_TAB_DESCRIPTION)

        uiStateList.forEachIndexed { index, uiState ->
            composeRule.runOnIdle { uiStateFlow.value = uiState }
            composeRule.waitForIdle()

            composeRule.onNodeWithContentDescription(DEFAULT_ADD_DESCRIPTION).performClick()

            navigateToMemoAddCount shouldBe index + 1
        }
    }

    @Test
    fun `TC-TAG-DETAIL-MEMO-FEATURE-028 완료된 메모 확인은 메모 탭에서만 제공된다`() {
        var navigateToMemoFinishedListCount = 0
        setTagDetailScreen(navigateToMemoFinishedList = { navigateToMemoFinishedListCount += 1 })

        composeRule.onNodeWithText(DEFAULT_FINISHED_LIST_LABEL).assertDoesNotExist()

        composeRule.selectTagDetailTab(DEFAULT_MEMO_TAB_DESCRIPTION)
        composeRule.onNodeWithText(DEFAULT_FINISHED_LIST_LABEL).performClick()

        navigateToMemoFinishedListCount shouldBe 1
    }

    @Test
    fun `TC-TAG-DETAIL-MEMO-FEATURE-007 메모를 선택하면 그 메모의 상세로 이동한다`() {
        val memo = tagMemo(title = SCREEN_MEMO_TITLE)
        val navigatedIdList = mutableListOf<Uuid>()
        setTagDetailScreenOnMemoTab(
            memoPagingData = tagMemoPagingData(itemList = listOf(MemoListItem.Content(memo = memo))),
            navigateToMemoDetail = navigatedIdList::add,
        )
        waitUntilMemoIsDisplayed(title = memo.detail.title)

        composeRule.onNodeWithText(memo.detail.title).performClick()

        navigatedIdList shouldBe listOf(memo.id)
    }

    @Test
    fun `TC-TAG-DETAIL-MEMO-FEATURE-008 완료하면 카드가 사라지고 기본 안내와 실행 취소를 표시한다`() {
        val memo = swipeMemo()

        composeRule.onNodeWithText(memo.detail.title).performTouchInput { swipeRight() }
        composeRule.waitForIdle()

        verify(exactly = 1) { memoViewModel().finish(id = memo.id) }
        emitMemoEffect(MemoListEffect.Finished(id = memo.id))

        assertMemoIsNotDisplayed(title = memo.detail.title)
        composeRule.onNodeWithText(DEFAULT_FINISHED_MESSAGE).assertIsDisplayed()
        composeRule.onNodeWithText(DEFAULT_UNDO_ACTION).assertIsDisplayed()
    }

    @Test
    @Config(qualifiers = "ko")
    fun `TC-TAG-DETAIL-MEMO-FEATURE-008 완료하면 한국어 안내와 실행 취소를 표시한다`() {
        val memo = swipeMemo(memoTabDescription = KOREAN_MEMO_TAB_DESCRIPTION)

        composeRule.onNodeWithText(memo.detail.title).performTouchInput { swipeRight() }
        composeRule.waitForIdle()
        emitMemoEffect(MemoListEffect.Finished(id = memo.id))

        composeRule.onNodeWithText(KOREAN_FINISHED_MESSAGE).assertIsDisplayed()
        composeRule.onNodeWithText(KOREAN_UNDO_ACTION).assertIsDisplayed()
    }

    @Test
    fun `TC-TAG-DETAIL-MEMO-FEATURE-008 삭제하면 카드가 사라지고 기본 안내와 실행 취소를 표시한다`() {
        val memo = swipeMemo()

        composeRule.onNodeWithText(memo.detail.title).performTouchInput { swipeLeft() }
        composeRule.waitForIdle()

        verify(exactly = 1) { memoViewModel().delete(id = memo.id) }
        emitMemoEffect(MemoListEffect.Deleted(id = memo.id))

        assertMemoIsNotDisplayed(title = memo.detail.title)
        composeRule.onNodeWithText(DEFAULT_DELETED_MESSAGE).assertIsDisplayed()
        composeRule.onNodeWithText(DEFAULT_UNDO_ACTION).assertIsDisplayed()
    }

    @Test
    @Config(qualifiers = "ko")
    fun `TC-TAG-DETAIL-MEMO-FEATURE-008 삭제하면 한국어 안내와 실행 취소를 표시한다`() {
        val memo = swipeMemo(memoTabDescription = KOREAN_MEMO_TAB_DESCRIPTION)

        composeRule.onNodeWithText(memo.detail.title).performTouchInput { swipeLeft() }
        composeRule.waitForIdle()
        emitMemoEffect(MemoListEffect.Deleted(id = memo.id))

        composeRule.onNodeWithText(KOREAN_DELETED_MESSAGE).assertIsDisplayed()
        composeRule.onNodeWithText(KOREAN_UNDO_ACTION).assertIsDisplayed()
    }

    @Test
    fun `TC-TAG-DETAIL-MEMO-FEATURE-009 완료 실행 취소는 메모 다시 시작을 요청한다`() {
        val memo = swipeMemo()

        composeRule.onNodeWithText(memo.detail.title).performTouchInput { swipeRight() }
        composeRule.waitForIdle()
        emitMemoEffect(MemoListEffect.Finished(id = memo.id))
        composeRule.onNodeWithText(DEFAULT_UNDO_ACTION).performClick()
        composeRule.waitForIdle()

        verify(exactly = 1) { memoViewModel().restart(id = memo.id) }
    }

    @Test
    fun `TC-TAG-DETAIL-MEMO-FEATURE-009 삭제 실행 취소는 메모 복구를 요청한다`() {
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
        setTagDetailScreenOnMemoTab(
            memoPagingData = tagMemoPagingData(itemList = listOf(MemoListItem.Content(memo = tagMemo(title = SCREEN_MEMO_TITLE)))),
        )
        waitUntilMemoIsDisplayed(title = SCREEN_MEMO_TITLE)

        composeRule.onNodeWithTag(TAG_DETAIL_MEMO_LIST_TEST_TAG).performTouchInput { swipeDown() }
        composeRule.waitForIdle()

        verify(exactly = 1) { requireNotNull(memoSyncViewModelRef).refresh() }
    }

    @Test
    fun `TC-TAG-DETAIL-MEMO-FEATURE-010 화면이 활성화되면 오늘 날짜 헤더를 표시한다`() {
        val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
        val memo = tagMemo(title = TODAY_MEMO_TITLE, dateTime = allDayMemoDateTime(today))
        setTagDetailScreenOnMemoTab(
            memoPagingData =
                tagMemoPagingData(
                    itemList =
                        listOf(
                            MemoListItem.DateHeader(date = today),
                            MemoListItem.Content(memo = memo),
                        ),
                ),
        )

        composeRule.waitUntil(timeoutMillis = PAGING_ITEMS_TIMEOUT_MILLIS) {
            composeRule.onAllNodes(todayHeader(DEFAULT_TODAY_HEADER)).fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNode(todayHeader(DEFAULT_TODAY_HEADER)).assertIsDisplayed()
    }

    @Test
    fun `TC-TAG-DETAIL-MEMO-FEATURE-010 화면이 다시 활성화되면 현재 시간대의 오늘을 다시 계산한다`() {
        val originalTimeZone = JavaTimeZone.getDefault()
        val lifecycleOwner = mockk<LifecycleOwner>()
        val lifecycleRegistry = LifecycleRegistry(lifecycleOwner)
        val memoListState = MemoListState()
        every { lifecycleOwner.lifecycle } returns lifecycleRegistry
        lifecycleRegistry.currentState = Lifecycle.State.CREATED

        try {
            composeRule.setContent {
                CompositionLocalProvider(LocalLifecycleOwner provides lifecycleOwner) {
                    UpdateMemoListTodayEffect(state = memoListState)
                }
            }

            val firstToday =
                composeRule.runOnIdle {
                    JavaTimeZone.setDefault(JavaTimeZone.getTimeZone(FIRST_TIME_ZONE))
                    lifecycleRegistry.currentState = Lifecycle.State.STARTED
                    memoListState.today shouldBe Clock.System.todayIn(TimeZone.currentSystemDefault())
                    memoListState.today
                }

            val secondToday =
                composeRule.runOnIdle {
                    lifecycleRegistry.currentState = Lifecycle.State.CREATED
                    JavaTimeZone.setDefault(JavaTimeZone.getTimeZone(SECOND_TIME_ZONE))
                    lifecycleRegistry.currentState = Lifecycle.State.STARTED
                    memoListState.today shouldBe Clock.System.todayIn(TimeZone.currentSystemDefault())
                    memoListState.today
                }

            firstToday shouldNotBe secondToday
        } finally {
            JavaTimeZone.setDefault(originalTimeZone)
        }
    }

    private fun swipeMemo(memoTabDescription: String = DEFAULT_MEMO_TAB_DESCRIPTION): Memo {
        val memo = tagMemo(title = SCREEN_MEMO_TITLE)
        setTagDetailScreenOnMemoTab(
            memoPagingData = tagMemoPagingData(itemList = listOf(MemoListItem.Content(memo = memo))),
            memoTabDescription = memoTabDescription,
        )
        waitUntilMemoIsDisplayed(title = memo.detail.title)

        return memo
    }

    // 목록은 스와이프 결과를 저장소 갱신으로 확인하므로, 사라진 목록과 결과 Effect를 함께 넣는다.
    private fun emitMemoEffect(effect: MemoListEffect) {
        memoPagingDataFlow.value = tagMemoPagingData(itemList = emptyList())
        memoEffectFlow.tryEmit(effect)
        composeRule.waitForIdle()
    }

    private fun memoViewModel(): TagDetailMemoViewModel = requireNotNull(memoViewModelRef)

    @Test
    fun `TC-TAG-DETAIL-MEMO-DATA-006 대상 태그를 조회하지 못해도 메모 목록은 노출 기준대로 표시된다`() {
        composeRule.setTagDetailScreen(
            viewModel = screenTestViewModel(MutableStateFlow(TagDetailUiState.Loading)),
            memoPagingData = tagMemoPagingData(itemList = listOf(MemoListItem.Content(memo = tagMemo(title = INDEPENDENT_MEMO_TITLE)))),
        )
        composeRule.selectTagDetailTab(DEFAULT_MEMO_TAB_DESCRIPTION)

        composeRule.onNodeWithText(INDEPENDENT_MEMO_TITLE).assertIsDisplayed()
    }

    @Test
    fun `TC-TAG-DETAIL-DOMAIN-015 대상 태그를 조회하지 못해도 표시 범위가 메모 목록 조회에 적용된다`() {
        composeRule.setTagDetailScreen(viewModel = screenTestViewModel(MutableStateFlow(TagDetailUiState.Loading)))
        composeRule.selectTagDetailTab(DEFAULT_MEMO_TAB_DESCRIPTION)

        composeRule.onNodeWithContentDescription(DEFAULT_SCOPE_BUTTON_DESCRIPTION).performClick()
        composeRule.onNodeWithText(DEFAULT_CHILD_SCOPE_LABEL).performClick()
        composeRule.waitForIdle()

        verify(exactly = 1) { checkNotNull(memoViewModelRef).select(scope = TagScope.CHILD) }
    }

    private fun setTagDetailScreenOnMemoTab(
        memoPagingData: PagingData<MemoListItem> = tagMemoPagingData(itemList = emptyList()),
        memoTabDescription: String = DEFAULT_MEMO_TAB_DESCRIPTION,
        navigateToMemoAdd: () -> Unit = {},
        navigateToMemoDetail: (Uuid) -> Unit = {},
        navigateToMemoFinishedList: () -> Unit = {},
    ) {
        setTagDetailScreen(
            memoPagingData = memoPagingData,
            navigateToMemoAdd = navigateToMemoAdd,
            navigateToMemoDetail = navigateToMemoDetail,
            navigateToMemoFinishedList = navigateToMemoFinishedList,
        )

        composeRule.selectTagDetailTab(memoTabDescription)
    }

    private fun setTagDetailScreen(
        memoPagingData: PagingData<MemoListItem> = tagMemoPagingData(itemList = emptyList()),
        navigateToMemoAdd: () -> Unit = {},
        navigateToMemoDetail: (Uuid) -> Unit = {},
        navigateToMemoFinishedList: () -> Unit = {},
    ) {
        composeRule.setTagDetailScreen(
            viewModel = screenTestViewModel(MutableStateFlow(tagDetailUiState(detail = tagDetail(TAG_TITLE)))),
            memoPagingData = memoPagingData,
            navigateToMemoAdd = navigateToMemoAdd,
            navigateToMemoDetail = navigateToMemoDetail,
            navigateToMemoFinishedList = navigateToMemoFinishedList,
        )
    }

    private fun assertMemoIsNotDisplayed(title: String) {
        composeRule
            .onNodeWithText(
                text = title,
                useUnmergedTree = true,
            ).assertIsNotDisplayed()
    }

    private fun waitUntilMemoIsDisplayed(title: String) {
        composeRule.waitUntil(timeoutMillis = PAGING_ITEMS_TIMEOUT_MILLIS) {
            composeRule.onAllNodesWithText(title).fetchSemanticsNodes().isNotEmpty()
        }
    }

    private fun todayHeader(text: String): SemanticsMatcher = hasTestTag(MEMO_DATE_HEADER_TEST_TAG) and hasText(text)

    private companion object {
        const val PAGING_ITEMS_TIMEOUT_MILLIS = 5_000L
        const val DEFAULT_FINISHED_MESSAGE = "Memo finished."
        const val KOREAN_FINISHED_MESSAGE = "메모가 완료되었습니다."
        const val DEFAULT_DELETED_MESSAGE = "Memo deleted."
        const val KOREAN_DELETED_MESSAGE = "메모가 삭제되었습니다."
        const val DEFAULT_UNDO_ACTION = "Undo"
        const val KOREAN_UNDO_ACTION = "실행 취소"
        const val DEFAULT_ADD_DESCRIPTION = "Add memo"
        const val DEFAULT_SCOPE_BUTTON_DESCRIPTION = "Display scope"
        const val DEFAULT_CHILD_SCOPE_LABEL = "Direct children"
        const val INDEPENDENT_MEMO_TITLE = "TagDetailIndependentMemo"
        const val DEFAULT_FINISHED_LIST_LABEL = "Finished memos"
        const val DEFAULT_TODAY_HEADER = "Today"
        const val SCREEN_MEMO_TITLE = "TagDetailScreenMemo"
        const val TODAY_MEMO_TITLE = "TodayMemo"
        const val FIRST_TIME_ZONE = "Pacific/Kiritimati"
        const val SECOND_TIME_ZONE = "Etc/GMT+12"
    }
}

private fun SemanticsNodeInteraction.performAddShortcut() {
    performKeyInput {
        keyDown(Key.MetaLeft)
        keyDown(Key.A)
        keyUp(Key.A)
        keyUp(Key.MetaLeft)
    }
}
