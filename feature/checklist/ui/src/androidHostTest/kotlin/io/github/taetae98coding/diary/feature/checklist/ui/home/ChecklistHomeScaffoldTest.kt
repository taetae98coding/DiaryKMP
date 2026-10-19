package io.github.taetae98coding.diary.feature.checklist.ui.home

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isDialog
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performKeyInput
import androidx.compose.ui.test.performScrollToIndex
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeDown
import androidx.paging.PagingData
import androidx.paging.compose.collectAsLazyPagingItems
import io.github.taetae98coding.diary.compose.core.empty.DIARY_EMPTY_BOX_TEST_TAG
import io.github.taetae98coding.diary.compose.core.pulltorefresh.PULL_TO_REFRESH_TEST_TAG
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.core.model.checklist.ChecklistSummary
import io.github.taetae98coding.diary.feature.checklist.ui.actionNameList
import io.github.taetae98coding.diary.feature.checklist.ui.card.CHECKLIST_CARD_TEST_TAG
import io.github.taetae98coding.diary.feature.checklist.ui.checklistPagingDataOf
import io.github.taetae98coding.diary.feature.checklist.ui.failedChecklistPagingData
import io.github.taetae98coding.diary.feature.checklist.ui.loadingChecklistPagingData
import io.github.taetae98coding.diary.feature.checklist.ui.refreshableList
import io.github.taetae98coding.diary.feature.checklist.ui.resetAndroidUiDispatcher
import io.github.taetae98coding.diary.feature.checklist.ui.testChecklistSummary
import io.github.taetae98coding.diary.feature.checklist.ui.visibleTextList
import io.github.taetae98coding.diary.feature.core.sync.SyncRefreshUiState
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w411dp-h891dp")
class ChecklistHomeScaffoldTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Before
    fun resetUiDispatcher() {
        resetAndroidUiDispatcher()
    }

    @Test
    @Config(qualifiers = "ko")
    fun `TC-CHECKLIST-HOME-FEATURE-001 한국어 환경에서 상단 바에 제목을 표시한다`() {
        setChecklistHomeScaffold()

        composeRule.onNodeWithText(KOREAN_TITLE).assertExists()
    }

    @Test
    @Config(qualifiers = "ko")
    fun `TC-CHECKLIST-HOME-FEATURE-013 한국어 환경에서 체크리스트 추가는 접근성 이름으로 제공된다`() {
        setChecklistHomeScaffold()

        composeRule.onNode(hasClickAction() and hasContentDescription(KOREAN_ADD_DESCRIPTION)).assertExists()
    }

    @Test
    fun `TC-CHECKLIST-HOME-FEATURE-013 기본 환경에서 체크리스트 추가는 접근성 이름으로 제공된다`() {
        setChecklistHomeScaffold()

        composeRule.onNode(hasClickAction() and hasContentDescription(DEFAULT_ADD_DESCRIPTION)).assertExists()
    }

    @Test
    fun `TC-CHECKLIST-HOME-FEATURE-006 계정에 저장된 체크리스트를 제목과 진행 정도로 표시한다`() {
        val summaryList = listOf(testChecklistSummary(checkedCount = 1, totalCount = 3), testChecklistSummary(checkedCount = 2, totalCount = 5))
        setChecklistHomeScaffold(pagingData = checklistPagingDataOf(summaryList))

        summaryList.forEach { summary ->
            composeRule
                .onNode(
                    hasTestTag(CHECKLIST_CARD_TEST_TAG) and
                        hasText(summary.checklist.detail.title) and
                        hasContentDescription(defaultProgressDescription(summary)),
                ).assertExists()
            composeRule.onNodeWithText("${summary.checkedCount}/${summary.totalCount}", useUnmergedTree = true).assertExists()
        }
    }

    @Test
    @Config(qualifiers = "ko")
    fun `TC-CHECKLIST-HOME-FEATURE-006 한국어 환경에서 진행 정도를 접근성 설명으로 읽는다`() {
        val summary = testChecklistSummary(checkedCount = 2, totalCount = 4)
        setChecklistHomeScaffold(pagingData = checklistPagingDataOf(listOf(summary)))

        composeRule.onNode(hasTestTag(CHECKLIST_CARD_TEST_TAG) and hasContentDescription("전체 4개 중 2개 체크")).assertExists()
    }

    @Test
    fun `체크 항목이 없는 체크리스트는 진행 정도를 0개 중 0개로 보인다`() {
        val summary = testChecklistSummary(checkedCount = 0, totalCount = 0)
        setChecklistHomeScaffold(pagingData = checklistPagingDataOf(listOf(summary)))

        composeRule.onNodeWithText("0/0", useUnmergedTree = true).assertExists()
    }

    @Test
    @Config(qualifiers = "ko")
    fun `TC-CHECKLIST-HOME-FEATURE-005 제목, 뒤로가기, 체크리스트 목록과 체크리스트 추가 외의 정보와 동작을 두지 않는다`() {
        val summary = testChecklistSummary(checkedCount = 1, totalCount = 2)
        setChecklistHomeScaffold(pagingData = checklistPagingDataOf(listOf(summary)))

        composeRule.visibleTextList() shouldContainExactlyInAnyOrder listOf(KOREAN_TITLE, summary.checklist.detail.title, "1/2")
        // 카드는 제목과 진행 정도 설명을 함께 읽으므로 진행 정도 설명이 그 카드의 이름으로 잡힌다.
        composeRule.actionNameList() shouldContainExactlyInAnyOrder listOf(KOREAN_NAVIGATE_UP_DESCRIPTION, KOREAN_ADD_DESCRIPTION, "전체 2개 중 1개 체크")
        composeRule.onNode(hasClickAction() and hasTestTag(CHECKLIST_CARD_TEST_TAG) and hasText(summary.checklist.detail.title)).assertExists()
    }

    @Test
    fun `TC-CHECKLIST-HOME-FEATURE-007 체크리스트 카드에는 제목과 진행 정도 외의 글을 표시하지 않는다`() {
        val summary = testChecklistSummary()
        setChecklistHomeScaffold(pagingData = checklistPagingDataOf(listOf(summary)))

        composeRule.onNode(hasTestTag(CHECKLIST_CARD_TEST_TAG) and hasText(summary.checklist.detail.title)).assertExists()
        composeRule.visibleTextList() shouldContainExactlyInAnyOrder
            listOf(DEFAULT_TITLE, summary.checklist.detail.title, "${summary.checkedCount}/${summary.totalCount}")
    }

    @Test
    fun `TC-CHECKLIST-HOME-FEATURE-009 목록의 체크리스트를 선택하면 그 체크리스트의 상세로 이동하는 이벤트를 한 번 내보낸다`() {
        val eventList = mutableListOf<ChecklistHomeScaffoldEvent>()
        val summaryList = listOf(testChecklistSummary(), testChecklistSummary())
        setChecklistHomeScaffold(pagingData = checklistPagingDataOf(summaryList), onEvent = eventList::add)

        composeRule
            .onNode(
                hasTestTag(CHECKLIST_CARD_TEST_TAG) and
                    hasText(
                        summaryList
                            .last()
                            .checklist.detail.title,
                    ),
            ).performClick()
        composeRule.waitForIdle()

        eventList shouldBe listOf(ChecklistHomeScaffoldEvent.ClickChecklist(id = summaryList.last().checklist.id))
    }

    @Test
    fun `TC-CHECKLIST-HOME-FEATURE-011 체크리스트가 표시된 목록에서 추가를 누르면 추가 이벤트를 한 번 내보낸다`() {
        assertAddClicked(pagingData = checklistPagingDataOf(listOf(testChecklistSummary())))
    }

    @Test
    fun `TC-CHECKLIST-HOME-FEATURE-011 빈 상태에서 추가를 누르면 추가 이벤트를 한 번 내보낸다`() {
        assertAddClicked(pagingData = checklistPagingDataOf(emptyList()))
    }

    @Test
    fun `TC-CHECKLIST-HOME-FEATURE-012 Cmd A 단축키를 입력하면 추가 이벤트를 한 번 내보낸다`() {
        val eventList = mutableListOf<ChecklistHomeScaffoldEvent>()
        setChecklistHomeScaffold(onEvent = eventList::add)

        composeRule.onRoot().performKeyInput {
            keyDown(Key.MetaLeft)
            keyDown(Key.A)
            keyUp(Key.A)
            keyUp(Key.MetaLeft)
        }
        composeRule.waitForIdle()

        eventList shouldBe listOf(ChecklistHomeScaffoldEvent.ClickAdd)
    }

    @Test
    fun `TC-CHECKLIST-HOME-FEATURE-003 뒤로가기를 누르면 뒤로가기 이벤트를 한 번 내보낸다`() {
        val eventList = mutableListOf<ChecklistHomeScaffoldEvent>()
        setChecklistHomeScaffold(onEvent = eventList::add)

        composeRule.onNodeWithContentDescription(DEFAULT_NAVIGATE_UP_DESCRIPTION).performClick()
        composeRule.waitForIdle()

        eventList shouldBe listOf(ChecklistHomeScaffoldEvent.ClickNavigateUp)
    }

    @Test
    @Config(qualifiers = "ko")
    fun `TC-CHECKLIST-HOME-FEATURE-014 표시할 체크리스트가 없으면 빈 상태 안내를 표시한다`() {
        setChecklistHomeScaffold(pagingData = checklistPagingDataOf(emptyList()))

        composeRule.onNodeWithTag(DIARY_EMPTY_BOX_TEST_TAG).assertExists()
        composeRule.onNodeWithText(KOREAN_EMPTY_TITLE).assertExists()
        composeRule.onNodeWithText(KOREAN_EMPTY_DESCRIPTION).assertExists()
    }

    @Test
    fun `TC-CHECKLIST-HOME-FEATURE-014 기본 환경에서 빈 상태 안내를 표시한다`() {
        setChecklistHomeScaffold(pagingData = checklistPagingDataOf(emptyList()))

        composeRule.onNodeWithText(DEFAULT_EMPTY_TITLE).assertExists()
        composeRule.onNodeWithText(DEFAULT_EMPTY_DESCRIPTION).assertExists()
    }

    @Test
    fun `TC-CHECKLIST-HOME-FEATURE-015 목록을 준비하는 동안에는 빈 상태 안내를 표시하지 않는다`() {
        setChecklistHomeScaffold(pagingData = loadingChecklistPagingData())

        composeRule.onNodeWithTag(DIARY_EMPTY_BOX_TEST_TAG).assertDoesNotExist()
        composeRule.onNodeWithText(DEFAULT_EMPTY_TITLE).assertDoesNotExist()
    }

    @Test
    fun `TC-CHECKLIST-HOME-FEATURE-016 표시할 체크리스트가 있으면 빈 상태 안내를 표시하지 않는다`() {
        val summary = testChecklistSummary()
        setChecklistHomeScaffold(pagingData = checklistPagingDataOf(listOf(summary)))

        composeRule.onNode(hasTestTag(CHECKLIST_CARD_TEST_TAG) and hasText(summary.checklist.detail.title)).assertExists()
        composeRule.onNodeWithTag(DIARY_EMPTY_BOX_TEST_TAG).assertDoesNotExist()
    }

    @Test
    fun `TC-CHECKLIST-HOME-DATA-002 목록을 조회하지 못하면 빈 상태 안내를 표시하고 오류를 알리지 않는다`() {
        val snackbarHostState = SnackbarHostState()
        setChecklistHomeScaffold(pagingData = failedChecklistPagingData(), snackbarHostState = snackbarHostState)

        composeRule.onNodeWithTag(DIARY_EMPTY_BOX_TEST_TAG).assertExists()
        composeRule.onNodeWithTag(CHECKLIST_CARD_TEST_TAG).assertDoesNotExist()
        composeRule.onAllNodes(isDialog()).assertCountEquals(0)
        composeRule.runOnIdle { snackbarHostState.currentSnackbarData shouldBe null }
    }

    @Test
    fun `TC-CHECKLIST-HOME-FEATURE-017 체크리스트가 표시된 목록을 당기면 새로고침을 요청한다`() {
        assertRefreshRequested(pagingData = checklistPagingDataOf(listOf(testChecklistSummary())))
    }

    @Test
    fun `TC-CHECKLIST-HOME-FEATURE-017 빈 상태 안내가 표시된 목록을 당기면 새로고침을 요청한다`() {
        assertRefreshRequested(pagingData = checklistPagingDataOf(emptyList()))
    }

    @Test
    fun `TC-CHECKLIST-HOME-FEATURE-018 새로고침이 실행되는 동안 진행이 표시되고 끝나면 사라진다`() {
        val isRefreshing = mutableStateOf(true)
        setChecklistHomeScaffold(
            pagingData = checklistPagingDataOf(listOf(testChecklistSummary())),
            uiStateProvider = { SyncRefreshUiState(isRefreshing = isRefreshing.value) },
        )
        composeRule.onNodeWithContentDescription(DEFAULT_REFRESHING_DESCRIPTION).assertExists()

        composeRule.runOnIdle { isRefreshing.value = false }
        composeRule.waitForIdle()

        composeRule.onNodeWithContentDescription(DEFAULT_REFRESHING_DESCRIPTION).assertDoesNotExist()
    }

    @Test
    fun `끝까지 스크롤하면 마지막 체크리스트 카드가 추가 버튼 위에 놓인다`() {
        val summaryList = List(SCROLL_COUNT) { testChecklistSummary() }
        setChecklistHomeScaffold(pagingData = checklistPagingDataOf(summaryList))

        composeRule.refreshableList().performScrollToIndex(summaryList.lastIndex)
        composeRule.waitForIdle()

        val lastCardBottom =
            composeRule
                .onAllNodesWithTag(CHECKLIST_CARD_TEST_TAG)
                .fetchSemanticsNodes()
                .maxOf { node -> node.boundsInRoot.bottom }
        val addButtonTop =
            composeRule
                .onNodeWithContentDescription(DEFAULT_ADD_DESCRIPTION)
                .fetchSemanticsNode()
                .boundsInRoot
                .top
        (lastCardBottom <= addButtonTop) shouldBe true
    }

    private fun assertAddClicked(pagingData: PagingData<ChecklistSummary>) {
        val eventList = mutableListOf<ChecklistHomeScaffoldEvent>()
        setChecklistHomeScaffold(pagingData = pagingData, onEvent = eventList::add)

        composeRule.onNodeWithContentDescription(DEFAULT_ADD_DESCRIPTION).performClick()
        composeRule.waitForIdle()

        eventList shouldBe listOf(ChecklistHomeScaffoldEvent.ClickAdd)
    }

    private fun assertRefreshRequested(pagingData: PagingData<ChecklistSummary>) {
        val eventList = mutableListOf<ChecklistHomeScaffoldEvent>()
        setChecklistHomeScaffold(pagingData = pagingData, onEvent = eventList::add)

        composeRule.onNodeWithTag(PULL_TO_REFRESH_TEST_TAG).performTouchInput { swipeDown() }
        composeRule.waitForIdle()

        eventList shouldBe listOf(ChecklistHomeScaffoldEvent.Refresh)
    }

    private fun setChecklistHomeScaffold(
        pagingData: PagingData<ChecklistSummary> = checklistPagingDataOf(emptyList()),
        onEvent: (ChecklistHomeScaffoldEvent) -> Unit = {},
        snackbarHostState: SnackbarHostState = SnackbarHostState(),
        uiStateProvider: () -> SyncRefreshUiState = { SyncRefreshUiState() },
    ) {
        val pagingDataFlow = MutableStateFlow(pagingData)

        composeRule.setContent {
            DiaryTheme {
                ChecklistHomeScaffold(
                    onEvent = onEvent,
                    snackbarHostState = snackbarHostState,
                    checklistPagingItems = pagingDataFlow.collectAsLazyPagingItems(),
                    uiStateProvider = uiStateProvider,
                )
            }
        }
        composeRule.waitForIdle()
    }

    private companion object {
        const val KOREAN_TITLE = "체크리스트"
        const val DEFAULT_TITLE = "Checklist"
        const val KOREAN_ADD_DESCRIPTION = "체크리스트 추가"
        const val KOREAN_NAVIGATE_UP_DESCRIPTION = "뒤로가기"
        const val DEFAULT_ADD_DESCRIPTION = "Add checklist"
        const val DEFAULT_NAVIGATE_UP_DESCRIPTION = "Navigate up"
        const val KOREAN_EMPTY_TITLE = "아직 체크리스트가 없습니다"
        const val KOREAN_EMPTY_DESCRIPTION = "추가 버튼으로 새 체크리스트를 만들 수 있습니다"
        const val DEFAULT_EMPTY_TITLE = "No checklists yet"
        const val DEFAULT_EMPTY_DESCRIPTION = "Use the add button to create a checklist."
        const val DEFAULT_REFRESHING_DESCRIPTION = "Refreshing"
        const val SCROLL_COUNT = 20

        fun defaultProgressDescription(summary: ChecklistSummary): String = "${summary.checkedCount} of ${summary.totalCount} checked"
    }
}
