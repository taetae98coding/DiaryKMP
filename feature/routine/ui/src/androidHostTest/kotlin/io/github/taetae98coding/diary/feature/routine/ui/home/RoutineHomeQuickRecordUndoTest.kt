package io.github.taetae98coding.diary.feature.routine.ui.home

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextReplacement
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeLeft
import androidx.lifecycle.Lifecycle
import io.github.taetae98coding.diary.core.model.routine.RoutineSummary
import io.github.taetae98coding.diary.feature.routine.ui.resetAndroidUiDispatcher
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.shouldBe
import kotlinx.datetime.LocalDate
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

// 기록 안내와 실행 취소는 제품 RoutineHomeScreen과 RoutineHomeViewModel로 실행하고, 기록의 저장과 삭제는 UseCase mock이 흉내 낸다.
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "ko-w411dp-h891dp")
class RoutineHomeQuickRecordUndoTest {
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
    fun `TC-0011e20c-a37d-44bf-8c8e-fdb644709145 빠른 기록을 실행 취소하면 기록이 빠져 카드 문구가 기록하기 전으로 돌아간다`() {
        setRoutineHome(waterSummary(cycleAmount = 1_500L))
        composeRule.clickRoutineHomeQuickRecord(WATER_TITLE)
        composeRule.waitUntilRoutineHomeQuickRecordNode(routineHomeQuickRecordCard(WATER_TITLE, TODAY_1750_OF_2000_ML_TEXT, "87%"))

        undo()

        assertUndone()
    }

    @Test
    fun `TC-0011e20c-a37d-44bf-8c8e-fdb644709145 양을 입력한 기록을 실행 취소하면 기록이 빠져 카드 문구가 기록하기 전으로 돌아간다`() {
        setRoutineHome(waterSummary(cycleAmount = 1_500L))
        composeRule.longPressRoutineHomeQuickRecord(WATER_TITLE)
        composeRule.onNode(routineAmountDialogField()).performTextReplacement("300")
        composeRule.onNode(routineAmountDialogButton(ROUTINE_AMOUNT_DIALOG_CONFIRM)).performClick()
        composeRule.waitForIdle()
        composeRule.waitUntilRoutineHomeQuickRecordNode(routineHomeQuickRecordCard(WATER_TITLE, "오늘 1800 / 2000ml", "90%"))

        undo()

        assertUndone()
    }

    @Test
    fun `TC-6b1c3e04-03dc-45e3-9fba-826c942f3006 실행 취소는 마지막으로 넣은 기록 하나만 되돌린다`() {
        setRoutineHome(waterSummary(cycleAmount = 0L))
        composeRule.clickRoutineHomeQuickRecord(WATER_TITLE)
        composeRule.waitUntilRoutineHomeQuickRecordNode(routineHomeQuickRecordCard(WATER_TITLE, "오늘 250 / 2000ml"))
        composeRule.clickRoutineHomeQuickRecord(WATER_TITLE)
        composeRule.waitUntilRoutineHomeQuickRecordNode(routineHomeQuickRecordCard(WATER_TITLE, "오늘 500 / 2000ml"))

        undo()

        composeRule.waitUntilRoutineHomeQuickRecordNode(routineHomeQuickRecordCard(WATER_TITLE, "오늘 250 / 2000ml"))
        environment.savedRecordList.map { record -> record.isDeleted } shouldBe listOf(false, true)
        composeRule.onNodeWithText(ROUTINE_HOME_QUICK_RECORD_UNDO_ACTION).assertDoesNotExist()
        composeRule.routineHomeQuickRecordSnackbarCount() shouldBe 0
    }

    @Test
    fun `TC-1a471c6c-66af-4298-aed4-7fd0c1bf89d0 안내가 보이는 동안 다른 루틴에 기록을 넣으면 마지막 기록의 안내만 남는다`() {
        val routineA = countSummary(title = ROUTINE_A_TITLE)
        val routineB = countSummary(title = ROUTINE_B_TITLE)
        setRoutineHome(routineA, routineB)
        composeRule.clickRoutineHomeQuickRecord(ROUTINE_A_TITLE)
        composeRule.waitUntilRoutineHomeQuickRecordNode(hasText(ROUTINE_A_RECORDED_TEXT))

        composeRule.clickRoutineHomeQuickRecord(ROUTINE_B_TITLE)
        composeRule.waitUntilRoutineHomeQuickRecordNode(hasText("$ROUTINE_B_TITLE 1회 기록"))

        composeRule.onNodeWithText(ROUTINE_A_RECORDED_TEXT).assertDoesNotExist()
        composeRule.onAllNodesWithText(ROUTINE_HOME_QUICK_RECORD_UNDO_ACTION).assertCountEquals(1)

        undo()

        composeRule.waitUntilRoutineHomeQuickRecordNode(routineHomeQuickRecordCard(ROUTINE_B_TITLE, "오늘 0회"))
        composeRule.onNode(routineHomeQuickRecordCard(ROUTINE_A_TITLE, "오늘 1회")).assertExists()
        environment.savedRecordList.single { record -> record.routineId == routineA.routine.id }.isDeleted shouldBe false
        environment.savedRecordList.single { record -> record.routineId == routineB.routine.id }.isDeleted shouldBe true
    }

