package io.github.taetae98coding.diary.feature.routine.ui.home

import androidx.activity.ComponentActivity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isDialog
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performImeAction
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTextReplacement
import androidx.lifecycle.Lifecycle
import androidx.navigation3.runtime.NavBackStack
import io.github.taetae98coding.diary.core.model.routine.RoutineSummary
import io.github.taetae98coding.diary.core.navigation.ScreenNavKey
import io.github.taetae98coding.diary.feature.routine.api.RoutineHomeNavKey
import io.github.taetae98coding.diary.feature.routine.ui.resetAndroidUiDispatcher
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
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

// 양 입력 창은 제품 RoutineHomeScreen에서 빠른 기록을 길게 눌러 열고, 기록 저장은 UseCase mock이 흉내 낸다.
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "ko-w411dp-h891dp")
class RoutineAmountDialogTest {
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
    fun `TC-e9e3597c-9be5-4e61-8f08-4191ddf71a88 빠른 기록을 길게 누르면 한 번에 기록할 양이 입력된 양 입력 창이 열린다`() {
        setRoutineHome(waterSummary(cycleAmount = 1_500L))

        composeRule.longPressRoutineHomeQuickRecord(WATER_TITLE)

        composeRule.routineAmountDialogAmount() shouldBe "250"
        environment.savedRecordList.shouldBeEmpty()
        composeRule.onNode(routineHomeQuickRecordCard(WATER_TITLE, TODAY_1500_OF_2000_ML_TEXT)).assertExists()
    }

    @Test
    fun `TC-aa3bbc4f-6635-4bd1-ab3f-e8243128a23c 양 입력 창에서는 기록하는 날짜를 바꿀 수 없다`() {
        setRoutineHome(waterSummary(cycleAmount = 0L))

        composeRule.longPressRoutineHomeQuickRecord(WATER_TITLE)

        composeRule.onAllNodes(routineAmountDialogField()).assertCountEquals(1)
        dialogActionNameList() shouldContainExactlyInAnyOrder listOf(ROUTINE_AMOUNT_DIALOG_CONFIRM, ROUTINE_AMOUNT_DIALOG_CANCEL)
    }

    @Test
    fun `TC-7814a8ec-7408-4bf8-96d6-a52987e90c91 양에는 반각 숫자만 일곱 자리까지 입력된다`() {
        setRoutineHome(waterSummary(cycleAmount = 0L))
        composeRule.longPressRoutineHomeQuickRecord(WATER_TITLE)

        listOf(
            Triple("12", "3", "123"),
            Triple("12", "a", "12"),
            Triple("12", "-", "12"),
            Triple("12", "3a4", "1234"),
            Triple("", "123456789", SEVEN_DIGIT_TEXT),
            Triple(SEVEN_DIGIT_TEXT, "8", SEVEN_DIGIT_TEXT),
        ).forEach { (before, input, expected) ->
            replaceAmount(before)
            composeRule.onNode(routineAmountDialogField()).performTextInput(input)
            composeRule.waitForIdle()

            composeRule.routineAmountDialogAmount() shouldBe expected
        }
    }

    @Test
    fun `TC-c11c5e88-d55a-44da-accb-27591ab6b00d 양이 비어 있거나 0이면 확인할 수 없다`() {
        setRoutineHome(waterSummary(cycleAmount = 0L))
        composeRule.longPressRoutineHomeQuickRecord(WATER_TITLE)

        listOf("", "0", "000").forEach { amount ->
            replaceAmount(amount)

            composeRule.onNode(routineAmountDialogButton(ROUTINE_AMOUNT_DIALOG_CONFIRM)).assertIsNotEnabled()
            composeRule.onNode(routineAmountDialogButton(ROUTINE_AMOUNT_DIALOG_CONFIRM)).performClick()
            composeRule.onNode(routineAmountDialogField()).performImeAction()
            composeRule.waitForIdle()

            environment.savedRecordList.shouldBeEmpty()
            environment.recordParameterList.shouldBeEmpty()
            composeRule.routineAmountDialogAmount() shouldBe amount
        }

        listOf("1", "9999999").forEach { amount ->
            replaceAmount(amount)

            composeRule.onNode(routineAmountDialogButton(ROUTINE_AMOUNT_DIALOG_CONFIRM)).assertIsEnabled()
        }
    }

