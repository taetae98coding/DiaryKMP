package io.github.taetae98coding.diary.feature.tag.ui.detail.routine

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isDialog
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.longClick
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeDown
import androidx.compose.ui.test.swipeLeft
import androidx.compose.ui.test.swipeRight
import androidx.paging.LoadState
import androidx.paging.PagingData
import androidx.paging.compose.collectAsLazyPagingItems
import io.github.taetae98coding.diary.compose.core.empty.DIARY_EMPTY_BOX_TEST_TAG
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.compose.routine.ROUTINE_CARD_TEST_TAG
import io.github.taetae98coding.diary.compose.routine.RoutineListEvent
import io.github.taetae98coding.diary.compose.routine.SwipeRoutineCard
import io.github.taetae98coding.diary.compose.routine.rememberRoutineListState
import io.github.taetae98coding.diary.core.model.routine.RoutineCycle
import io.github.taetae98coding.diary.core.model.routine.RoutineSummary
import io.github.taetae98coding.diary.feature.tag.ui.refreshableList
import io.github.taetae98coding.diary.feature.tag.ui.resetAndroidUiDispatcher
import io.github.taetae98coding.diary.feature.tag.ui.tagEntityLoadingPagingData
import io.github.taetae98coding.diary.feature.tag.ui.tagEntityPagingData
import io.github.taetae98coding.diary.feature.tag.ui.tagRoutine
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.datetime.LocalDate
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w411dp-h891dp")
class TagDetailRoutineTabTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Before
    fun setUp() {
        resetAndroidUiDispatcher()
    }

    @Test
    fun `TC-c6eef3a9-261c-4525-98f7-dcfac6e409d0 대상 태그와 연결된 루틴을 목록에 표시한다`() {
        setRoutineTab(pagingData = tagEntityPagingData(itemList = listOf(tagRoutine(title = FIRST_TITLE), tagRoutine(title = SECOND_TITLE))))

        composeRule.refreshableList().assertExists()
        composeRule.onAllNodesWithTag(ROUTINE_CARD_TEST_TAG).assertCountEquals(2)
        composeRule.onNodeWithText(FIRST_TITLE).assertIsDisplayed()
        composeRule.onNodeWithText(SECOND_TITLE).assertIsDisplayed()
        composeRule.onNodeWithTag(DIARY_EMPTY_BOX_TEST_TAG).assertDoesNotExist()
    }

    @Test
    @Config(qualifiers = "ko")
    fun `TC-6ffa2548-a255-4425-8814-7ce97c0228fd 각 루틴의 제목과 카드 문구를 표시한다`() {
        val marchFirst = LocalDate(year = 2026, month = 3, day = 1)
        setRoutineTab(
            pagingData =
                tagEntityPagingData(
                    itemList =
                        listOf(
                            tagRoutine(title = WORKOUT_TITLE, startDate = marchFirst, cycle = RoutineCycle.WEEKLY, unit = "회", goalAmount = 3, cycleAmount = 2),
                            tagRoutine(title = RUNNING_TITLE, startDate = marchFirst, cycle = RoutineCycle.MONTHLY, unit = "m", cycleAmount = 12_000),
                            tagRoutine(title = READING_TITLE, startDate = LocalDate(year = 2026, month = 3, day = 21), goalAmount = 1),
                        ),
                ),
            today = LocalDate(year = 2026, month = 3, day = 20),
        )

        assertCard(title = WORKOUT_TITLE, text = WORKOUT_CARD_TEXT)
        assertCard(title = WORKOUT_TITLE, text = WORKOUT_RATE_TEXT)
        assertCard(title = RUNNING_TITLE, text = RUNNING_CARD_TEXT)
        assertCard(title = READING_TITLE, text = KOREAN_NOT_STARTED_TEXT)
    }

    @Test
    fun `TC-7db1d0bd-a553-40c2-b74b-6f2789f89ae8 진행 중인 루틴 항목에는 빠른 기록이 없고 선택과 완료와 삭제를 할 수 있다`() {
        val item = tagRoutine(title = FIRST_TITLE, goalAmount = 3)
        val eventList = mutableListOf<RoutineListEvent>()
        setRoutineTab(pagingData = tagEntityPagingData(itemList = listOf(item)), onRoutineListEvent = eventList::add, today = LocalDate(year = 2026, month = 3, day = 20))

        // 카드 안에는 카드 자신 말고 누를 수 있는 것이 없다.
        composeRule.onAllNodes(hasClickAction() and hasAnyAncestor(hasTestTag(ROUTINE_CARD_TEST_TAG)), useUnmergedTree = true).assertCountEquals(0)
        composeRule.onNodeWithTag(ROUTINE_CARD_TEST_TAG).performTouchInput { longClick() }
        composeRule.waitForIdle()
        composeRule.onAllNodes(isDialog()).assertCountEquals(0)

        composeRule.onNodeWithTag(ROUTINE_CARD_TEST_TAG).performClick()
        composeRule.onNodeWithTag(ROUTINE_CARD_TEST_TAG).performTouchInput { swipeRight() }
        composeRule.mainClock.advanceTimeBy(SWIPE_RESET_MILLIS)
        composeRule.waitForIdle()
        composeRule.onNodeWithTag(ROUTINE_CARD_TEST_TAG).performTouchInput { swipeLeft() }
        composeRule.waitForIdle()

        eventList.filterIsInstance<RoutineListEvent.ClickRoutine>().distinct() shouldBe listOf(RoutineListEvent.ClickRoutine(id = item.routine.id))
        eventList.filterIsInstance<RoutineListEvent.SwipeFinish>() shouldBe listOf(RoutineListEvent.SwipeFinish(id = item.routine.id))
        eventList.filterIsInstance<RoutineListEvent.SwipeDelete>() shouldBe listOf(RoutineListEvent.SwipeDelete(id = item.routine.id))
    }

    @Test
    fun `TC-d6db5574-94bc-44f0-8a9c-f6de4cfd94f1 목록은 전달된 순서 그대로 한 줄에 하나씩 표시한다`() {
        val titleList = listOf(FIRST_TITLE, SECOND_TITLE, THIRD_TITLE)
        setRoutineTab(pagingData = tagEntityPagingData(itemList = titleList.map { title -> tagRoutine(title = title) }))

        val topList =
            titleList.map { title ->
                composeRule
                    .onNodeWithText(title)
                    .fetchSemanticsNode()
                    .boundsInRoot
                    .top
            }

        topList shouldBe topList.sorted()
        topList.distinct().size shouldBe titleList.size
    }

    @Test
    fun `TC-ca88a9c0-daf9-479d-b6e7-c152cca20b12 루틴 탭은 정렬 선택을 제공하지 않는다`() {
        setRoutineTab(pagingData = tagEntityPagingData(itemList = listOf(tagRoutine(title = FIRST_TITLE))))

        composeRule.onNodeWithContentDescription(DEFAULT_SORT_DESCRIPTION).assertDoesNotExist()
    }

    @Test
    fun `TC-89855302-a22e-4d35-90b7-d537a23fe3a1 자리 표시 상태의 루틴은 선택해도 상세 이동을 요청하지 않는다`() {
        val eventList = mutableListOf<RoutineListEvent>()
        setPlaceholderCard(onEvent = eventList::add)

        composeRule.onNodeWithTag(ROUTINE_CARD_TEST_TAG).assertIsNotEnabled()
        composeRule.onNodeWithTag(ROUTINE_CARD_TEST_TAG).performClick()
        composeRule.waitForIdle()

        eventList shouldBe emptyList()
    }

    @Test
    fun `TC-cde1fc61-d194-4a93-9a54-35cda9ca6d45 아직 준비되지 않은 자리는 밀어도 삭제를 요청하지 않는다`() {
        val eventList = mutableListOf<RoutineListEvent>()
        setPlaceholderCard(onEvent = eventList::add)

        composeRule.onNodeWithTag(ROUTINE_CARD_TEST_TAG).performTouchInput { swipeLeft() }
        composeRule.waitForIdle()

        eventList shouldBe emptyList()
        composeRule.onNodeWithTag(ROUTINE_CARD_TEST_TAG).assertIsDisplayed()
    }

    @Test
    fun `TC-36a384cc-de03-4b96-afa5-908992377649 최초 조회 실패는 오류와 재시도를 표시하지 않는다`() {
        setRoutineTab(
            pagingData =
                tagEntityPagingData<RoutineSummary>(
                    itemList = emptyList(),
                    refresh = LoadState.Error(IllegalStateException("Refresh failed")),
                ),
        )

        composeRule.onAllNodesWithTag(ROUTINE_CARD_TEST_TAG).assertCountEquals(0)
        composeRule.onNodeWithText(DEFAULT_ERROR_TEXT).assertDoesNotExist()
        composeRule.onNodeWithText(DEFAULT_RETRY_TEXT).assertDoesNotExist()
    }

    @Test
    fun `TC-36a384cc-de03-4b96-afa5-908992377649 추가 조회 실패는 표시된 루틴을 유지한다`() {
        setRoutineTab(
            pagingData =
                tagEntityPagingData(
                    itemList = listOf(tagRoutine(title = FIRST_TITLE)),
                    append = LoadState.Error(IllegalStateException("Append failed")),
                ),
        )

        composeRule.onNodeWithText(FIRST_TITLE).assertIsDisplayed()
        composeRule.onAllNodesWithTag(ROUTINE_CARD_TEST_TAG).assertCountEquals(1)
        composeRule.onNodeWithText(DEFAULT_ERROR_TEXT).assertDoesNotExist()
        composeRule.onNodeWithText(DEFAULT_RETRY_TEXT).assertDoesNotExist()
    }

    @Test
    fun `TC-36a384cc-de03-4b96-afa5-908992377649 다시 조회가 실패해 빈 목록이 되면 이전 루틴을 남기지 않는다`() {
        val pagingDataFlow = MutableStateFlow(tagEntityPagingData(itemList = listOf(tagRoutine(title = FIRST_TITLE))))
        setRoutineTab(pagingDataFlow = pagingDataFlow)
        composeRule.onNodeWithText(FIRST_TITLE).assertIsDisplayed()

        composeRule.runOnIdle { pagingDataFlow.value = tagEntityPagingData(itemList = emptyList()) }
        composeRule.waitForIdle()

        composeRule.onNodeWithText(FIRST_TITLE).assertDoesNotExist()
        composeRule.onNodeWithText(DEFAULT_RETRY_TEXT).assertDoesNotExist()
    }

    @Test
    fun `TC-18af2db4-3af3-46cc-b4b8-62b88bf64a53 루틴을 선택하면 그 루틴의 상세 이동을 요청한다`() {
        val item = tagRoutine(title = FIRST_TITLE)
        val eventList = mutableListOf<RoutineListEvent>()
        setRoutineTab(pagingData = tagEntityPagingData(itemList = listOf(item)), onRoutineListEvent = eventList::add)

        composeRule.onNodeWithText(FIRST_TITLE).performClick()

        eventList shouldBe listOf(RoutineListEvent.ClickRoutine(id = item.routine.id))
    }

    @Test
    fun `TC-3bda7dc5-6f20-440d-ab47-aa056943fd49 연결된 루틴이 없으면 빈 상태 안내를 표시한다`() {
        setRoutineTab(pagingData = tagEntityPagingData(itemList = emptyList()))

        composeRule.onNodeWithTag(DIARY_EMPTY_BOX_TEST_TAG).assertExists()
        composeRule.onNodeWithText(DEFAULT_EMPTY_TITLE).assertExists()
        composeRule.onNodeWithText(DEFAULT_EMPTY_DESCRIPTION).assertExists()
        composeRule.refreshableList().assertDoesNotExist()
    }

    @Test
    @Config(qualifiers = "ko")
    fun `TC-3bda7dc5-6f20-440d-ab47-aa056943fd49 한국어 환경에서 빈 상태 안내를 표시한다`() {
        setRoutineTab(pagingData = tagEntityPagingData(itemList = emptyList()))

        composeRule.onNodeWithText(KOREAN_EMPTY_TITLE).assertExists()
        composeRule.onNodeWithText(KOREAN_EMPTY_DESCRIPTION).assertExists()
    }

    @Test
    fun `TC-c03267fd-3424-42d9-9170-3c9341b4a867 목록을 준비하는 동안에는 빈 상태 안내를 표시하지 않는다`() {
        setRoutineTab(pagingData = tagEntityLoadingPagingData())

        composeRule.onNodeWithTag(DIARY_EMPTY_BOX_TEST_TAG).assertDoesNotExist()
    }

    @Test
    fun `TC-45005d24-e175-40e7-914d-b32ee895a652 목록을 당기면 새로고침을 요청한다`() {
        val eventList = mutableListOf<TagDetailRoutineContentEvent>()
        setRoutineTab(pagingData = tagEntityPagingData(itemList = listOf(tagRoutine(title = FIRST_TITLE))), onEvent = eventList::add)

        composeRule.refreshableList().performTouchInput { swipeDown() }
        composeRule.waitForIdle()

        eventList shouldBe listOf(TagDetailRoutineContentEvent.Refresh)
    }

    @Test
    fun `TC-45005d24-e175-40e7-914d-b32ee895a652 빈 상태에서도 목록을 당기면 새로고침을 요청한다`() {
        val eventList = mutableListOf<TagDetailRoutineContentEvent>()
        setRoutineTab(pagingData = tagEntityPagingData(itemList = emptyList()), onEvent = eventList::add)

        composeRule.onNodeWithTag(DIARY_EMPTY_BOX_TEST_TAG).performTouchInput { swipeDown() }
        composeRule.waitForIdle()

        eventList shouldBe listOf(TagDetailRoutineContentEvent.Refresh)
    }

    @Test
    fun `TC-45005d24-e175-40e7-914d-b32ee895a652 루틴이 표시된 채 새로고침이 진행 중이면 진행 표시가 나타나고 끝나면 사라진다`() {
        assertRefreshingIndicator(pagingData = tagEntityPagingData(itemList = listOf(tagRoutine(title = FIRST_TITLE))))
    }

    @Test
    fun `TC-45005d24-e175-40e7-914d-b32ee895a652 빈 상태에서 새로고침이 진행 중이면 진행 표시가 나타나고 끝나면 사라진다`() {
        assertRefreshingIndicator(pagingData = tagEntityPagingData(itemList = emptyList()))
    }

    @Test
    fun `TC-78c2f045-7fd9-423f-ba6d-5de1e37027d2 루틴 카드를 삭제 방향으로 밀면 그 루틴의 삭제를 전달한다`() {
        val item = tagRoutine(title = FIRST_TITLE)
        val eventList = mutableListOf<RoutineListEvent>()
        setRoutineTab(pagingData = tagEntityPagingData(itemList = listOf(item)), onRoutineListEvent = eventList::add)

        composeRule.onNodeWithTag(ROUTINE_CARD_TEST_TAG).performTouchInput { swipeLeft() }
        composeRule.waitForIdle()

        eventList shouldBe listOf(RoutineListEvent.SwipeDelete(id = item.routine.id))
    }

    @Test
    fun `TC-648019bb-ee48-40e7-9ad8-62a28ae546d8 루틴 카드를 완료 방향으로 밀면 그 루틴의 완료만 전달한다`() {
        val item = tagRoutine(title = FIRST_TITLE)
        val eventList = mutableListOf<RoutineListEvent>()
        setRoutineTab(pagingData = tagEntityPagingData(itemList = listOf(item)), onRoutineListEvent = eventList::add)

        composeRule.onNodeWithTag(ROUTINE_CARD_TEST_TAG).performTouchInput { swipeRight() }
        composeRule.waitForIdle()

        eventList shouldBe listOf(RoutineListEvent.SwipeFinish(id = item.routine.id))
        composeRule.onAllNodes(isDialog()).assertCountEquals(0)
    }

    @Test
    fun `TC-042d8083-1f81-4269-a5ee-bfc36cd23c62 아직 준비되지 않은 자리는 밀어도 완료를 요청하지 않는다`() {
        val eventList = mutableListOf<RoutineListEvent>()
        setPlaceholderCard(onEvent = eventList::add)

        composeRule.onNodeWithTag(ROUTINE_CARD_TEST_TAG).performTouchInput { swipeRight() }
        composeRule.waitForIdle()

        eventList shouldBe emptyList()
        composeRule.onNodeWithTag(ROUTINE_CARD_TEST_TAG).assertIsDisplayed()
    }

    @Test
    fun `TC-bc592962-fcae-45cb-b7c6-f61d2f3295b8 완료된 루틴 확인을 누르면 완료된 루틴 목록 이동을 요청한다`() {
        val eventList = mutableListOf<TagDetailRoutineContentEvent>()
        setRoutineTab(pagingData = tagEntityPagingData(itemList = listOf(tagRoutine(title = FIRST_TITLE))), onEvent = eventList::add)

        composeRule.onNodeWithText(DEFAULT_FINISHED_LIST_LABEL).performClick()

        eventList shouldBe listOf(TagDetailRoutineContentEvent.ClickFinishedList)
    }

    @Test
    fun `TC-7bb0cca0-dfc0-4de4-9e4d-0a5fb45ac8c9 빈 상태에서도 완료된 루틴 확인을 실행할 수 있다`() {
        val eventList = mutableListOf<TagDetailRoutineContentEvent>()
        setRoutineTab(pagingData = tagEntityPagingData(itemList = emptyList()), onEvent = eventList::add)
        composeRule.onNodeWithTag(DIARY_EMPTY_BOX_TEST_TAG).assertExists()

        composeRule.onNodeWithText(DEFAULT_FINISHED_LIST_LABEL).performClick()

        eventList shouldBe listOf(TagDetailRoutineContentEvent.ClickFinishedList)
    }

    @Test
    @Config(qualifiers = "ko")
    fun `TC-bc592962-fcae-45cb-b7c6-f61d2f3295b8 한국어 환경에서 완료된 루틴 진입 라벨을 표시한다`() {
        setRoutineTab(pagingData = tagEntityPagingData(itemList = emptyList()))

        composeRule.onNodeWithText(KOREAN_FINISHED_LIST_LABEL).assertIsDisplayed()
    }

    @Test
    @Config(qualifiers = "ko")
    fun `TC-ddbeec1c-afd0-43d6-81e4-5940ae265442 기록이 바뀐 루틴은 카드 문구가 바뀐다`() {
        val workout = tagRoutine(title = WORKOUT_TITLE, cycle = RoutineCycle.WEEKLY, unit = "회", goalAmount = 3, cycleAmount = 2)
        val pagingDataFlow = MutableStateFlow(tagEntityPagingData(itemList = listOf(workout)))
        setRoutineTab(pagingDataFlow = pagingDataFlow, today = LocalDate(year = 2026, month = 3, day = 20))
        assertCard(title = WORKOUT_TITLE, text = WORKOUT_CARD_TEXT)

        composeRule.runOnIdle { pagingDataFlow.value = tagEntityPagingData(itemList = listOf(workout.copy(cycleAmount = 3))) }
        composeRule.waitForIdle()

        assertCard(title = WORKOUT_TITLE, text = CHANGED_WORKOUT_CARD_TEXT)
        composeRule.onNodeWithText(WORKOUT_CARD_TEXT).assertDoesNotExist()
    }

    private fun assertCard(
        title: String,
        text: String,
    ) {
        composeRule.onNode(hasTestTag(ROUTINE_CARD_TEST_TAG) and hasText(title) and hasText(text)).assertIsDisplayed()
    }

    private fun assertRefreshingIndicator(pagingData: PagingData<RoutineSummary>) {
        val isRefreshing = mutableStateOf(true)
        setRoutineTab(pagingDataFlow = MutableStateFlow(pagingData), isRefreshingProvider = { isRefreshing.value })

        composeRule.onNodeWithContentDescription(DEFAULT_REFRESHING_DESCRIPTION).assertExists()

        composeRule.runOnIdle { isRefreshing.value = false }
        composeRule.waitForIdle()

        composeRule.onNodeWithContentDescription(DEFAULT_REFRESHING_DESCRIPTION).assertDoesNotExist()
    }

    private fun setPlaceholderCard(onEvent: (RoutineListEvent) -> Unit) {
        // 목록은 아직 준비되지 않은 자리를 루틴 없이 같은 카드로 그린다.
        composeRule.setContent {
            DiaryTheme {
                SwipeRoutineCard(
                    onEvent = onEvent,
                    modifier = Modifier.fillMaxWidth(),
                    summary = null,
                )
            }
        }
    }

    private fun setRoutineTab(
        pagingData: PagingData<RoutineSummary> = PagingData.empty(),
        onEvent: (TagDetailRoutineContentEvent) -> Unit = {},
        onRoutineListEvent: (RoutineListEvent) -> Unit = {},
        today: LocalDate? = null,
    ) {
        setRoutineTab(pagingDataFlow = MutableStateFlow(pagingData), onEvent = onEvent, onRoutineListEvent = onRoutineListEvent, today = today)
    }

    private fun setRoutineTab(
        pagingDataFlow: MutableStateFlow<PagingData<RoutineSummary>>,
        onEvent: (TagDetailRoutineContentEvent) -> Unit = {},
        onRoutineListEvent: (RoutineListEvent) -> Unit = {},
        today: LocalDate? = null,
        isRefreshingProvider: () -> Boolean = { false },
    ) {
        composeRule.setContent {
            DiaryTheme {
                TagDetailRoutineTab(
                    onEvent = onEvent,
                    onRoutineListEvent = onRoutineListEvent,
                    modifier = Modifier.fillMaxSize(),
                    listState = rememberRoutineListState(initialToday = today),
                    routinePagingItems = pagingDataFlow.collectAsLazyPagingItems(),
                    isRefreshingProvider = isRefreshingProvider,
                )
            }
        }
    }

    private companion object {
        const val FIRST_TITLE = "AlphaRoutine"
        const val SECOND_TITLE = "BetaRoutine"
        const val THIRD_TITLE = "GammaRoutine"
        const val WORKOUT_TITLE = "운동"
        const val WORKOUT_CARD_TEXT = "이번 주 2 / 3회"
        const val WORKOUT_RATE_TEXT = "66%"
        const val CHANGED_WORKOUT_CARD_TEXT = "이번 주 3 / 3회"
        const val RUNNING_TITLE = "달리기"
        const val RUNNING_CARD_TEXT = "이번 달 12000m"
        const val READING_TITLE = "독서"
        const val KOREAN_NOT_STARTED_TEXT = "시작 전"
        const val SWIPE_RESET_MILLIS = 2_000L
        const val DEFAULT_FINISHED_LIST_LABEL = "Finished routines"
        const val KOREAN_FINISHED_LIST_LABEL = "완료된 루틴"
        const val DEFAULT_EMPTY_TITLE = "No routines linked to this tag"
        const val DEFAULT_EMPTY_DESCRIPTION = "Use the add button to create a routine."
        const val KOREAN_EMPTY_TITLE = "이 태그에 연결된 루틴이 없습니다"
        const val KOREAN_EMPTY_DESCRIPTION = "추가 버튼으로 새 루틴을 만들 수 있습니다"
        const val DEFAULT_ERROR_TEXT = "Error"
        const val DEFAULT_RETRY_TEXT = "Retry"
        const val DEFAULT_REFRESHING_DESCRIPTION = "Refreshing"
        const val DEFAULT_SORT_DESCRIPTION = "List sort"
    }
}
