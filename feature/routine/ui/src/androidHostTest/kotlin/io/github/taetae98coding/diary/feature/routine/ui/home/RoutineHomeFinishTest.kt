package io.github.taetae98coding.diary.feature.routine.ui.home

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isDialog
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToIndex
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeLeft
import androidx.compose.ui.test.swipeRight
import io.github.taetae98coding.diary.core.model.routine.CalendarRoutineRecord
import io.github.taetae98coding.diary.core.model.routine.RoutineSummary
import io.github.taetae98coding.diary.core.model.routine.RoutineViewScope
import io.github.taetae98coding.diary.core.testing.tag.tag
import io.github.taetae98coding.diary.feature.routine.ui.home.calendar.ROUTINE_HOME_CALENDAR_RECORD_TEST_TAG
import io.github.taetae98coding.diary.feature.routine.ui.resetAndroidUiDispatcher
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next
import kotlinx.datetime.LocalDate
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

// 완료와 그 실행 취소는 제품 RoutineHomeScreen과 RoutineHomeViewModel로 실행하고, 저장은 UseCase mock이 흉내 낸다.
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "ko-w411dp-h891dp")
class RoutineHomeFinishTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    private val environment = RoutineHomeQuickRecordTestEnvironment()

    @Before
    fun setUp() {
        stopKoin()
        resetAndroidUiDispatcher()
        setRoutineHomeQuickRecordToday(TODAY)
    }

    @After
    fun tearDown() {
        releaseRoutineHomeQuickRecordToday()
    }

    @Test
    fun `TC-d243e034-7dbe-4b36-9f81-bfc258a14528 완료된 루틴은 목록에 보이지 않는다`() {
        setRoutineHome(countSummary(title = GA_TITLE), countSummary(title = NA_TITLE, isFinished = true))

        composeRule.onNode(routineHomeQuickRecordCard(GA_TITLE)).assertIsDisplayed()
        composeRule.onNode(routineHomeQuickRecordCard(NA_TITLE)).assertDoesNotExist()
        composeRule.onNodeWithText(FINISHED_CARD_TEXT).assertDoesNotExist()
    }

    @Test
    fun `TC-7256771b-8298-416e-a3a1-851c8dcdbaf2 상세에서 루틴을 완료한 뒤 돌아오면 그 루틴이 목록에서 사라져 있다`() {
        val na = countSummary(title = NA_TITLE)
        setRoutineHome(countSummary(title = GA_TITLE), na)
        composeRule.onNode(routineHomeQuickRecordCard(NA_TITLE)).performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithText(ROUTINE_HOME_QUICK_RECORD_DETAIL_CONTENT).assertExists()

        composeRule.runOnIdle { environment.setFinished(id = na.routine.id, isFinished = true) }
        back()

        composeRule.waitUntilRoutineHomeQuickRecordNode(routineHomeQuickRecordCard(GA_TITLE))
        composeRule.onNode(routineHomeQuickRecordCard(NA_TITLE)).assertDoesNotExist()
    }

    @Test
    fun `TC-f805951d-adfc-45d4-b7be-f38f1b1ba7b4 기록 안내 뒤에 완료하면 완료 안내만 남고 실행 취소는 완료를 되돌린다`() {
        val routineB = countSummary(title = ROUTINE_B_TITLE)
        setRoutineHome(countSummary(title = ROUTINE_A_TITLE), routineB)
        composeRule.clickRoutineHomeQuickRecord(ROUTINE_A_TITLE)
        composeRule.waitUntilRoutineHomeQuickRecordNode(hasText(ROUTINE_A_RECORDED_TEXT))

        swipeFinish(ROUTINE_B_TITLE)

        composeRule.waitUntilRoutineHomeQuickRecordNode(hasText(FINISHED_MESSAGE))
        composeRule.onNodeWithText(ROUTINE_A_RECORDED_TEXT).assertDoesNotExist()
        composeRule.routineHomeQuickRecordSnackbarCount() shouldBe 1

        undo()

        composeRule.waitUntilRoutineHomeQuickRecordNode(routineHomeQuickRecordCard(ROUTINE_B_TITLE))
        composeRule.onNode(routineHomeQuickRecordCard(ROUTINE_A_TITLE, "오늘 1회")).assertExists()
        environment.restartRoutineIdList shouldBe listOf(routineB.routine.id)
        environment.undoRecordIdList.shouldBeEmpty()
        environment.savedRecordList.single().isDeleted shouldBe false
    }

    @Test
    fun `TC-f805951d-adfc-45d4-b7be-f38f1b1ba7b4 완료 안내 뒤에 기록하면 기록 안내만 남고 실행 취소는 기록을 되돌린다`() {
        setRoutineHome(countSummary(title = ROUTINE_A_TITLE), countSummary(title = ROUTINE_B_TITLE))
        swipeFinish(ROUTINE_B_TITLE)
        composeRule.waitUntilRoutineHomeQuickRecordNode(hasText(FINISHED_MESSAGE))

        composeRule.clickRoutineHomeQuickRecord(ROUTINE_A_TITLE)

        composeRule.waitUntilRoutineHomeQuickRecordNode(hasText(ROUTINE_A_RECORDED_TEXT))
        composeRule.onNodeWithText(FINISHED_MESSAGE).assertDoesNotExist()
        composeRule.routineHomeQuickRecordSnackbarCount() shouldBe 1

        undo()

        composeRule.waitUntilRoutineHomeQuickRecordNode(routineHomeQuickRecordCard(ROUTINE_A_TITLE, "오늘 0회"))
        environment.savedRecordList.single().isDeleted shouldBe true
        environment.restartRoutineIdList.shouldBeEmpty()
        composeRule.onNode(routineHomeQuickRecordCard(ROUTINE_B_TITLE)).assertDoesNotExist()
    }

    @Test
    fun `TC-65be3fea-f2e8-4688-ba43-2630bd90d09b 완료 안내 뒤에 삭제하면 삭제 안내만 남고 실행 취소는 삭제를 되돌린다`() {
        val routineB = countSummary(title = ROUTINE_B_TITLE)
        setRoutineHome(countSummary(title = ROUTINE_A_TITLE), routineB, countSummary(title = ROUTINE_C_TITLE))
        swipeFinish(ROUTINE_A_TITLE)
        composeRule.waitUntilRoutineHomeQuickRecordNode(hasText(FINISHED_MESSAGE))

        swipeDelete(ROUTINE_B_TITLE)

        composeRule.waitUntilRoutineHomeQuickRecordNode(hasText(ROUTINE_HOME_QUICK_RECORD_DELETED_MESSAGE))
        composeRule.onNodeWithText(FINISHED_MESSAGE).assertDoesNotExist()
        composeRule.routineHomeQuickRecordSnackbarCount() shouldBe 1

        undo()

        composeRule.waitUntilRoutineHomeQuickRecordNode(routineHomeQuickRecordCard(ROUTINE_B_TITLE))
        environment.restoreRoutineIdList shouldBe listOf(routineB.routine.id)
        environment.restartRoutineIdList.shouldBeEmpty()
        composeRule.onNode(routineHomeQuickRecordCard(ROUTINE_A_TITLE)).assertDoesNotExist()
    }

    @Test
    fun `TC-65be3fea-f2e8-4688-ba43-2630bd90d09b 삭제 안내 뒤에 완료하면 완료 안내만 남고 실행 취소는 완료를 되돌린다`() {
        val routineB = countSummary(title = ROUTINE_B_TITLE)
        setRoutineHome(countSummary(title = ROUTINE_A_TITLE), routineB, countSummary(title = ROUTINE_C_TITLE))
        swipeDelete(ROUTINE_A_TITLE)
        composeRule.waitUntilRoutineHomeQuickRecordNode(hasText(ROUTINE_HOME_QUICK_RECORD_DELETED_MESSAGE))

        swipeFinish(ROUTINE_B_TITLE)

        composeRule.waitUntilRoutineHomeQuickRecordNode(hasText(FINISHED_MESSAGE))
        composeRule.onNodeWithText(ROUTINE_HOME_QUICK_RECORD_DELETED_MESSAGE).assertDoesNotExist()
        composeRule.routineHomeQuickRecordSnackbarCount() shouldBe 1

        undo()

        composeRule.waitUntilRoutineHomeQuickRecordNode(routineHomeQuickRecordCard(ROUTINE_B_TITLE))
        environment.restartRoutineIdList shouldBe listOf(routineB.routine.id)
        environment.restoreRoutineIdList.shouldBeEmpty()
        composeRule.onNode(routineHomeQuickRecordCard(ROUTINE_A_TITLE)).assertDoesNotExist()
    }

    @Test
    fun `TC-60d1124f-8d20-41bc-9120-69a57fe14e96 완료 안내가 보이는 동안 달력 보기에 다녀오면 안내가 닫히고 되돌릴 수 없다`() {
        showFinishNotice()

        switchToCalendar()
        switchToList()

        assertFinishNoticeClosed()
    }

    @Test
    fun `TC-60d1124f-8d20-41bc-9120-69a57fe14e96 완료 안내가 보이는 동안 루틴 추가에 다녀오면 안내가 닫히고 되돌릴 수 없다`() {
        showFinishNotice()

        composeRule.onNodeWithContentDescription(ADD_BUTTON_DESCRIPTION).performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithText(ROUTINE_HOME_QUICK_RECORD_ADD_CONTENT).assertExists()
        back()

        assertFinishNoticeClosed()
    }

    @Test
    fun `TC-60d1124f-8d20-41bc-9120-69a57fe14e96 완료 안내가 보이는 동안 완료된 루틴 확인에 다녀오면 안내가 닫히고 되돌릴 수 없다`() {
        showFinishNotice()

        openFinishedList()
        back()

        assertFinishNoticeClosed()
    }

    @Test
    fun `TC-ca89891d-cfcf-4daa-a2b7-eb6ef0dc6491 실행 취소를 선택하지 않으면 완료 안내가 잠시 뒤 사라지고 루틴은 완료된 채로 남는다`() {
        showFinishNotice()

        composeRule.mainClock.advanceTimeBy(ROUTINE_HOME_QUICK_RECORD_AFTER_SNACKBAR_DISMISS_MILLIS)
        composeRule.waitForIdle()

        composeRule.onNodeWithText(FINISHED_MESSAGE).assertDoesNotExist()
        composeRule.onNodeWithText(ROUTINE_HOME_QUICK_RECORD_UNDO_ACTION).assertDoesNotExist()
        environment.restartRoutineIdList.shouldBeEmpty()
        composeRule.onNode(routineHomeQuickRecordCard(ROUTINE_A_TITLE)).assertDoesNotExist()
    }

    @Test
    fun `TC-84e33b0c-c3b3-43d7-ad10-583cdd282703 완료된 루틴은 목록 보기에 보이지 않아도 그 기록은 달력 보기에 보인다`() {
        setExerciseHome(isFinished = true)
        composeRule.onNode(routineHomeQuickRecordCard(EXERCISE_TITLE)).assertDoesNotExist()

        switchToCalendar()

        composeRule.onAllNodes(calendarRecord(EXERCISE_JULY_10_DESCRIPTION)).fetchSemanticsNodes().isNotEmpty() shouldBe true
        composeRule.onAllNodes(hasText(EXERCISE_JULY_10_TEXT)).fetchSemanticsNodes().isNotEmpty() shouldBe true
    }

    @Test
    fun `TC-7d1c72c5-ae3e-4c67-8b72-b70fdd92826e 완료된 루틴의 기록을 선택해도 그 루틴의 RoutineDetail 화면으로 이동한다`() {
        val exercise = setExerciseHome(isFinished = true)
        switchToCalendar()

        composeRule.onAllNodes(calendarRecord(EXERCISE_JULY_10_DESCRIPTION))[0].performClick()
        composeRule.waitForIdle()

        environment.detailNavigationList shouldBe listOf(exercise.routine.id)
    }

    @Test
    fun `TC-4e8b3034-f1d6-45ab-a64f-258e23e8c08e 달력에서 연 상세에서 루틴을 완료해도 그 루틴의 기록은 달력에 그대로 보인다`() {
        assertCalendarRecordKeptAfterDetailChange(isFinished = false)
    }

    @Test
    fun `TC-4e8b3034-f1d6-45ab-a64f-258e23e8c08e 달력에서 연 상세에서 루틴을 다시 시작해도 그 루틴의 기록은 달력에 그대로 보인다`() {
        assertCalendarRecordKeptAfterDetailChange(isFinished = true)
    }

    @Test
    fun `TC-823aa860-a19b-416c-9039-c84d2982a7fd 완료된 루틴만 있으면 빈 상태 안내를 표시한다`() {
        environment.setSummaryList(listOf(countSummary(title = GA_TITLE, isFinished = true)))
        composeRule.setContent { environment.Content() }

        composeRule.waitUntilRoutineHomeQuickRecordNode(hasText(EMPTY_TITLE))
        composeRule.onNodeWithText(EMPTY_DESCRIPTION).assertExists()
        composeRule.onNode(routineHomeQuickRecordCard(GA_TITLE)).assertDoesNotExist()
    }

    @Test
    fun `TC-791aa349-e7d4-4bdd-9866-a6c9726738ee 마지막 루틴을 완료하면 빈 상태가 되고 실행 취소로 돌아오면 빈 상태가 사라진다`() {
        setRoutineHome(countSummary(title = ROUTINE_A_TITLE))

        swipeFinish(ROUTINE_A_TITLE)
        composeRule.waitUntilRoutineHomeQuickRecordNode(hasText(EMPTY_TITLE))

        undo()

        composeRule.waitUntilRoutineHomeQuickRecordNode(routineHomeQuickRecordCard(ROUTINE_A_TITLE))
        composeRule.waitUntilRoutineHomeQuickRecordNodeGone(hasText(EMPTY_TITLE))
    }

    @Test
    fun `TC-28fe97c1-6c3d-456e-ac1e-f9da6393b47c 완료된 루틴 확인을 선택하면 RoutineFinishedList 화면으로 이동한다`() {
        setRoutineHome(countSummary(title = ROUTINE_A_TITLE))

        openFinishedList()

        environment.finishedListNavigationCount shouldBe 1
    }

    @Test
    fun `TC-9c992a40-79e0-4309-8bcd-521b46cfa07c 완료된 루틴이 하나도 없어도 완료된 루틴 확인을 제공한다`() {
        setRoutineHome(countSummary(title = ROUTINE_A_TITLE))

        assertFinishedListProvided()
    }

    @Test
    fun `TC-9c992a40-79e0-4309-8bcd-521b46cfa07c 빈 상태 안내가 표시되어 있어도 완료된 루틴 확인을 제공한다`() {
        composeRule.setContent { environment.Content() }
        composeRule.waitUntilRoutineHomeQuickRecordNode(hasText(EMPTY_TITLE))

        assertFinishedListProvided()
    }

    @Test
    fun `TC-9c992a40-79e0-4309-8bcd-521b46cfa07c 태그 필터로 좁힌 루틴이 표시되어 있어도 완료된 루틴 확인을 제공한다`() {
        environment.filterTagList = listOf(Arb.tag().next())
        setRoutineHome(countSummary(title = ROUTINE_A_TITLE))
        composeRule.onNode(appliedFilterButton()).assertExists()

        assertFinishedListProvided()
    }

    @Test
    fun `TC-9c992a40-79e0-4309-8bcd-521b46cfa07c 태그 필터를 적용한 빈 상태 안내가 표시되어 있어도 완료된 루틴 확인을 제공한다`() {
        environment.filterTagList = listOf(Arb.tag().next())
        composeRule.setContent { environment.Content() }
        composeRule.waitUntilRoutineHomeQuickRecordNode(hasText(FILTERED_EMPTY_TITLE))

        assertFinishedListProvided()
    }

    @Test
    fun `TC-9c992a40-79e0-4309-8bcd-521b46cfa07c 오늘 할 루틴이 없는 빈 상태 안내가 표시되어 있어도 완료된 루틴 확인을 제공한다`() {
        // 완료되지 않은 루틴은 있지만 오늘 할 루틴 조회가 돌려주는 루틴이 없다.
        environment.setPageTitleList(viewScope = RoutineViewScope.TODAY, today = TODAY, titleList = emptyList())
        environment.setSummaryList(listOf(routineHomeQuickRecordSummary(title = ROUTINE_A_TITLE, startDate = NOT_STARTED_START_DATE, unit = "회")))
        composeRule.setContent { environment.Content() }
        composeRule.waitUntilRoutineHomeQuickRecordNode(hasText(TODAY_EMPTY_TITLE))

        assertFinishedListProvided()
    }

    @Test
    fun `TC-9c992a40-79e0-4309-8bcd-521b46cfa07c 보기 범위를 모든 루틴으로 바꿔 두어도 완료된 루틴 확인을 제공한다`() {
        environment.viewScope.value = RoutineViewScope.ALL
        setRoutineHome(routineHomeQuickRecordSummary(title = ROUTINE_A_TITLE, startDate = NOT_STARTED_START_DATE, unit = "회"))

        assertFinishedListProvided()
    }

    @Test
    fun `TC-00a4ba83-2169-44a1-b395-0cb762ad9e4e 완료된 루틴 확인에서 돌아오면 목록 보기와 필터 선택, 보던 목록 위치가 그대로다`() {
        val summaryList = List(ROUTINE_COUNT) { index -> countSummary(title = "$SCROLL_TITLE_PREFIX${index.toString().padStart(length = 2, padChar = '0')}") }
        val firstTitle =
            summaryList
                .first()
                .routine.detail.title
        val scrolledTitle = summaryList[SCROLLED_INDEX].routine.detail.title
        environment.filterTagList = listOf(Arb.tag().next())
        environment.setSummaryList(summaryList)
        composeRule.setContent { environment.Content() }
        composeRule.waitUntilRoutineHomeQuickRecordNode(routineHomeQuickRecordCard(firstTitle))
        composeRule.filterCaseRefreshableList().performScrollToIndex(SCROLLED_INDEX)
        composeRule.onNode(routineHomeQuickRecordCard(scrolledTitle)).assertIsDisplayed()

        openFinishedList()
        back()

        composeRule.waitUntilRoutineHomeQuickRecordNode(routineHomeQuickRecordCard(scrolledTitle))
        composeRule.onNode(routineHomeQuickRecordCard(scrolledTitle)).assertIsDisplayed()
        composeRule.onNode(routineHomeQuickRecordCard(firstTitle)).assertDoesNotExist()
        composeRule.onNodeWithContentDescription(ROUTINE_HOME_QUICK_RECORD_CALENDAR_VIEW_DESCRIPTION).assertExists()
        composeRule.onNode(appliedFilterButton()).assertExists()
    }

    @Test
    fun `TC-b7326827-053e-4ad2-a65f-187e91aae352 달력 보기에서는 완료된 루틴 확인을 제공하지 않는다`() {
        setRoutineHome(countSummary(title = ROUTINE_A_TITLE))
        environment.addSummary(countSummary(title = ROUTINE_B_TITLE, isFinished = true))
        composeRule.onNodeWithText(FINISHED_LIST_LABEL).assertExists()

        switchToCalendar()

        composeRule.onNodeWithText(FINISHED_LIST_LABEL).assertDoesNotExist()
    }

    @Test
    fun `TC-08080f94-9198-4ced-a92b-e291989f0576 진행 중인 루틴 카드를 완료 방향으로 밀면 완료되고 안내와 실행 취소가 표시된다`() {
        assertSwipeFinished(countSummary(title = ROUTINE_A_TITLE))
    }

    @Test
    fun `TC-08080f94-9198-4ced-a92b-e291989f0576 시작 전인 루틴 카드를 완료 방향으로 밀면 완료되고 안내와 실행 취소가 표시된다`() {
        environment.viewScope.value = RoutineViewScope.ALL
        assertSwipeFinished(routineHomeQuickRecordSummary(title = ROUTINE_A_TITLE, startDate = NOT_STARTED_START_DATE, unit = "회"))
    }

    @Test
    fun `TC-1730d138-f1e8-4c16-a918-3d633792065e 완료를 실행 취소하면 목록 정렬에 맞는 자리에 완료 전의 카드 문구로 다시 나타난다`() {
        val na =
            routineHomeQuickRecordSummary(
                title = NA_TITLE,
                startDate = START_DATE,
                unit = "ml",
                goalAmount = 2_000,
                amountPerRecord = 250,
                cycleAmount = 1_500L,
            )
        val titleList = listOf(GA_TITLE, NA_TITLE, DA_TITLE)
        setRoutineHome(countSummary(title = GA_TITLE), na, countSummary(title = DA_TITLE))
        swipeFinish(NA_TITLE)
        composeRule.waitUntilRoutineHomeQuickRecordNode(hasText(FINISHED_MESSAGE))
        composeRule.onNode(routineHomeQuickRecordCard(NA_TITLE)).assertDoesNotExist()

        undo()

        composeRule.waitUntilRoutineHomeQuickRecordNode(routineHomeQuickRecordCard(NA_TITLE, "오늘 1500 / 2000ml", "75%"))
        environment.restartRoutineIdList shouldBe listOf(na.routine.id)
        composeRule.filterCaseCardTitleList(titleList) shouldBe titleList
    }

    @Test
    fun `TC-543783a6-fd15-4c26-8217-4c3e95ef9027 루틴을 완료하고 실행 취소하지 않아도 그 루틴의 기록은 달력에 그대로 보인다`() {
        setExerciseHome(isFinished = false)

        swipeFinish(EXERCISE_TITLE)
        composeRule.waitUntilRoutineHomeQuickRecordNode(hasText(FINISHED_MESSAGE))
        switchToCalendar()

        assertExerciseRecordsOnCalendar()
    }

    @Test
    fun `TC-543783a6-fd15-4c26-8217-4c3e95ef9027 루틴을 완료하고 실행 취소해도 그 루틴의 기록은 달력에 그대로 보인다`() {
        setExerciseHome(isFinished = false)

        swipeFinish(EXERCISE_TITLE)
        composeRule.waitUntilRoutineHomeQuickRecordNode(hasText(FINISHED_MESSAGE))
        undo()
        composeRule.waitUntilRoutineHomeQuickRecordNode(routineHomeQuickRecordCard(EXERCISE_TITLE))
        switchToCalendar()

        assertExerciseRecordsOnCalendar()
    }

    @Test
    fun `TC-be59232a-b39c-44d3-8d35-29bf42965bf7 완료를 저장하지 못하면 루틴이 목록에 남고 안내가 보이지 않으며 다시 완료할 수 있다`() {
        val routine = countSummary(title = ROUTINE_A_TITLE)
        environment.isFinishSaveSuccess = false
        setRoutineHome(routine)

        swipeFinish(ROUTINE_A_TITLE)

        environment.finishRoutineIdList shouldBe listOf(routine.routine.id)
        composeRule.onNodeWithText(FINISHED_MESSAGE).assertDoesNotExist()
        composeRule.onNodeWithText(ROUTINE_HOME_QUICK_RECORD_UNDO_ACTION).assertDoesNotExist()
        composeRule.onNode(routineHomeQuickRecordCard(ROUTINE_A_TITLE)).assertIsDisplayed()

        environment.isFinishSaveSuccess = true
        composeRule.mainClock.advanceTimeBy(SWIPE_RESET_MILLIS)
        composeRule.waitForIdle()
        swipeFinish(ROUTINE_A_TITLE)

        composeRule.waitUntilRoutineHomeQuickRecordNode(hasText(FINISHED_MESSAGE))
        composeRule.onNodeWithText(ROUTINE_HOME_QUICK_RECORD_UNDO_ACTION).assertExists()
        composeRule.onNode(routineHomeQuickRecordCard(ROUTINE_A_TITLE)).assertDoesNotExist()
    }

    @Test
    fun `TC-721521d1-4be6-4f41-ba70-c50ab99415f2 달력 보기에서는 루틴을 완료하는 동작을 제공하지 않는다`() {
        setExerciseHome(isFinished = false)
        switchToCalendar()

        composeRule.onAllNodes(calendarRecord(EXERCISE_JULY_10_DESCRIPTION))[0].performTouchInput { swipeRight() }
        composeRule.waitForIdle()

        environment.finishRoutineIdList.shouldBeEmpty()
        composeRule.onNodeWithText(FINISHED_MESSAGE).assertDoesNotExist()
        composeRule.onNodeWithText(ROUTINE_HOME_QUICK_RECORD_UNDO_ACTION).assertDoesNotExist()
    }

    @Test
    fun `TC-671812a3-cadc-4364-a28a-ee5458ab2643 완료의 실행 취소를 저장하지 못하면 루틴이 완료된 채로 남고 안내하지 않는다`() {
        val routine = countSummary(title = ROUTINE_A_TITLE)
        setRoutineHome(routine, countSummary(title = ROUTINE_B_TITLE))
        swipeFinish(ROUTINE_A_TITLE)
        composeRule.waitUntilRoutineHomeQuickRecordNode(hasText(FINISHED_MESSAGE))
        environment.isRestartSaveSuccess = false

        undo()

        environment.restartRoutineIdList shouldBe listOf(routine.routine.id)
        composeRule.onNode(routineHomeQuickRecordCard(ROUTINE_A_TITLE)).assertDoesNotExist()
        composeRule.onNode(routineHomeQuickRecordCard(ROUTINE_B_TITLE)).assertIsDisplayed()
        composeRule.routineHomeQuickRecordSnackbarCount() shouldBe 0
    }

    private fun assertSwipeFinished(summary: RoutineSummary) {
        setRoutineHome(summary, countSummary(title = ROUTINE_B_TITLE))

        swipeFinish(summary.routine.detail.title)

        composeRule.waitUntilRoutineHomeQuickRecordNode(hasText(FINISHED_MESSAGE))
        environment.finishRoutineIdList shouldBe listOf(summary.routine.id)
        composeRule.onNode(isDialog()).assertDoesNotExist()
        composeRule.onNode(routineHomeQuickRecordCard(summary.routine.detail.title)).assertDoesNotExist()
        composeRule.onNodeWithText(ROUTINE_HOME_QUICK_RECORD_UNDO_ACTION).assertExists()
    }

    private fun assertCalendarRecordKeptAfterDetailChange(isFinished: Boolean) {
        val exercise = setExerciseHome(isFinished = isFinished)
        switchToCalendar()
        composeRule.onAllNodes(calendarRecord(EXERCISE_JULY_10_DESCRIPTION))[0].performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithText(ROUTINE_HOME_QUICK_RECORD_DETAIL_CONTENT).assertExists()

        composeRule.runOnIdle { environment.setFinished(id = exercise.routine.id, isFinished = !isFinished) }
        back()

        composeRule.waitUntilRoutineHomeQuickRecordNode(hasContentDescription(ROUTINE_HOME_QUICK_RECORD_LIST_VIEW_DESCRIPTION))
        composeRule.onAllNodes(calendarRecord(EXERCISE_JULY_10_DESCRIPTION)).fetchSemanticsNodes().isNotEmpty() shouldBe true
        composeRule.onAllNodes(hasText(EXERCISE_JULY_10_TEXT)).fetchSemanticsNodes().isNotEmpty() shouldBe true
    }

    private fun assertFinishedListProvided() {
        openFinishedList()

        environment.finishedListNavigationCount shouldBe 1
    }

    private fun assertExerciseRecordsOnCalendar() {
        listOf(EXERCISE_JULY_10_DESCRIPTION, EXERCISE_JULY_15_DESCRIPTION).forEach { description ->
            composeRule.onAllNodes(calendarRecord(description)).fetchSemanticsNodes().isNotEmpty() shouldBe true
        }
    }

    private fun showFinishNotice() {
        setRoutineHome(countSummary(title = ROUTINE_A_TITLE), countSummary(title = ROUTINE_B_TITLE))
        swipeFinish(ROUTINE_A_TITLE)
        composeRule.waitUntilRoutineHomeQuickRecordNode(hasText(FINISHED_MESSAGE))
        composeRule.onNodeWithText(ROUTINE_HOME_QUICK_RECORD_UNDO_ACTION).assertExists()
    }

    private fun assertFinishNoticeClosed() {
        composeRule.waitUntilRoutineHomeQuickRecordNode(routineHomeQuickRecordCard(ROUTINE_B_TITLE))
        composeRule.onNodeWithText(FINISHED_MESSAGE).assertDoesNotExist()
        composeRule.onNodeWithText(ROUTINE_HOME_QUICK_RECORD_UNDO_ACTION).assertDoesNotExist()
        composeRule.mainClock.advanceTimeBy(ROUTINE_HOME_QUICK_RECORD_AFTER_SNACKBAR_DISMISS_MILLIS)
        composeRule.waitForIdle()
        environment.restartRoutineIdList.shouldBeEmpty()
        composeRule.onNode(routineHomeQuickRecordCard(ROUTINE_A_TITLE)).assertDoesNotExist()
    }

    private fun openFinishedList() {
        composeRule.onNodeWithText(FINISHED_LIST_LABEL).performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithText(ROUTINE_HOME_QUICK_RECORD_FINISHED_LIST_CONTENT).assertExists()
    }

    private fun back() {
        composeRule.runOnIdle { composeRule.activity.onBackPressedDispatcher.onBackPressed() }
        composeRule.waitForIdle()
    }

    private fun undo() {
        composeRule.onNodeWithText(ROUTINE_HOME_QUICK_RECORD_UNDO_ACTION).performClick()
        composeRule.waitForIdle()
    }

    private fun swipeFinish(title: String) {
        composeRule.onNode(routineHomeQuickRecordCard(title)).performTouchInput { swipeRight() }
        composeRule.waitForIdle()
    }

    private fun swipeDelete(title: String) {
        composeRule.onNode(routineHomeQuickRecordCard(title)).performTouchInput { swipeLeft() }
        composeRule.waitForIdle()
    }

    private fun switchToCalendar() {
        composeRule.onNodeWithContentDescription(ROUTINE_HOME_QUICK_RECORD_CALENDAR_VIEW_DESCRIPTION).performClick()
        composeRule.waitForIdle()
    }

    private fun switchToList() {
        composeRule.onNodeWithContentDescription(ROUTINE_HOME_QUICK_RECORD_LIST_VIEW_DESCRIPTION).performClick()
        composeRule.waitForIdle()
    }

    private fun setRoutineHome(vararg summary: RoutineSummary) {
        environment.setSummaryList(summary.toList())
        composeRule.setContent { environment.Content() }
        composeRule.waitForIdle()
        summary
            .filterNot { value -> value.routine.isFinished }
            .forEach { value -> composeRule.waitUntilRoutineHomeQuickRecordNode(routineHomeQuickRecordCard(value.routine.detail.title)) }
    }

    // 단위가 회인 루틴 `운동`에 7월 10일과 오늘의 기록이 양 1씩 있다. 목록이 불러와졌는지는 함께 둔 다른 루틴으로 확인한다.
    private fun setExerciseHome(isFinished: Boolean): RoutineSummary {
        val exercise = countSummary(title = EXERCISE_TITLE, isFinished = isFinished)

        environment.setCalendarRecordList(
            listOf(JULY_10, TODAY).map { date ->
                CalendarRoutineRecord(
                    routineId = exercise.routine.id,
                    title = EXERCISE_TITLE,
                    color = exercise.routine.detail.color,
                    date = date,
                    amount = if (date == JULY_10) 1L else 2L,
                    unit = "회",
                )
            },
        )
        setRoutineHome(exercise, countSummary(title = ROUTINE_B_TITLE))

        return exercise
    }

    private fun countSummary(
        title: String,
        isFinished: Boolean = false,
    ): RoutineSummary = routineHomeQuickRecordSummary(title = title, startDate = START_DATE, unit = "회", amountPerRecord = 1, isFinished = isFinished)

    private fun calendarRecord(description: String) = hasTestTag(ROUTINE_HOME_CALENDAR_RECORD_TEST_TAG) and hasContentDescription(description)

    private fun appliedFilterButton() = hasContentDescription(FILTER_DESCRIPTION) and hasFilterCaseStateDescription(FILTER_APPLIED_STATE_DESCRIPTION)

    private companion object {
        val TODAY: LocalDate = LocalDate(year = 2026, month = 7, day = 15)
        val JULY_10: LocalDate = LocalDate(year = 2026, month = 7, day = 10)
        val START_DATE: LocalDate = LocalDate(year = 2026, month = 7, day = 1)
        val NOT_STARTED_START_DATE: LocalDate = LocalDate(year = 2026, month = 7, day = 20)
        const val GA_TITLE = "가"
        const val NA_TITLE = "나"
        const val DA_TITLE = "다"
        const val ROUTINE_A_TITLE = "루틴 A"
        const val ROUTINE_A_RECORDED_TEXT = "$ROUTINE_A_TITLE 1회 기록"
        const val ROUTINE_B_TITLE = "루틴 B"
        const val ROUTINE_C_TITLE = "루틴 C"
        const val EXERCISE_TITLE = "운동"
        const val EXERCISE_JULY_10_TEXT = "운동 1회"
        const val EXERCISE_JULY_10_DESCRIPTION = "운동 1회, 루틴"
        const val EXERCISE_JULY_15_DESCRIPTION = "운동 2회, 루틴"
        const val FINISHED_CARD_TEXT = "완료"
        const val FINISHED_MESSAGE = "루틴이 완료되었습니다."
        const val FINISHED_LIST_LABEL = "완료된 루틴"
        const val ADD_BUTTON_DESCRIPTION = "루틴 추가"
        const val EMPTY_TITLE = "아직 루틴이 없습니다"
        const val TODAY_EMPTY_TITLE = "오늘 할 루틴이 없습니다"
        const val EMPTY_DESCRIPTION = "추가 버튼으로 새 루틴을 만들 수 있습니다"
        const val FILTERED_EMPTY_TITLE = "조건에 맞는 루틴이 없습니다"
        const val FILTER_DESCRIPTION = "필터"
        const val FILTER_APPLIED_STATE_DESCRIPTION = "태그 필터 적용됨"
        const val SCROLL_TITLE_PREFIX = "스크롤 루틴"
        const val ROUTINE_COUNT = 40
        const val SCROLLED_INDEX = 30
        const val SWIPE_RESET_MILLIS = 2_000L
    }
}
