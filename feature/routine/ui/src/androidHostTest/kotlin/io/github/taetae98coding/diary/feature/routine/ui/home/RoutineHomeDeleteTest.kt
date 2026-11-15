package io.github.taetae98coding.diary.feature.routine.ui.home

import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.isDialog
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeLeft
import androidx.compose.ui.test.swipeRight
import androidx.paging.PagingData
import io.github.taetae98coding.diary.compose.core.empty.DIARY_EMPTY_BOX_TEST_TAG
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.compose.routine.RoutineListEffect
import io.github.taetae98coding.diary.core.model.routine.CalendarRoutineRecord
import io.github.taetae98coding.diary.core.model.routine.RoutineCycle
import io.github.taetae98coding.diary.core.model.routine.RoutineSummary
import io.github.taetae98coding.diary.domain.core.invoke
import io.github.taetae98coding.diary.domain.routine.usecase.DeleteRoutineUseCase
import io.github.taetae98coding.diary.domain.routine.usecase.GetRoutineFilterTagIdUseCase
import io.github.taetae98coding.diary.domain.routine.usecase.GetRoutineFilterUseCase
import io.github.taetae98coding.diary.domain.routine.usecase.PageViewScopeRoutineUseCase
import io.github.taetae98coding.diary.feature.routine.ui.home.calendar.ROUTINE_HOME_CALENDAR_RECORD_TEST_TAG
import io.github.taetae98coding.diary.feature.routine.ui.home.calendar.RoutineHomeCalendarUiState
import io.github.taetae98coding.diary.feature.routine.ui.home.calendar.RoutineHomeCalendarViewModel
import io.github.taetae98coding.diary.feature.routine.ui.resetAndroidUiDispatcher
import io.github.taetae98coding.diary.feature.routine.ui.routineHomeCalendarViewModel
import io.github.taetae98coding.diary.feature.routine.ui.routineHomeHolidayViewModel
import io.github.taetae98coding.diary.feature.routine.ui.routineSyncRefreshViewModel
import io.github.taetae98coding.diary.feature.routine.ui.routineViewScopeUseCase
import io.github.taetae98coding.diary.feature.routine.ui.unfinishedRoutineExistsUseCase
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next
import io.kotest.property.arbitrary.string
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.todayIn
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.time.Clock
import kotlin.uuid.Uuid

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w411dp-h891dp")
class RoutineHomeDeleteTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Before
    fun resetUiDispatcher() {
        resetAndroidUiDispatcher()
    }

    @Test
    @Config(qualifiers = "ko")
    fun `TC-b2aa114d-4bae-48c8-96e0-c52dca307491 루틴 카드를 삭제 방향으로 밀면 삭제되고 안내와 실행 취소가 표시된다`() {
        val environment = setRoutineHomeScreen(listOf(filterCaseRoutineSummaryOf(title = FIRST_TITLE), filterCaseRoutineSummaryOf(title = SECOND_TITLE)))
        val target = environment.summaryList.first()

        swipeCard(title = FIRST_TITLE)

        verify(exactly = 1) { environment.viewModel.delete(id = target.routine.id) }
        composeRule.onNode(isDialog()).assertDoesNotExist()
        composeRule.onNode(filterCaseRoutineCard(FIRST_TITLE)).assertDoesNotExist()
        composeRule.onNodeWithText(KOREAN_DELETED_MESSAGE).assertExists()
        composeRule.onNodeWithText(KOREAN_UNDO_ACTION).assertExists()
    }

    @Test
    fun `TC-b2aa114d-4bae-48c8-96e0-c52dca307491 기본 환경에서 삭제 안내와 실행 취소를 표시한다`() {
        val environment = setRoutineHomeScreen(listOf(filterCaseRoutineSummaryOf(title = FIRST_TITLE)))

        swipeCard(title = FIRST_TITLE)

        verify(exactly = 1) {
            environment.viewModel.delete(
                id =
                    environment.summaryList
                        .first()
                        .routine.id,
            )
        }
        composeRule.onNodeWithText(DEFAULT_DELETED_MESSAGE).assertExists()
        composeRule.onNodeWithText(DEFAULT_UNDO_ACTION).assertExists()
    }

    @Test
    @Config(qualifiers = "ko")
    fun `TC-86e61dbb-b1cb-4bd0-b9af-03d709d0b17c 삭제를 실행 취소하면 목록 정렬에 맞는 자리에 삭제 전의 카드 문구로 다시 나타난다`() {
        val summaryList =
            listOf(
                filterCaseRoutineSummaryOf(title = KOREAN_FIRST_TITLE),
                filterCaseRoutineSummaryOf(title = KOREAN_SECOND_TITLE, cycleAmount = 1_500L) { detail ->
                    detail.copy(cycle = RoutineCycle.DAILY, unit = "ml", goalAmount = 2_000)
                },
                filterCaseRoutineSummaryOf(title = KOREAN_THIRD_TITLE),
            )
        val environment = setRoutineHomeScreen(summaryList)
        val target = summaryList[1]
        composeRule.onNode(filterCaseRoutineCard(KOREAN_SECOND_TITLE)).assertTextContainsAll(KOREAN_CARD_TEXT, KOREAN_ACHIEVEMENT_RATE)

        swipeCard(title = KOREAN_SECOND_TITLE)
        composeRule.onNode(filterCaseRoutineCard(KOREAN_SECOND_TITLE)).assertDoesNotExist()
        composeRule.onNodeWithText(KOREAN_UNDO_ACTION).performClick()
        composeRule.waitUntil(timeoutMillis = TIMEOUT_MILLIS) {
            composeRule.onAllNodesWithText(KOREAN_SECOND_TITLE).fetchSemanticsNodes().isNotEmpty()
        }

        verify(exactly = 1) { environment.viewModel.restore(id = target.routine.id) }
        composeRule.onNode(filterCaseRoutineCard(KOREAN_SECOND_TITLE)).assertTextContainsAll(KOREAN_CARD_TEXT, KOREAN_ACHIEVEMENT_RATE)
        composeRule.filterCaseCardTitleList(summaryList.map { summary -> summary.routine.detail.title }) shouldBe listOf(KOREAN_FIRST_TITLE, KOREAN_SECOND_TITLE, KOREAN_THIRD_TITLE)
    }

    @Test
    fun `TC-7431fc32-44af-40eb-86b9-592e8dc4d72f 안내가 보이는 동안 다른 루틴을 삭제하면 마지막 삭제의 안내만 남는다`() {
        val environment = setRoutineHomeScreen(listOf(filterCaseRoutineSummaryOf(title = FIRST_TITLE), filterCaseRoutineSummaryOf(title = SECOND_TITLE)))
        val (first, second) = environment.summaryList

        swipeCard(title = FIRST_TITLE)
        composeRule.onNodeWithText(DEFAULT_DELETED_MESSAGE).assertExists()
        swipeCard(title = SECOND_TITLE)

        composeRule.onAllNodesWithText(DEFAULT_DELETED_MESSAGE).fetchSemanticsNodes().size shouldBe 1
        composeRule.onNodeWithText(DEFAULT_UNDO_ACTION).performClick()
        composeRule.waitForIdle()

        verify(exactly = 1) { environment.viewModel.restore(id = second.routine.id) }
        verify(exactly = 0) { environment.viewModel.restore(id = first.routine.id) }
    }

    @Test
    fun `TC-00e20165-67a0-4248-9139-014e8422cc4c 삭제가 저장되지 못하면 안내를 표시하지 않는다`() {
        val summary = filterCaseRoutineSummaryOf(title = FIRST_TITLE)
        val pageRoutineUseCase = mockk<PageViewScopeRoutineUseCase>()
        every { pageRoutineUseCase(parameter = any()) } returns flowOf(Result.success(filterCaseRoutinePagingDataOf(listOf(summary))))
        val getRoutineFilterUseCase = mockk<GetRoutineFilterUseCase>()
        every { getRoutineFilterUseCase() } returns flowOf(Result.success(emptyList()))
        val getRoutineFilterTagIdUseCase = mockk<GetRoutineFilterTagIdUseCase>()
        every { getRoutineFilterTagIdUseCase() } returns flowOf(Result.success(emptySet()))
        val deleteRoutineUseCase = mockk<DeleteRoutineUseCase>()
        coEvery { deleteRoutineUseCase(parameter = any()) } returns Result.failure(IllegalStateException(Arb.string().next()))
        val viewModel =
            RoutineHomeViewModel(
                getRoutineFilterUseCase = getRoutineFilterUseCase,
                getRoutineFilterTagIdUseCase = getRoutineFilterTagIdUseCase,
                getRoutineViewScopeUseCase = routineViewScopeUseCase(),
                getUnfinishedRoutineExistsUseCase = unfinishedRoutineExistsUseCase(),
                pageViewScopeRoutineUseCase = pageRoutineUseCase,
                finishRoutineUseCase = mockk(),
                restartRoutineUseCase = mockk(),
                deleteRoutineUseCase = deleteRoutineUseCase,
                restoreRoutineUseCase = mockk(),
                addQuickRoutineRecordUseCase = mockk(),
                deleteRoutineRecordUseCase = mockk(),
            )
        setRoutineHomeScreenContent(viewModel = viewModel, calendarViewModel = routineHomeCalendarViewModel(), firstTitle = FIRST_TITLE)

        swipeCard(title = FIRST_TITLE)

        coVerify(exactly = 1) { deleteRoutineUseCase(parameter = summary.routine.id) }
        composeRule.onNodeWithText(DEFAULT_DELETED_MESSAGE).assertDoesNotExist()
        composeRule.onNodeWithText(DEFAULT_UNDO_ACTION).assertDoesNotExist()
        composeRule.onNode(filterCaseRoutineCard(FIRST_TITLE)).assertIsDisplayed()
    }

    @Test
    fun `TC-ef54e369-132e-40a9-8cf6-0a4150a55850 실행 취소를 선택하지 않으면 삭제 안내가 잠시 뒤 사라지고 되돌릴 수 없다`() {
        val environment = setRoutineHomeScreen(listOf(filterCaseRoutineSummaryOf(title = FIRST_TITLE)))

        swipeCard(title = FIRST_TITLE)
        composeRule.onNodeWithText(DEFAULT_DELETED_MESSAGE).assertExists()

        composeRule.mainClock.advanceTimeBy(AFTER_UNDO_SNACKBAR_DISMISS_MILLIS)
        composeRule.waitForIdle()

        composeRule.onNodeWithText(DEFAULT_DELETED_MESSAGE).assertDoesNotExist()
        composeRule.onNodeWithText(DEFAULT_UNDO_ACTION).assertDoesNotExist()
        verify(exactly = 0) { environment.viewModel.restore(id = any()) }
    }

    @Test
    fun `TC-bcc0a9ae-1a36-4bb7-b201-1188c40a114b 마지막 루틴이 사라지면 빈 상태가 되고 실행 취소로 돌아오면 빈 상태가 사라진다`() {
        val environment = setRoutineHomeScreen(listOf(filterCaseRoutineSummaryOf(title = FIRST_TITLE)))

        swipeCard(title = FIRST_TITLE)
        composeRule.waitUntil(timeoutMillis = TIMEOUT_MILLIS) {
            composeRule.onAllNodesWithText(DEFAULT_EMPTY_TITLE).fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithTag(DIARY_EMPTY_BOX_TEST_TAG).assertExists()

        composeRule.onNodeWithText(DEFAULT_UNDO_ACTION).performClick()
        composeRule.waitUntil(timeoutMillis = TIMEOUT_MILLIS) {
            composeRule.onAllNodesWithText(FIRST_TITLE).fetchSemanticsNodes().isNotEmpty()
        }

        verify(exactly = 1) {
            environment.viewModel.restore(
                id =
                    environment.summaryList
                        .first()
                        .routine.id,
            )
        }
        composeRule.onNodeWithTag(DIARY_EMPTY_BOX_TEST_TAG).assertDoesNotExist()
        composeRule.onNode(filterCaseRoutineCard(FIRST_TITLE)).assertIsDisplayed()
    }

    @Test
    fun `TC-e6cd90ec-c02d-40a6-b11e-13e858e2ecc9 루틴을 삭제하고 실행 취소하지 않으면 그 루틴의 기록이 달력에서 사라진다`() {
        setRoutineHomeScreen(listOf(filterCaseRoutineSummaryOf(title = FIRST_TITLE), filterCaseRoutineSummaryOf(title = SECOND_TITLE)))

        swipeCard(title = FIRST_TITLE)
        showCalendar()

        calendarRecordDescriptionList() shouldBe recordDescriptionSet(title = SECOND_TITLE)
    }

    @Test
    fun `TC-e6cd90ec-c02d-40a6-b11e-13e858e2ecc9 루틴을 삭제하고 실행 취소하면 그 루틴의 기록이 달력에 다시 나타난다`() {
        setRoutineHomeScreen(listOf(filterCaseRoutineSummaryOf(title = FIRST_TITLE), filterCaseRoutineSummaryOf(title = SECOND_TITLE)))

        swipeCard(title = FIRST_TITLE)
        composeRule.onNodeWithText(DEFAULT_UNDO_ACTION).performClick()
        composeRule.waitUntil(timeoutMillis = TIMEOUT_MILLIS) {
            composeRule.onAllNodesWithText(FIRST_TITLE).fetchSemanticsNodes().isNotEmpty()
        }
        showCalendar()

        calendarRecordDescriptionList() shouldBe recordDescriptionSet(title = FIRST_TITLE) + recordDescriptionSet(title = SECOND_TITLE)
    }

    @Test
    fun `TC-91f8781a-4df5-4e2b-925d-d8903fd3c68e 달력 보기에서는 루틴 기록을 밀어도 삭제하지 않는다`() {
        val environment = setRoutineHomeScreen(listOf(filterCaseRoutineSummaryOf(title = FIRST_TITLE)))
        showCalendar()

        composeRule.onAllNodes(hasTestTag(ROUTINE_HOME_CALENDAR_RECORD_TEST_TAG))[0].performTouchInput { swipeLeft() }
        composeRule.waitForIdle()

        verify(exactly = 0) { environment.viewModel.delete(id = any()) }
        verify(exactly = 0) { environment.viewModel.undoRecord(recordId = any()) }
        composeRule.onNodeWithText(DEFAULT_DELETED_MESSAGE).assertDoesNotExist()
        composeRule.onNodeWithText(DEFAULT_UNDO_ACTION).assertDoesNotExist()
    }

    // 두 날짜의 양이 달라 날짜마다 다른 접근성 이름을 가진다.
    private fun recordDescriptionSet(title: String): Set<String> = setOf("$title ${TODAY_RECORD_AMOUNT}ml, routine", "$title ${OTHER_RECORD_AMOUNT}ml, routine")

    private fun swipeCard(title: String) {
        composeRule.onNode(filterCaseRoutineCard(title)).performTouchInput { swipeLeft() }
        composeRule.waitForIdle()
    }

    private fun showCalendar() {
        composeRule.onNodeWithContentDescription(DEFAULT_CALENDAR_VIEW_MODE_DESCRIPTION).performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithContentDescription(DEFAULT_TODAY_DESCRIPTION).assertExists()
    }

    // 달력은 이웃한 달의 주를 함께 그릴 수 있어 같은 기록이 두 번 놓일 수 있으므로 집합으로 비교한다.
    private fun calendarRecordDescriptionList(): Set<String> =
        composeRule
            .onAllNodes(hasTestTag(ROUTINE_HOME_CALENDAR_RECORD_TEST_TAG))
            .fetchSemanticsNodes()
            .map { node ->
                node.config[SemanticsProperties.ContentDescription].joinToString()
            }.toSet()

    private fun SemanticsNodeInteraction.assertTextContainsAll(vararg textList: String) {
        val nodeTextList = fetchSemanticsNode().config[SemanticsProperties.Text].map { text -> text.text }

        textList.forEach { text -> (text in nodeTextList) shouldBe true }
    }

    // 삭제와 실행 취소가 저장되면 저장소의 목록과 달력의 기록이 바뀌어 조회 결과가 다시 전달되는 것을 테스트가 대신한다.
    private fun setRoutineHomeScreen(summaryList: List<RoutineSummary>): Environment {
        val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
        val recordList =
            summaryList.flatMap { summary ->
                // 오늘이 속한 달의 두 날짜에 기록이 있다. 달의 첫날과 마지막 날 근처에서도 같은 달에 머물게 한다.
                val otherDate = if (today.day > 1) today.minus(1, DateTimeUnit.DAY) else today.plus(1, DateTimeUnit.DAY)

                listOf(today to TODAY_RECORD_AMOUNT, otherDate to OTHER_RECORD_AMOUNT).map { (date, amount) ->
                    CalendarRoutineRecord(
                        routineId = summary.routine.id,
                        title = summary.routine.detail.title,
                        color = summary.routine.detail.color,
                        date = date,
                        amount = amount,
                        unit = "ml",
                    )
                }
            }
        val visibleIdSet = MutableStateFlow(summaryList.map { summary -> summary.routine.id }.toSet())
        val pagingDataFlow = MutableStateFlow<PagingData<RoutineSummary>>(filterCaseRoutinePagingDataOf(summaryList))
        val calendarUiState = MutableStateFlow(RoutineHomeCalendarUiState(recordList = recordList))
        val effectChannel = Channel<RoutineListEffect>(capacity = Channel.BUFFERED)
        val viewModel = mockk<RoutineHomeViewModel>(relaxed = true)
        every { viewModel.routinePagingData } returns pagingDataFlow
        every { viewModel.filterUiState } returns MutableStateFlow(RoutineHomeScaffoldFilterUiState())
        every { viewModel.effect } returns effectChannel.receiveAsFlow()
        every { viewModel.recordEffect } returns emptyFlow()
        val calendarViewModel = mockk<RoutineHomeCalendarViewModel>(relaxed = true)
        every { calendarViewModel.uiState } returns calendarUiState

        fun publish(idSet: Set<Uuid>) {
            visibleIdSet.value = idSet
            pagingDataFlow.value = filterCaseRoutinePagingDataOf(summaryList.filter { summary -> summary.routine.id in idSet })
            calendarUiState.value = RoutineHomeCalendarUiState(recordList = recordList.filter { record -> record.routineId in idSet })
        }
        every { viewModel.delete(id = any()) } answers {
            publish(visibleIdSet.value - firstArg<Uuid>())
            effectChannel.trySend(RoutineListEffect.Deleted(id = firstArg())).getOrThrow()
        }
        every { viewModel.restore(id = any()) } answers { publish(visibleIdSet.value + firstArg<Uuid>()) }
        every { viewModel.undo(effect = any()) } answers { callOriginal() }

        setRoutineHomeScreenContent(
            viewModel = viewModel,
            calendarViewModel = calendarViewModel,
            firstTitle =
                summaryList
                    .first()
                    .routine.detail.title,
        )

        return Environment(viewModel = viewModel, summaryList = summaryList)
    }

    private fun setRoutineHomeScreenContent(
        viewModel: RoutineHomeViewModel,
        calendarViewModel: RoutineHomeCalendarViewModel,
        firstTitle: String,
    ) {
        val holidayViewModel = routineHomeHolidayViewModel()
        val syncViewModel = routineSyncRefreshViewModel()

        composeRule.setContent {
            DiaryTheme {
                RoutineHomeScreen(
                    navigateToAdd = {},
                    navigateToDetail = {},
                    navigateToFilter = {},
                    navigateToFinishedList = {},
                    componentVisibleProvider = { RoutineHomeScaffoldComponentVisible() },
                    homeReselectEvent = emptyFlow(),
                    routineViewModel = viewModel,
                    calendarViewModel = calendarViewModel,
                    holidayViewModel = holidayViewModel,
                    syncViewModel = syncViewModel,
                )
            }
        }
        composeRule.waitUntil(timeoutMillis = TIMEOUT_MILLIS) {
            composeRule.onAllNodesWithText(firstTitle).fetchSemanticsNodes().isNotEmpty()
        }
    }

    private class Environment(
        val viewModel: RoutineHomeViewModel,
        val summaryList: List<RoutineSummary>,
    )

    private companion object {
        const val DEFAULT_DELETED_MESSAGE = "Routine deleted."
        const val KOREAN_DELETED_MESSAGE = "루틴이 삭제되었습니다."
        const val DEFAULT_UNDO_ACTION = "Undo"
        const val KOREAN_UNDO_ACTION = "실행 취소"
        const val DEFAULT_EMPTY_TITLE = "No routines yet"
        const val DEFAULT_CALENDAR_VIEW_MODE_DESCRIPTION = "Show calendar"
        const val DEFAULT_TODAY_DESCRIPTION = "Go to today"
        const val KOREAN_CARD_TEXT = "오늘 1500 / 2000ml"
        const val KOREAN_ACHIEVEMENT_RATE = "75%"
        const val FIRST_TITLE = "Alpha"
        const val SECOND_TITLE = "Bravo"
        const val KOREAN_FIRST_TITLE = "가"
        const val KOREAN_SECOND_TITLE = "나"
        const val KOREAN_THIRD_TITLE = "다"
        const val TODAY_RECORD_AMOUNT = 1500L
        const val OTHER_RECORD_AMOUNT = 700L
        const val TIMEOUT_MILLIS = 5_000L
        const val AFTER_UNDO_SNACKBAR_DISMISS_MILLIS = 11_000L
    }
}
