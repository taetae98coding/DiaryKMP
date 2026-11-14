package io.github.taetae98coding.diary.feature.routine.ui.add

import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTextReplacement
import io.github.taetae98coding.diary.feature.routine.ui.RoutineTestTheme
import io.github.taetae98coding.diary.feature.routine.ui.form.ROUTINE_FORM_DEFAULT_CYCLE_DAILY
import io.github.taetae98coding.diary.feature.routine.ui.form.ROUTINE_FORM_DEFAULT_CYCLE_MONTHLY
import io.github.taetae98coding.diary.feature.routine.ui.form.ROUTINE_FORM_DEFAULT_CYCLE_WEEKDAYS
import io.github.taetae98coding.diary.feature.routine.ui.form.ROUTINE_FORM_DEFAULT_CYCLE_WEEKENDS
import io.github.taetae98coding.diary.feature.routine.ui.form.ROUTINE_FORM_DEFAULT_CYCLE_WEEKLY
import io.github.taetae98coding.diary.feature.routine.ui.form.ROUTINE_FORM_UNIT_MILLILITER
import io.github.taetae98coding.diary.feature.routine.ui.form.RoutineFormState
import io.github.taetae98coding.diary.feature.routine.ui.form.rememberRoutineAddFormState
import io.github.taetae98coding.diary.feature.routine.ui.form.routineCycleMenuOptionList
import io.github.taetae98coding.diary.feature.routine.ui.form.routineFormAmountPerRecordInput
import io.github.taetae98coding.diary.feature.routine.ui.form.routineFormCancelDatePicker
import io.github.taetae98coding.diary.feature.routine.ui.form.routineFormChangeColor
import io.github.taetae98coding.diary.feature.routine.ui.form.routineFormColor
import io.github.taetae98coding.diary.feature.routine.ui.form.routineFormConfirmDatePicker
import io.github.taetae98coding.diary.feature.routine.ui.form.routineFormDateButton
import io.github.taetae98coding.diary.feature.routine.ui.form.routineFormDisplayedColorHex
import io.github.taetae98coding.diary.feature.routine.ui.form.routineFormEditableText
import io.github.taetae98coding.diary.feature.routine.ui.form.routineFormGoalAmountInput
import io.github.taetae98coding.diary.feature.routine.ui.form.routineFormInputUnit
import io.github.taetae98coding.diary.feature.routine.ui.form.routineFormInputtedUnit
import io.github.taetae98coding.diary.feature.routine.ui.form.routineFormOpenDatePicker
import io.github.taetae98coding.diary.feature.routine.ui.form.routineFormPickerSelection
import io.github.taetae98coding.diary.feature.routine.ui.form.routineFormSelectCycle
import io.github.taetae98coding.diary.feature.routine.ui.form.routineFormSelectedCycle
import io.github.taetae98coding.diary.feature.routine.ui.form.routineFormTitleInput
import io.github.taetae98coding.diary.feature.routine.ui.form.routineFormTypePickerDate
import io.github.taetae98coding.diary.feature.routine.ui.resetAndroidUiDispatcher
import io.github.taetae98coding.diary.library.compose.ui.color.toHexString
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.shouldBe
import kotlinx.datetime.LocalDate
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w480dp-h1200dp")
class RoutineAddFormTest {
    @get:Rule
    val composeRule = createComposeRule()

    private lateinit var state: RoutineFormState
    private val addEventList = mutableListOf<RoutineAddScaffoldEvent>()

    @Before
    fun setUp() {
        resetAndroidUiDispatcher()
    }

    @Test
    fun `TC-c2e105de-e58e-43ea-847c-25a0a7f11dbb 컬러를 바꾸면 바꾼 컬러가 표시된다`() {
        setForm(startDate = START_DATE)
        val colorA = composeRule.routineFormDisplayedColorHex()
        val colorB = generateSequence { routineFormColor().toHexString() }.first { hex -> hex != colorA }

        composeRule.routineFormChangeColor(fromHex = colorA, toHex = colorB)

        composeRule.routineFormDisplayedColorHex() shouldBe colorB
        addEventList.shouldBeEmpty()
    }

    @Test
    fun `TC-479ba6c5-3b0e-4823-aa9d-2fa4e02fb28d 시작 날짜 고르기는 지금 시작 날짜에서 시작하고 확인하면 오늘보다 앞의 날짜가 시작 날짜가 된다`() {
        assertStartDatePicked(LocalDate(2020, 5, 1))
    }