    @Test
    fun `TC-3c4b593b-df0e-4d3c-996f-316ab550768c 기록 안내 뒤에 삭제하면 삭제 안내만 남고 실행 취소는 삭제를 되돌린다`() {
        val routineA = countSummary(title = ROUTINE_A_TITLE)
        val routineB = countSummary(title = ROUTINE_B_TITLE)
        setRoutineHome(routineA, routineB)
        composeRule.clickRoutineHomeQuickRecord(ROUTINE_A_TITLE)
        composeRule.waitUntilRoutineHomeQuickRecordNode(hasText(ROUTINE_A_RECORDED_TEXT))

        swipeDelete(ROUTINE_B_TITLE)

        composeRule.waitUntilRoutineHomeQuickRecordNode(hasText(ROUTINE_HOME_QUICK_RECORD_DELETED_MESSAGE))
        composeRule.onNodeWithText(ROUTINE_A_RECORDED_TEXT).assertDoesNotExist()
        composeRule.routineHomeQuickRecordSnackbarCount() shouldBe 1

        undo()

        composeRule.waitUntilRoutineHomeQuickRecordNode(routineHomeQuickRecordCard(ROUTINE_B_TITLE))
        composeRule.onNode(routineHomeQuickRecordCard(ROUTINE_A_TITLE, "오늘 1회")).assertExists()
        environment.restoreRoutineIdList shouldBe listOf(routineB.routine.id)
        environment.undoRecordIdList.shouldBeEmpty()
        environment.savedRecordList.single().isDeleted shouldBe false
    }

    @Test
    fun `TC-3c4b593b-df0e-4d3c-996f-316ab550768c 삭제 안내 뒤에 기록하면 기록 안내만 남고 실행 취소는 기록을 되돌린다`() {
        val routineA = countSummary(title = ROUTINE_A_TITLE)
        val routineB = countSummary(title = ROUTINE_B_TITLE)
        setRoutineHome(routineA, routineB)
        swipeDelete(ROUTINE_B_TITLE)
        composeRule.waitUntilRoutineHomeQuickRecordNode(hasText(ROUTINE_HOME_QUICK_RECORD_DELETED_MESSAGE))

        composeRule.clickRoutineHomeQuickRecord(ROUTINE_A_TITLE)

        composeRule.waitUntilRoutineHomeQuickRecordNode(hasText(ROUTINE_A_RECORDED_TEXT))
        composeRule.onNodeWithText(ROUTINE_HOME_QUICK_RECORD_DELETED_MESSAGE).assertDoesNotExist()
        composeRule.routineHomeQuickRecordSnackbarCount() shouldBe 1

        undo()

        composeRule.waitUntilRoutineHomeQuickRecordNode(routineHomeQuickRecordCard(ROUTINE_A_TITLE, "오늘 0회"))
        environment.savedRecordList.single().isDeleted shouldBe true
        environment.restoreRoutineIdList.shouldBeEmpty()
        composeRule.onNode(routineHomeQuickRecordCard(ROUTINE_B_TITLE)).assertDoesNotExist()
    }

    @Test
    fun `TC-e346190c-bc56-4531-8569-0dc7473f409c 실행 취소를 선택하지 않으면 기록 안내가 잠시 뒤 사라지고 기록이 남는다`() {
        setRoutineHome(waterSummary(cycleAmount = 1_500L))
        composeRule.clickRoutineHomeQuickRecord(WATER_TITLE)
        composeRule.waitUntilRoutineHomeQuickRecordNode(hasText(WATER_RECORDED_MESSAGE))

        composeRule.mainClock.advanceTimeBy(ROUTINE_HOME_QUICK_RECORD_AFTER_SNACKBAR_DISMISS_MILLIS)
        composeRule.waitForIdle()

        composeRule.onNodeWithText(WATER_RECORDED_MESSAGE).assertDoesNotExist()
        composeRule.onNodeWithText(ROUTINE_HOME_QUICK_RECORD_UNDO_ACTION).assertDoesNotExist()
        environment.savedRecordList.single().isDeleted shouldBe false
        environment.undoRecordIdList.shouldBeEmpty()
        composeRule.onNode(routineHomeQuickRecordCard(WATER_TITLE, TODAY_1750_OF_2000_ML_TEXT, "87%")).assertExists()
    }