    @Test
    fun `TC-29093cde-144c-4980-9874-c74cbf93fbe8 300을 입력해 확인하면 창이 닫히고 오늘 기록으로 저장된다`() {
        assertConfirmRecorded(input = "300", amount = 300, cardText = "오늘 1800 / 2000ml", rateText = "90%")
    }

    @Test
    fun `TC-29093cde-144c-4980-9874-c74cbf93fbe8 앞에 0이 붙은 0500을 확인하면 500으로 저장된다`() {
        assertConfirmRecorded(input = "0500", amount = 500, cardText = "오늘 2000 / 2000ml", rateText = "100%")
    }

    @Test
    fun `TC-29093cde-144c-4980-9874-c74cbf93fbe8 양을 바꾸지 않고 확인하면 한 번에 기록할 양으로 저장된다`() {
        assertConfirmRecorded(input = null, amount = WATER_AMOUNT_PER_RECORD, cardText = "오늘 1750 / 2000ml", rateText = "87%")
    }

    @Test
    fun `TC-d07d0ba4-9783-4bdc-bbec-8e7fd0ff373e 취소하면 기록하지 않고 입력하던 양도 남지 않는다`() {
        assertClosedWithoutRecord {
            composeRule.onNode(routineAmountDialogButton(ROUTINE_AMOUNT_DIALOG_CANCEL)).performClick()
        }
    }

    @Test
    fun `TC-d07d0ba4-9783-4bdc-bbec-8e7fd0ff373e 창 밖으로 벗어나면 기록하지 않고 입력하던 양도 남지 않는다`() {
        assertClosedWithoutRecord {
            composeRule.runOnIdle { dismissRoutineAmountDialogOutside() }
        }
    }

    @Test
    fun `TC-a272d13b-0f9e-4e91-86f4-4e5651a02201 창에서 양을 바꿔 기록해도 한 번에 기록할 양은 바뀌지 않는다`() {
        setRoutineHome(waterSummary(cycleAmount = 0L))
        composeRule.longPressRoutineHomeQuickRecord(WATER_TITLE)
        replaceAmount("900")
        confirm()
        composeRule.waitUntilRoutineHomeQuickRecordNode(routineHomeQuickRecordCard(WATER_TITLE, "오늘 900 / 2000ml"))

        composeRule.clickRoutineHomeQuickRecord(WATER_TITLE)

        composeRule.waitUntilRoutineHomeQuickRecordNode(routineHomeQuickRecordCard(WATER_TITLE, "오늘 1150 / 2000ml", "57%"))
        environment.savedRecordList.map { record -> record.amount } shouldBe listOf(900, WATER_AMOUNT_PER_RECORD)
        environment.storedSummaryList
            .single()
            .routine.detail.amountPerRecord shouldBe WATER_AMOUNT_PER_RECORD
    }

    @Test
    fun `TC-8d9cb061-9a76-4810-a880-98923e37b409 화면이 재생성되어도 양 입력 창과 입력하던 양이 그대로 남는다`() {
        val restorationTester = StateRestorationTester(composeRule)
        setRoutineHome(waterSummary(cycleAmount = 0L)) { restorationTester.setContent { environment.Content() } }
        composeRule.longPressRoutineHomeQuickRecord(WATER_TITLE)
        replaceAmount("900")

        restorationTester.emulateSavedInstanceStateRestore()
        composeRule.waitForIdle()

        assertDialogKeptAndRecorded()
    }

    @Test
    fun `TC-8d9cb061-9a76-4810-a880-98923e37b409 백그라운드에 다녀와도 양 입력 창과 입력하던 양이 그대로 남는다`() {
        setRoutineHome(waterSummary(cycleAmount = 0L))
        composeRule.longPressRoutineHomeQuickRecord(WATER_TITLE)
        replaceAmount("900")

        moveToBackgroundAndReturn()

        assertDialogKeptAndRecorded()
    }