    @Test
    fun `TC-479ba6c5-3b0e-4823-aa9d-2fa4e02fb28d 시작 날짜 고르기는 지금 시작 날짜에서 시작하고 확인하면 오늘보다 뒤의 날짜가 시작 날짜가 된다`() {
        assertStartDatePicked(LocalDate(2030, 12, 31))
    }

    private fun assertStartDatePicked(date: LocalDate) {
        setForm(startDate = START_DATE)

        composeRule.routineFormOpenDatePicker(START_DATE)
        composeRule.routineFormPickerSelection(START_DATE).assertExists()
        composeRule.routineFormTypePickerDate(date)
        composeRule.routineFormConfirmDatePicker()

        composeRule.routineFormDateButton(date).assertExists()
        composeRule.routineFormDateButton(START_DATE).assertDoesNotExist()
    }

    @Test
    fun `TC-7393ced4-bc55-46e2-99b4-32240f6c9f41 시작 날짜 고르기를 취소하면 고르기 전 시작 날짜를 유지한다`() {
        setForm(startDate = START_DATE)

        composeRule.routineFormOpenDatePicker(START_DATE)
        composeRule.routineFormTypePickerDate(LocalDate(2026, 4, 1))
        composeRule.routineFormCancelDatePicker()

        composeRule.routineFormDateButton(START_DATE).assertExists()
        composeRule.routineFormDateButton(LocalDate(2026, 4, 1)).assertDoesNotExist()
    }

    @Test
    fun `TC-f8478e48-9874-451d-bf52-69d7a89ba7c6 시작 날짜는 지울 수 없다`() {
        setForm(startDate = START_DATE, amountPerRecord = null)

        // 글자 입력을 모두 비워 두면 화면에 지우는 동작이 하나도 없다.
        composeRule.onAllNodes(clearAction()).fetchSemanticsNodes().shouldBeEmpty()
        composeRule.routineFormOpenDatePicker(START_DATE)
        // 날짜 고르기에는 확인과 취소만 있고 날짜를 비우는 동작은 없다.
        composeRule.onAllNodes(clearAction()).fetchSemanticsNodes().shouldBeEmpty()
        composeRule.routineFormCancelDatePicker()
        composeRule.routineFormDateButton(START_DATE).assertExists()
    }

    @Test
    fun `TC-aedbfaff-6672-4617-9c1d-3533fa4b5a1d 주기는 다섯 값 중 하나가 언제나 골라져 있고 바꿔도 다른 입력은 바뀌지 않는다`() {
        setFilledForm()
        composeRule.routineFormSelectedCycle() shouldBe ROUTINE_FORM_DEFAULT_CYCLE_DAILY
        composeRule.routineCycleMenuOptionList() shouldBe
            listOf(
                ROUTINE_FORM_DEFAULT_CYCLE_DAILY,
                ROUTINE_FORM_DEFAULT_CYCLE_WEEKDAYS,
                ROUTINE_FORM_DEFAULT_CYCLE_WEEKENDS,
                ROUTINE_FORM_DEFAULT_CYCLE_WEEKLY,
                ROUTINE_FORM_DEFAULT_CYCLE_MONTHLY,
            )

        listOf(
            ROUTINE_FORM_DEFAULT_CYCLE_WEEKDAYS,
            ROUTINE_FORM_DEFAULT_CYCLE_WEEKENDS,
            ROUTINE_FORM_DEFAULT_CYCLE_WEEKLY,
            ROUTINE_FORM_DEFAULT_CYCLE_MONTHLY,
            ROUTINE_FORM_DEFAULT_CYCLE_DAILY,
        ).forEach { cycle ->
            composeRule.routineFormSelectCycle(cycle)

            composeRule.routineFormSelectedCycle() shouldBe cycle
            assertFilledFormExceptCycleAndUnit()
            composeRule.routineFormInputtedUnit() shouldBe ROUTINE_FORM_UNIT_MILLILITER
        }

        // 골라진 주기를 다시 눌러도 고르지 않은 상태가 되지 않는다.
        composeRule.routineFormSelectCycle(ROUTINE_FORM_DEFAULT_CYCLE_DAILY)
        composeRule.routineFormSelectedCycle() shouldBe ROUTINE_FORM_DEFAULT_CYCLE_DAILY
    }