    @Test
    fun `TC-5271eaca-f6fd-481e-8a1f-153eb67939f7 기록 안내가 보이는 동안 루틴 상세에 다녀오면 안내가 닫히고 되돌릴 수 없다`() {
        setRoutineHome(waterSummary(cycleAmount = 1_500L))
        showRecordNotice()

        composeRule.onNode(routineHomeQuickRecordCard(WATER_TITLE)).performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithText(ROUTINE_HOME_QUICK_RECORD_DETAIL_CONTENT).assertExists()
        composeRule.runOnIdle { composeRule.activity.onBackPressedDispatcher.onBackPressed() }
        composeRule.waitForIdle()

        assertRecordNoticeClosed()
    }

    @Test
    fun `TC-5271eaca-f6fd-481e-8a1f-153eb67939f7 기록 안내가 보이는 동안 화면이 재생성되면 안내가 닫히고 되돌릴 수 없다`() {
        val restorationTester = StateRestorationTester(composeRule)
        environment.setSummaryList(listOf(waterSummary(cycleAmount = 1_500L)))
        restorationTester.setContent { environment.Content() }
        composeRule.waitUntilRoutineHomeQuickRecordNode(routineHomeQuickRecordCard(WATER_TITLE))
        showRecordNotice()

        restorationTester.emulateSavedInstanceStateRestore()
        composeRule.waitForIdle()

        assertRecordNoticeClosed()
    }

    @Test
    fun `TC-5271eaca-f6fd-481e-8a1f-153eb67939f7 기록 안내가 보이는 동안 달력 보기에 다녀오면 안내가 닫히고 되돌릴 수 없다`() {
        setRoutineHome(waterSummary(cycleAmount = 1_500L))
        showRecordNotice()

        switchToCalendarAndBack()

        assertRecordNoticeClosed()
    }

    @Test
    fun `TC-5271eaca-f6fd-481e-8a1f-153eb67939f7 삭제 안내가 보이는 동안 달력 보기에 다녀오면 안내가 닫히고 되돌릴 수 없다`() {
        setRoutineHome(waterSummary(cycleAmount = 1_500L), countSummary(title = ROUTINE_A_TITLE))
        swipeDelete(ROUTINE_A_TITLE)
        composeRule.waitUntilRoutineHomeQuickRecordNode(hasText(ROUTINE_HOME_QUICK_RECORD_DELETED_MESSAGE))

        switchToCalendarAndBack()

        composeRule.waitUntilRoutineHomeQuickRecordNode(routineHomeQuickRecordCard(WATER_TITLE))
        composeRule.onNodeWithText(ROUTINE_HOME_QUICK_RECORD_DELETED_MESSAGE).assertDoesNotExist()
        composeRule.onNodeWithText(ROUTINE_HOME_QUICK_RECORD_UNDO_ACTION).assertDoesNotExist()
        composeRule.mainClock.advanceTimeBy(ROUTINE_HOME_QUICK_RECORD_AFTER_SNACKBAR_DISMISS_MILLIS)
        composeRule.waitForIdle()
        environment.restoreRoutineIdList.shouldBeEmpty()
        composeRule.onNode(routineHomeQuickRecordCard(ROUTINE_A_TITLE)).assertDoesNotExist()
    }

    @Test
    fun `TC-7cdc34cd-2e37-4352-aeba-578d58bf3010 기록 안내가 보이는 동안 백그라운드에 다녀와도 실행 취소할 수 있다`() {
        setRoutineHome(waterSummary(cycleAmount = 1_500L))
        showRecordNotice()

        composeRule.activityRule.scenario.moveToState(Lifecycle.State.CREATED)
        composeRule.waitForIdle()
        composeRule.activityRule.scenario.moveToState(Lifecycle.State.RESUMED)
        composeRule.waitForIdle()

        composeRule.onNodeWithText(WATER_RECORDED_MESSAGE).assertExists()
        composeRule.onNodeWithText(ROUTINE_HOME_QUICK_RECORD_UNDO_ACTION).assertExists()

        undo()

        assertUndone()
    }

