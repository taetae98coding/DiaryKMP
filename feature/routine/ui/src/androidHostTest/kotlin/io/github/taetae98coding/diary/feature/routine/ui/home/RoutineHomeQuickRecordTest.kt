package io.github.taetae98coding.diary.feature.routine.ui.home

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isDialog
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextReplacement
import io.github.taetae98coding.diary.core.model.routine.RoutineSummary
import io.github.taetae98coding.diary.feature.routine.ui.resetAndroidUiDispatcher
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.CompletableDeferred
import kotlinx.datetime.LocalDate
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

// 빠른 기록은 제품 RoutineHomeScreen과 RoutineHomeViewModel로 실행하고, 기록 저장은 UseCase mock이 흉내 낸다.
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "ko-w411dp-h891dp")
class RoutineHomeQuickRecordTest {
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
    fun `TC-240ed16d-72da-4867-b62a-6a28da5b6a08 빠른 기록을 누르면 되묻지 않고 오늘 기록이 저장되고 카드 문구가 바뀐다`() {
        val water = waterSummary(cycleAmount = 1_500L)
        setRoutineHome(water)
        composeRule.onNode(routineHomeQuickRecordCard(WATER_TITLE, "오늘 1500 / 2000ml", "75%")).assertExists()

        composeRule.clickRoutineHomeQuickRecord(WATER_TITLE)

        composeRule.onAllNodes(isDialog()).assertCountEquals(0)
        environment.savedRecordList.map { record -> Triple(record.routineId, record.date, record.amount) } shouldBe listOf(Triple(water.routine.id, TODAY, WATER_AMOUNT_PER_RECORD))
        composeRule.waitUntilRoutineHomeQuickRecordNode(routineHomeQuickRecordCard(WATER_TITLE, "오늘 1750 / 2000ml", "87%"))
    }

    @Test
    fun `TC-36ae0040-60aa-4f93-8865-58a3ac573834 목표량이 없는 루틴은 빠른 기록으로 합계만 늘어난다`() {
        setRoutineHome(routineHomeQuickRecordSummary(title = READING_TITLE, startDate = START_DATE, unit = "회", amountPerRecord = 1))
        composeRule.routineHomeQuickRecordCardTextList(READING_TITLE) shouldBe listOf(READING_TITLE, "오늘 0회")

        composeRule.clickRoutineHomeQuickRecord(READING_TITLE)

        composeRule.waitUntilRoutineHomeQuickRecordNode(routineHomeQuickRecordCard(READING_TITLE, "오늘 1회"))
        composeRule.routineHomeQuickRecordCardTextList(READING_TITLE) shouldBe listOf(READING_TITLE, "오늘 1회")
    }

    @Test
    fun `TC-f06587fb-6c22-4f41-ab61-22676e4b000f 빠른 기록은 누를 때마다 기록 하나를 새로 넣는다`() {
        setRoutineHome(waterSummary(cycleAmount = 0L))
        composeRule.onNode(routineHomeQuickRecordCard(WATER_TITLE, "오늘 0 / 2000ml", "0%")).assertExists()

        composeRule.clickRoutineHomeQuickRecord(WATER_TITLE)
        composeRule.waitUntilRoutineHomeQuickRecordNode(routineHomeQuickRecordCard(WATER_TITLE, TODAY_250_OF_2000_ML_TEXT))
        composeRule.clickRoutineHomeQuickRecord(WATER_TITLE)

        environment.savedRecordList.map { record -> record.date to record.amount } shouldBe List(2) { TODAY to WATER_AMOUNT_PER_RECORD }
        environment.savedRecordList.map { record -> record.id }.distinct() shouldHaveSize 2
        composeRule.waitUntilRoutineHomeQuickRecordNode(routineHomeQuickRecordCard(WATER_TITLE, "오늘 500 / 2000ml", "25%"))
    }

    @Test
    fun `TC-11267459-481b-4b8f-9bb6-465cb8505f96 달성률이 100이어도 빠른 기록을 계속 넣을 수 있다`() {
        setRoutineHome(waterSummary(cycleAmount = 2_000L))
        composeRule.onNode(routineHomeQuickRecordCard(WATER_TITLE, "오늘 2000 / 2000ml", "100%")).assertExists()

        composeRule.clickRoutineHomeQuickRecord(WATER_TITLE)

        environment.savedRecordList shouldHaveSize 1
        composeRule.waitUntilRoutineHomeQuickRecordNode(routineHomeQuickRecordCard(WATER_TITLE, "오늘 2250 / 2000ml", "100%"))
    }

    @Test
    fun `TC-fcd6c0ac-4cf3-4a03-88e4-337830cb9f33 빠른 기록을 누르면 기록 안내와 실행 취소를 표시한다`() {
        setRoutineHome(waterSummary(cycleAmount = 0L))

        composeRule.clickRoutineHomeQuickRecord(WATER_TITLE)

        composeRule.waitUntilRoutineHomeQuickRecordNode(hasText(WATER_250_ML_RECORDED_TEXT))
        composeRule.onNodeWithText(ROUTINE_HOME_QUICK_RECORD_UNDO_ACTION).assertExists()
    }