    @Test
    fun `TC-45042d5f-3ef9-4560-ad51-e6c8b8445a88 목표량과 한 번에 기록할 양에는 반각 숫자만 일곱 자리까지 반영한다`() {
        setForm(startDate = START_DATE)

        listOf(
            Triple("", "2000", "2000"),
            Triple("", "0500", "0500"),
            Triple("12", "a", "12"),
            Triple("12", "-", "12"),
            Triple("12", ".", "12"),
            Triple("12", "３", "12"),
            Triple(SEVEN_DIGIT_TEXT, "8", SEVEN_DIGIT_TEXT),
            Triple("", "9999999", "9999999"),
            Triple("", "123456789", SEVEN_DIGIT_TEXT),
            Triple("12", "3a4", "1234"),
            Triple("999999", "123", "9999991"),
        ).forEach { (before, input, expected) ->
            listOf(composeRule.routineFormGoalAmountInput(), composeRule.routineFormAmountPerRecordInput()).forEach { amountInput ->
                amountInput.performTextReplacement(before)
                composeRule.waitForIdle()

                amountInput.performTextInput(input)
                composeRule.waitForIdle()

                amountInput.routineFormEditableText() shouldBe expected
            }
        }
    }

    @Test
    fun `TC-16ff4671-d0dc-49fa-a820-ade064267ac6 한 번에 기록할 양은 입력하는 동안 모두 지워 비워 둘 수 있다`() {
        setForm(startDate = START_DATE)
        composeRule.routineFormAmountPerRecordInput().routineFormEditableText() shouldBe "1"

        composeRule.routineFormAmountPerRecordInput().performTextClearance()
        composeRule.waitForIdle()

        composeRule.routineFormAmountPerRecordInput().routineFormEditableText() shouldBe ""
        // 다른 입력으로 초점을 옮겨도 비운 값이 다른 값으로 채워지지 않는다.
        composeRule.runOnIdle { state.titleState.requestFocus() }
        composeRule.waitForIdle()
        composeRule.routineFormAmountPerRecordInput().routineFormEditableText() shouldBe ""
    }

    private fun setFilledForm() {
        setForm(
            startDate = FILLED_START_DATE,
            goalAmount = GOAL_AMOUNT,
            amountPerRecord = AMOUNT_PER_RECORD,
        )
        composeRule.routineFormTitleInput().performTextInput(DRINK_WATER)
        composeRule.routineFormInputUnit(ROUTINE_FORM_UNIT_MILLILITER)
    }

    private fun assertFilledFormExceptCycleAndUnit() {
        composeRule.routineFormTitleInput().routineFormEditableText() shouldBe DRINK_WATER
        composeRule.routineFormDateButton(FILLED_START_DATE).assertExists()
        composeRule.routineFormGoalAmountInput().routineFormEditableText() shouldBe GOAL_AMOUNT.toString()
        composeRule.routineFormAmountPerRecordInput().routineFormEditableText() shouldBe AMOUNT_PER_RECORD.toString()
    }

    private fun setForm(
        startDate: LocalDate,
        goalAmount: Int? = null,
        amountPerRecord: Int? = DEFAULT_AMOUNT_PER_RECORD,
    ) {
        composeRule.setContent {
            RoutineTestTheme {
                state = rememberRoutineAddFormState(initialStartDate = startDate)
                RoutineAddScaffold(
                    onEvent = addEventList::add,
                    onFormEvent = {},
                    onTagPickerEvent = {},
                    state = state,
                )
            }
        }
        composeRule.waitForIdle()
        composeRule.runOnIdle {
            state.goalAmountState.setAmount(amount = goalAmount)
            state.amountPerRecordState.setAmount(amount = amountPerRecord)
            state.titleState.requestFocus()
        }
        composeRule.waitForIdle()
    }

    private companion object {
        const val DRINK_WATER = "물 마시기"
        const val GOAL_AMOUNT = 2000
        const val AMOUNT_PER_RECORD = 250
        const val DEFAULT_AMOUNT_PER_RECORD = 1
        val START_DATE = LocalDate(2026, 3, 20)
        val FILLED_START_DATE = LocalDate(2026, 3, 1)

        fun clearAction() = hasClickAction() and (hasContentDescription("Clear", substring = true) or hasText("Clear", substring = true))
    }
}

private const val SEVEN_DIGIT_TEXT = "1234567"