    @Test
    fun `TC-f8b3d050-ec39-48e9-92ad-a5137d6db0b9 실행 취소를 저장하지 못하면 기록이 그대로 남고 안내하지 않는다`() {
        setRoutineHome(waterSummary(cycleAmount = 1_500L))
        showRecordNotice()
        environment.isUndoSaveSuccess = false

        undo()

        environment.undoRecordIdList.size shouldBe 1
        environment.savedRecordList.single().isDeleted shouldBe false
        composeRule.onNode(routineHomeQuickRecordCard(WATER_TITLE, TODAY_1750_OF_2000_ML_TEXT)).assertExists()
        composeRule.routineHomeQuickRecordSnackbarCount() shouldBe 0
    }

    private fun showRecordNotice() {
        composeRule.clickRoutineHomeQuickRecord(WATER_TITLE)
        composeRule.waitUntilRoutineHomeQuickRecordNode(hasText(WATER_RECORDED_MESSAGE))
        composeRule.onNodeWithText(ROUTINE_HOME_QUICK_RECORD_UNDO_ACTION).assertExists()
    }

    private fun assertRecordNoticeClosed() {
        composeRule.waitUntilRoutineHomeQuickRecordNode(routineHomeQuickRecordCard(WATER_TITLE))
        composeRule.onNodeWithText(WATER_RECORDED_MESSAGE).assertDoesNotExist()
        composeRule.onNodeWithText(ROUTINE_HOME_QUICK_RECORD_UNDO_ACTION).assertDoesNotExist()
        composeRule.mainClock.advanceTimeBy(ROUTINE_HOME_QUICK_RECORD_AFTER_SNACKBAR_DISMISS_MILLIS)
        composeRule.waitForIdle()
        environment.undoRecordIdList.shouldBeEmpty()
        environment.savedRecordList.single().isDeleted shouldBe false
    }

    private fun assertUndone() {
        composeRule.waitUntilRoutineHomeQuickRecordNode(routineHomeQuickRecordCard(WATER_TITLE, "오늘 1500 / 2000ml", "75%"))
        environment.savedRecordList.single().isDeleted shouldBe true
    }

    private fun undo() {
        composeRule.onNodeWithText(ROUTINE_HOME_QUICK_RECORD_UNDO_ACTION).performClick()
        composeRule.waitForIdle()
    }

    private fun swipeDelete(title: String) {
        composeRule.onNode(routineHomeQuickRecordCard(title)).performTouchInput { swipeLeft() }
        composeRule.waitForIdle()
    }

    private fun switchToCalendarAndBack() {
        composeRule.onNodeWithContentDescription(ROUTINE_HOME_QUICK_RECORD_CALENDAR_VIEW_DESCRIPTION).performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithContentDescription(ROUTINE_HOME_QUICK_RECORD_LIST_VIEW_DESCRIPTION).performClick()
        composeRule.waitForIdle()
    }

    private fun setRoutineHome(vararg summary: RoutineSummary) {
        environment.setSummaryList(summary.toList())
        composeRule.setContent { environment.Content() }
        composeRule.waitForIdle()
        summary.forEach { value -> composeRule.waitUntilRoutineHomeQuickRecordNode(routineHomeQuickRecordCard(value.routine.detail.title)) }
    }

    private fun waterSummary(cycleAmount: Long): RoutineSummary =
        routineHomeQuickRecordSummary(
            title = WATER_TITLE,
            startDate = START_DATE,
            unit = "ml",
            goalAmount = WATER_GOAL_AMOUNT,
            amountPerRecord = WATER_AMOUNT_PER_RECORD,
            cycleAmount = cycleAmount,
        )

    private fun countSummary(title: String): RoutineSummary = routineHomeQuickRecordSummary(title = title, startDate = START_DATE, unit = "회", amountPerRecord = 1)

    private companion object {
        val TODAY: LocalDate = LocalDate(year = 2026, month = 7, day = 15)
        val START_DATE: LocalDate = LocalDate(year = 2026, month = 7, day = 1)
        const val WATER_TITLE = "물 마시기"
        const val WATER_RECORDED_MESSAGE = "물 마시기 250ml 기록"
        const val ROUTINE_A_TITLE = "루틴 A"
        const val ROUTINE_A_RECORDED_TEXT = "$ROUTINE_A_TITLE 1회 기록"
        const val ROUTINE_B_TITLE = "루틴 B"
        const val WATER_GOAL_AMOUNT = 2_000
        const val WATER_AMOUNT_PER_RECORD = 250
    }
}

private const val TODAY_1750_OF_2000_ML_TEXT = "오늘 1750 / 2000ml"