    @Test
    fun `TC-fcd6c0ac-4cf3-4a03-88e4-337830cb9f33 양을 입력해 기록하면 입력한 양의 기록 안내와 실행 취소를 표시한다`() {
        setRoutineHome(waterSummary(cycleAmount = 0L))

        composeRule.longPressRoutineHomeQuickRecord(WATER_TITLE)
        composeRule.onNode(routineAmountDialogField()).performTextReplacement("500")
        composeRule.onNode(routineAmountDialogButton(ROUTINE_AMOUNT_DIALOG_CONFIRM)).performClick()
        composeRule.waitForIdle()

        composeRule.waitUntilRoutineHomeQuickRecordNode(hasText("물 마시기 500ml 기록"))
        composeRule.onNodeWithText(ROUTINE_HOME_QUICK_RECORD_UNDO_ACTION).assertExists()
    }

    @Test
    fun `TC-9f044e4d-efb7-48c6-9ca9-61c80f0eb561 빠른 기록을 저장하지 못하면 카드 문구와 안내가 그대로이고 다시 기록할 수 있다`() {
        assertRecordRetryAfterFailure(expectedAmount = WATER_AMOUNT_PER_RECORD) {
            composeRule.clickRoutineHomeQuickRecord(WATER_TITLE)
        }
    }

    @Test
    fun `TC-9f044e4d-efb7-48c6-9ca9-61c80f0eb561 양을 입력한 기록을 저장하지 못하면 카드 문구와 안내가 그대로이고 다시 기록할 수 있다`() {
        assertRecordRetryAfterFailure(expectedAmount = WATER_AMOUNT_PER_RECORD) {
            composeRule.longPressRoutineHomeQuickRecord(WATER_TITLE)
            composeRule.onNode(routineAmountDialogButton(ROUTINE_AMOUNT_DIALOG_CONFIRM)).performClick()
            composeRule.waitForIdle()
        }
    }

    @Test
    fun `TC-0b5314d0-e101-4994-8bd9-4fe65a4b495c 실행 취소한 기록을 다시 되돌리는 동작은 제공하지 않는다`() {
        setRoutineHome(waterSummary(cycleAmount = 0L))
        composeRule.clickRoutineHomeQuickRecord(WATER_TITLE)
        composeRule.waitUntilRoutineHomeQuickRecordNode(hasText(ROUTINE_HOME_QUICK_RECORD_UNDO_ACTION))

        composeRule.onNodeWithText(ROUTINE_HOME_QUICK_RECORD_UNDO_ACTION).performClick()
        composeRule.waitForIdle()

        composeRule.waitUntilRoutineHomeQuickRecordNode(routineHomeQuickRecordCard(WATER_TITLE, "오늘 0 / 2000ml"))
        environment.savedRecordList.single().isDeleted shouldBe true
        composeRule.onNodeWithText(ROUTINE_HOME_QUICK_RECORD_UNDO_ACTION).assertDoesNotExist()
        composeRule.onNodeWithText(WATER_250_ML_RECORDED_TEXT).assertDoesNotExist()
        composeRule.mainClock.advanceTimeBy(ROUTINE_HOME_QUICK_RECORD_AFTER_SNACKBAR_DISMISS_MILLIS)
        composeRule.waitForIdle()
        composeRule.onNodeWithText(ROUTINE_HOME_QUICK_RECORD_UNDO_ACTION).assertDoesNotExist()
        environment.undoRecordIdList shouldHaveSize 1
    }

    @Test
    fun `TC-74ea6b30-22c7-46f6-bc81-0da504109eac 한 루틴의 기록을 처리하는 동안 같은 루틴의 기록은 다시 처리하지 않는다`() {
        val water = waterSummary(cycleAmount = 0L)
        val gate = CompletableDeferred<Unit>()
        environment.recordGateMap[water.routine.id] = gate
        setRoutineHome(water)

        composeRule.clickRoutineHomeQuickRecord(WATER_TITLE)
        composeRule.clickRoutineHomeQuickRecord(WATER_TITLE)
        composeRule.runOnIdle { gate.complete(Unit) }
        composeRule.waitForIdle()

        composeRule.waitUntilRoutineHomeQuickRecordNode(routineHomeQuickRecordCard(WATER_TITLE, TODAY_250_OF_2000_ML_TEXT))
        environment.savedRecordList.map { record -> record.amount } shouldBe listOf(WATER_AMOUNT_PER_RECORD)
    }