    @Test
    fun `TC-8d9cb061-9a76-4810-a880-98923e37b409 시스템이 앱을 정리했다가 다시 만들어도 양 입력 창과 입력하던 양이 그대로 남는다`() {
        val restorationTester = StateRestorationTester(composeRule)
        setRoutineHome(waterSummary(cycleAmount = 0L)) { restorationTester.setContent { environment.Content() } }
        composeRule.longPressRoutineHomeQuickRecord(WATER_TITLE)
        replaceAmount("900")

        // 시스템이 앱을 정리하면 ViewModel은 사라지고 저장해 둔 화면 상태만 복원된다.
        composeRule.runOnIdle { environment.viewModelStoreOwner = routineHomeQuickRecordViewModelStoreOwner() }
        restorationTester.emulateSavedInstanceStateRestore()
        composeRule.waitForIdle()

        assertDialogKeptAndRecorded()
    }

    @Test
    fun `TC-7b6971ee-9640-45c5-8c6a-6bf67c85ba73 양 입력 창이 열린 채 앱을 다시 실행하면 창은 닫혀 있고 입력하던 양은 남지 않는다`() {
        var launchCount by mutableIntStateOf(0)
        setRoutineHome(waterSummary(cycleAmount = 0L)) {
            composeRule.setContent {
                // 앱을 다시 실행하면 저장해 둔 화면 상태와 ViewModel이 없으므로 새 키로 전환 이력과 ViewModel을 다시 만든다.
                key(launchCount) {
                    val relaunchedBackStack = remember { NavBackStack<ScreenNavKey>(RoutineHomeQuickRecordOtherNavKey, RoutineHomeNavKey) }
                    environment.Content(navBackStack = relaunchedBackStack)
                }
            }
        }
        composeRule.longPressRoutineHomeQuickRecord(WATER_TITLE)
        replaceAmount("900")

        composeRule.runOnIdle {
            environment.viewModelStoreOwner = routineHomeQuickRecordViewModelStoreOwner()
            launchCount += 1
        }
        composeRule.waitForIdle()

        assertDialogClosedAndAmountReset()
    }

    @Test
    fun `TC-7b6971ee-9640-45c5-8c6a-6bf67c85ba73 양 입력 창이 열린 채 다른 주요 목적지에 다녀오면 창은 닫혀 있고 입력하던 양은 남지 않는다`() {
        setRoutineHome(waterSummary(cycleAmount = 0L))
        composeRule.longPressRoutineHomeQuickRecord(WATER_TITLE)
        replaceAmount("900")

        composeRule.runOnIdle { environment.backStack.remove(RoutineHomeNavKey) }
        composeRule.waitForIdle()
        composeRule.onNodeWithText(ROUTINE_HOME_QUICK_RECORD_OTHER_CONTENT).assertExists()
        composeRule.runOnIdle { environment.backStack.add(RoutineHomeNavKey) }
        composeRule.waitForIdle()

        assertDialogClosedAndAmountReset()
    }

    @Test
    fun `TC-0e39318e-df0e-4550-8a30-905115f4b2dd 창이 열려 있는 동안 오늘을 다시 정해도 창은 닫히지 않고 확인하면 그 시점의 오늘로 기록한다`() {
        setRoutineHome(waterSummary(cycleAmount = 0L))
        composeRule.longPressRoutineHomeQuickRecord(WATER_TITLE)

        composeRule.activityRule.scenario.moveToState(Lifecycle.State.CREATED)
        composeRule.waitForIdle()
        setRoutineHomeQuickRecordToday(TOMORROW)
        composeRule.activityRule.scenario.moveToState(Lifecycle.State.RESUMED)
        composeRule.waitForIdle()

        composeRule.routineAmountDialogAmount() shouldBe "250"
        confirm()
        environment.savedRecordList.map { record -> record.date } shouldBe listOf(TOMORROW)
    }

    private fun assertConfirmRecorded(
        input: String?,
        amount: Int,
        cardText: String,
        rateText: String,
    ) {
        val water = waterSummary(cycleAmount = 1_500L)
        setRoutineHome(water)
        composeRule.onNode(routineHomeQuickRecordCard(WATER_TITLE, TODAY_1500_OF_2000_ML_TEXT, "75%")).assertExists()
        composeRule.longPressRoutineHomeQuickRecord(WATER_TITLE)

        input?.let { value -> replaceAmount(value) }
        confirm()

        composeRule.onAllNodes(isDialog()).assertCountEquals(0)
        environment.savedRecordList.map { record -> Triple(record.routineId, record.date, record.amount) } shouldBe listOf(Triple(water.routine.id, TODAY, amount))
        composeRule.waitUntilRoutineHomeQuickRecordNode(routineHomeQuickRecordCard(WATER_TITLE, cardText, rateText))
    }

    private fun assertClosedWithoutRecord(close: () -> Unit) {
        setRoutineHome(waterSummary(cycleAmount = 1_500L))
        composeRule.longPressRoutineHomeQuickRecord(WATER_TITLE)
        replaceAmount("900")

        close()
        composeRule.waitForIdle()

        composeRule.onAllNodes(isDialog()).assertCountEquals(0)
        environment.savedRecordList.shouldBeEmpty()
        environment.recordParameterList.shouldBeEmpty()
        composeRule.onNode(routineHomeQuickRecordCard(WATER_TITLE, TODAY_1500_OF_2000_ML_TEXT, "75%")).assertExists()
        composeRule.routineHomeQuickRecordSnackbarCount() shouldBe 0

        composeRule.longPressRoutineHomeQuickRecord(WATER_TITLE)

        composeRule.routineAmountDialogAmount() shouldBe "250"
    }

    private fun assertDialogKeptAndRecorded() {
        composeRule.waitUntilRoutineHomeQuickRecordNode(routineAmountDialogField())
        composeRule.routineAmountDialogAmount() shouldBe "900"

        confirm()

        environment.savedRecordList.map { record -> record.amount } shouldBe listOf(900)
    }

    private fun assertDialogClosedAndAmountReset() {
        composeRule.waitUntilRoutineHomeQuickRecordNode(routineHomeQuickRecordCard(WATER_TITLE))
        composeRule.onAllNodes(isDialog()).assertCountEquals(0)
        environment.savedRecordList.shouldBeEmpty()
        environment.recordParameterList.shouldBeEmpty()

        composeRule.longPressRoutineHomeQuickRecord(WATER_TITLE)

        composeRule.routineAmountDialogAmount() shouldBe "250"
    }

    private fun moveToBackgroundAndReturn() {
        composeRule.activityRule.scenario.moveToState(Lifecycle.State.CREATED)
        composeRule.waitForIdle()
        composeRule.activityRule.scenario.moveToState(Lifecycle.State.RESUMED)
        composeRule.waitForIdle()
    }

    private fun replaceAmount(amount: String) {
        if (amount.isEmpty()) {
            composeRule.onNode(routineAmountDialogField()).performTextClearance()
        } else {
            composeRule.onNode(routineAmountDialogField()).performTextReplacement(amount)
        }
        composeRule.waitForIdle()
    }

    private fun confirm() {
        composeRule.onNode(routineAmountDialogButton(ROUTINE_AMOUNT_DIALOG_CONFIRM)).performClick()
        composeRule.waitForIdle()
    }

    // 양 입력을 뺀, 창 안에서 누를 수 있는 동작의 이름이다.
    private fun dialogActionNameList(): List<String> =
        composeRule
            .onAllNodes(hasClickAction() and !hasSetTextAction() and hasAnyAncestor(isDialog()))
            .fetchSemanticsNodes()
            .map { node ->
                node.config
                    .getOrNull(SemanticsProperties.Text)
                    .orEmpty()
                    .joinToString(separator = " ") { text -> text.text }
            }

    private fun setRoutineHome(
        summary: RoutineSummary,
        setContent: () -> Unit = { composeRule.setContent { environment.Content() } },
    ) {
        environment.setSummaryList(listOf(summary))
        setContent()
        composeRule.waitForIdle()
        composeRule.waitUntilRoutineHomeQuickRecordNode(routineHomeQuickRecordCard(summary.routine.detail.title))
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
        val TOMORROW: LocalDate = LocalDate(year = 2026, month = 7, day = 16)
        val START_DATE: LocalDate = LocalDate(year = 2026, month = 7, day = 1)
        const val WATER_TITLE = "물 마시기"
        const val WATER_GOAL_AMOUNT = 2_000
        const val WATER_AMOUNT_PER_RECORD = 250
    }
}

private const val TODAY_1500_OF_2000_ML_TEXT = "오늘 1500 / 2000ml"

private const val SEVEN_DIGIT_TEXT = "1234567"