    @Test
    fun `TC-c8845230-5dba-4f21-abbf-d5c10ab79573 기록을 처리하는 동안 양 입력 창을 열어 확인하면 창이 닫히고 기록되지 않는다`() {
        val water = waterSummary(cycleAmount = 0L)
        val gate = CompletableDeferred<Unit>()
        environment.recordGateMap[water.routine.id] = gate
        setRoutineHome(water)

        composeRule.clickRoutineHomeQuickRecord(WATER_TITLE)
        composeRule.longPressRoutineHomeQuickRecord(WATER_TITLE)
        composeRule.onNode(routineAmountDialogField()).assertExists()
        composeRule.onNode(routineAmountDialogField()).performTextReplacement("500")
        composeRule.onNode(routineAmountDialogButton(ROUTINE_AMOUNT_DIALOG_CONFIRM)).performClick()
        composeRule.waitForIdle()

        composeRule.onAllNodes(isDialog()).assertCountEquals(0)
        assertNoRecordNotice()

        composeRule.runOnIdle { gate.complete(Unit) }
        composeRule.waitForIdle()

        composeRule.waitUntilRoutineHomeQuickRecordNode(routineHomeQuickRecordCard(WATER_TITLE, TODAY_250_OF_2000_ML_TEXT))
        environment.savedRecordList.map { record -> record.amount } shouldBe listOf(WATER_AMOUNT_PER_RECORD)
    }

    @Test
    fun `TC-aab9e2bd-2fab-4a45-a960-624c1951c2ff 처리가 끝나면 같은 루틴에 이어서 기록하고 다른 루틴은 처리 중에도 기록한다`() {
        val routineA = routineHomeQuickRecordSummary(title = ROUTINE_A_TITLE, startDate = START_DATE)
        val routineB = routineHomeQuickRecordSummary(title = ROUTINE_B_TITLE, startDate = START_DATE)
        val gate = CompletableDeferred<Unit>()
        environment.recordGateMap[routineA.routine.id] = gate
        setRoutineHome(routineA, routineB)

        composeRule.clickRoutineHomeQuickRecord(ROUTINE_A_TITLE)
        composeRule.clickRoutineHomeQuickRecord(ROUTINE_B_TITLE)

        environment.savedRecordList.map { record -> record.routineId } shouldBe listOf(routineB.routine.id)

        composeRule.runOnIdle {
            gate.complete(Unit)
            environment.recordGateMap.clear()
        }
        composeRule.waitForIdle()
        composeRule.clickRoutineHomeQuickRecord(ROUTINE_A_TITLE)

        environment.savedRecordList.count { record -> record.routineId == routineA.routine.id } shouldBe 2
        environment.savedRecordList.count { record -> record.routineId == routineB.routine.id } shouldBe 1
    }

    private fun assertRecordRetryAfterFailure(
        expectedAmount: Int,
        record: () -> Unit,
    ) {
        environment.isRecordSaveSuccess = false
        setRoutineHome(waterSummary(cycleAmount = 1_500L))

        record()

        composeRule.onNode(routineHomeQuickRecordCard(WATER_TITLE, "오늘 1500 / 2000ml", "75%")).assertExists()
        environment.savedRecordList.shouldBeEmpty()
        assertNoRecordNotice()

        environment.isRecordSaveSuccess = true
        composeRule.clickRoutineHomeQuickRecord(WATER_TITLE)

        environment.savedRecordList.map { saved -> saved.amount } shouldBe listOf(expectedAmount)
        composeRule.waitUntilRoutineHomeQuickRecordNode(routineHomeQuickRecordCard(WATER_TITLE, "오늘 1750 / 2000ml", "87%"))
    }

    // 기록 안내와 실패 안내는 모두 스낵바로 뜨므로, 안내가 놓이는 자리에 글이 없는지로 판정한다.
    private fun assertNoRecordNotice() {
        composeRule.onNodeWithText(ROUTINE_HOME_QUICK_RECORD_UNDO_ACTION).assertDoesNotExist()
        composeRule.onNodeWithText(WATER_250_ML_RECORDED_TEXT).assertDoesNotExist()
        composeRule.onNodeWithText("물 마시기 500ml 기록").assertDoesNotExist()
        composeRule.routineHomeQuickRecordSnackbarCount() shouldBe 0
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

    private companion object {
        val TODAY: LocalDate = LocalDate(year = 2026, month = 7, day = 15)
        val START_DATE: LocalDate = LocalDate(year = 2026, month = 7, day = 1)
        const val WATER_TITLE = "물 마시기"
        const val READING_TITLE = "독서"
        const val ROUTINE_A_TITLE = "루틴 A"
        const val ROUTINE_B_TITLE = "루틴 B"
        const val WATER_GOAL_AMOUNT = 2_000
        const val WATER_AMOUNT_PER_RECORD = 250
    }
}

private const val TODAY_250_OF_2000_ML_TEXT = "오늘 250 / 2000ml"

private const val WATER_250_ML_RECORDED_TEXT = "물 마시기 250ml 기록"
