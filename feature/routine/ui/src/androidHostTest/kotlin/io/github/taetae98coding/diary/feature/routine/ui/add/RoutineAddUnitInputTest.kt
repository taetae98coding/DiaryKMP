package io.github.taetae98coding.diary.feature.routine.ui.add

import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTextReplacement
import io.github.taetae98coding.diary.core.model.routine.RoutineCycle
import io.github.taetae98coding.diary.feature.routine.ui.RoutineTestTheme
import io.github.taetae98coding.diary.feature.routine.ui.form.ROUTINE_FORM_DEFAULT_CYCLE_WEEKDAYS
import io.github.taetae98coding.diary.feature.routine.ui.form.ROUTINE_FORM_DEFAULT_CYCLE_WEEKENDS
import io.github.taetae98coding.diary.feature.routine.ui.form.RoutineFormState
import io.github.taetae98coding.diary.feature.routine.ui.form.rememberRoutineAddFormState
import io.github.taetae98coding.diary.feature.routine.ui.form.routineFormAmountPerRecordInput
import io.github.taetae98coding.diary.feature.routine.ui.form.routineFormDateButton
import io.github.taetae98coding.diary.feature.routine.ui.form.routineFormEditableText
import io.github.taetae98coding.diary.feature.routine.ui.form.routineFormGoalAmountInput
import io.github.taetae98coding.diary.feature.routine.ui.form.routineFormInputUnit
import io.github.taetae98coding.diary.feature.routine.ui.form.routineFormInputtedUnit
import io.github.taetae98coding.diary.feature.routine.ui.form.routineFormPickDate
import io.github.taetae98coding.diary.feature.routine.ui.form.routineFormSelectCycle
import io.github.taetae98coding.diary.feature.routine.ui.form.routineFormSelectedCycle
import io.github.taetae98coding.diary.feature.routine.ui.form.routineUnitInput
import io.github.taetae98coding.diary.feature.routine.ui.resetAndroidUiDispatcher
import io.kotest.matchers.collections.shouldHaveSize
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
class RoutineAddUnitInputTest {
    @get:Rule
    val composeRule = createComposeRule()

    private lateinit var state: RoutineFormState

    @Before
    fun setUp() {
        resetAndroidUiDispatcher()
    }

    @Test
    fun `TC-e9205b85-abcf-4687-8f9a-4be8d4cf1c54 단위는 글자로 직접 적고 미리 정해 둔 단위에서 고르는 방법은 없다`() {
        setForm()
        composeRule.routineFormInputtedUnit() shouldBe ""

        listOf(COUNT, MILLILITER, "쪽").forEach { unit ->
            composeRule.routineUnitInput().performTextClearance()
            composeRule.routineUnitInput().performTextInput(unit)
            composeRule.waitForIdle()

            composeRule.routineFormInputtedUnit() shouldBe unit
        }

        // 고르는 컨트롤은 주기 하나뿐이고, 단위 입력을 눌러도 고를 항목이 펼쳐지지 않는다.
        composeRule.onAllNodes(SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.DropdownList)).fetchSemanticsNodes() shouldHaveSize 1
        val selectableCount = selectableNodeCount()
        composeRule.routineUnitInput().performClick()
        composeRule.waitForIdle()
        selectableNodeCount() shouldBe selectableCount
    }

    @Test
    fun `TC-d0d7c5a7-8c1d-42db-b49e-030df4ce48cd 단위를 적거나 고치거나 비워도 목표량과 한 번에 기록할 양에 입력한 숫자는 그대로다`() {
        setForm(goalAmount = 2000, amountPerRecord = 250)
        composeRule.routineFormInputtedUnit() shouldBe ""

        listOf(MILLILITER, "L", "").forEach { unit ->
            if (unit.isEmpty()) composeRule.routineUnitInput().performTextClearance() else composeRule.routineFormInputUnit(unit)
            composeRule.waitForIdle()

            composeRule.routineFormInputtedUnit() shouldBe unit
            composeRule.routineFormGoalAmountInput().routineFormEditableText() shouldBe "2000"
            composeRule.routineFormAmountPerRecordInput().routineFormEditableText() shouldBe "250"
        }
    }

    @Test
    fun `TC-e8529bc9-3555-40f6-b62f-7ca372f6dc5d 단위에는 글자를 10자까지 한 줄로만 반영한다`() {
        setForm()

        assertUnitInput(
            listOf(
                Triple("", COUNT, COUNT),
                Triple(KILOMETER, "s", KILOMETERS),
                Triple(KILOMETERS, "s", KILOMETERS),
                Triple("가나다라마바사아자", "차", KOREAN_TEN),
                Triple(KOREAN_TEN, "카", KOREAN_TEN),
                Triple(MILLILITER, "\n", MILLILITER),
                Triple("", KILOMETERS_PER_HOUR, KILOMETERS),
                Triple("kilo", "meters per hour", KILOMETERS),
                Triple("", "밀리\n리터", "밀리리터"),
                Triple("", "kilo\nmeters per hour", KILOMETERS),
            ),
        )
    }

    @Test
    fun `TC-6047239e-9a3f-4542-a0ab-9b47b36de4fe 단위에는 글자의 종류를 가리지 않고 입력한 그대로 반영한다`() {
        setForm()

        assertUnitInput(listOf(COUNT, MILLILITER, "ML", "mL", "3세트", "km/h", "%", "분 걷기").map { unit -> Triple("", unit, unit) })
    }

    @Test
    fun `TC-1fc17336-8c40-455b-b207-55a51f8f4f94 단위를 입력하는 동안에는 앞뒤에 공백을 둘 수 있고 그 공백도 10자에 포함해 센다`() {
        setForm()

        assertUnitInput(
            listOf(
                Triple("", " ", " "),
                Triple(" ", MILLILITER, " ml"),
                Triple(MILLILITER, " ", "ml "),
                Triple(" kilomete", "r", SPACE_KILOMETER),
                Triple(SPACE_KILOMETER, "s", SPACE_KILOMETER),
                Triple("$KILOMETER ", " ", "$KILOMETER "),
            ),
        )
    }

    @Test
    fun `단위에는 여러 종류의 줄바꿈 글자를 입력할 수 없고 그림 글자는 한 글자로 센다`() {
        setForm()
        val emoji = "💧"

        assertUnitInput(
            listOf(
                Triple(MILLILITER, "\r\n", MILLILITER),
                Triple(MILLILITER, " ", MILLILITER),
                Triple("", "a\u0085b\u000Bc", "abc"),
                Triple(emoji.repeat(9), emoji, emoji.repeat(10)),
                Triple(emoji.repeat(10), emoji, emoji.repeat(10)),
                Triple(emoji.repeat(10), "a", emoji.repeat(10)),
            ),
        )
    }

    @Test
    fun `단위 입력 상태는 입력한 글자와 앞뒤 공백을 뺀 단위를 따로 알린다`() {
        setForm()

        composeRule.routineFormInputUnit(SPACED_WALK)

        composeRule.runOnIdle {
            state.unitState.text shouldBe SPACED_WALK
            state.unitState.unit shouldBe "분 걷기"
            state.unitState.hasText shouldBe true
            state.input.unit shouldBe SPACED_WALK
        }

        composeRule.runOnIdle { state.unitState.clear() }
        composeRule.waitForIdle()

        composeRule.routineFormInputtedUnit() shouldBe ""
        composeRule.runOnIdle { state.unitState.hasText shouldBe false }
    }

    @Test
    fun `TC-812af3a7-1e38-492a-b91c-76d97c5a3e5d 평일만이나 주말만을 골라도 시작 날짜는 어느 요일의 날짜든 고를 수 있다`() {
        setForm()
        var startDate = START_DATE

        listOf(
            Triple(ROUTINE_FORM_DEFAULT_CYCLE_WEEKDAYS, RoutineCycle.WEEKDAYS, LocalDate(2026, 3, 21)),
            Triple(ROUTINE_FORM_DEFAULT_CYCLE_WEEKDAYS, RoutineCycle.WEEKDAYS, LocalDate(2026, 3, 22)),
            Triple(ROUTINE_FORM_DEFAULT_CYCLE_WEEKENDS, RoutineCycle.WEEKENDS, LocalDate(2026, 3, 20)),
            Triple(ROUTINE_FORM_DEFAULT_CYCLE_WEEKENDS, RoutineCycle.WEEKENDS, LocalDate(2026, 3, 23)),
        ).forEach { (cycleText, cycle, date) ->
            composeRule.routineFormSelectCycle(cycleText)

            composeRule.routineFormPickDate(from = startDate, to = date)
            startDate = date

            composeRule.routineFormDateButton(date).assertExists()
            composeRule.routineFormSelectedCycle() shouldBe cycleText
            composeRule.runOnIdle {
                state.ruleState.startDate shouldBe date
                state.ruleState.cycle shouldBe cycle
            }
        }
    }

    // 고를 수 있는 항목은 골라졌는지를 알리며 누를 수 있다. 설명 입력의 탭처럼 처음부터 있던 항목과 견주어 새로 펼쳐진 항목이 없는지 본다.
    private fun selectableNodeCount(): Int = composeRule.onAllNodes(hasClickAction() and SemanticsMatcher.keyIsDefined(SemanticsProperties.Selected)).fetchSemanticsNodes().size

    private fun assertUnitInput(rowList: List<Triple<String, String, String>>) {
        rowList.forEachIndexed { index, (before, input, expected) ->
            composeRule.routineUnitInput().performTextReplacement(before)
            composeRule.waitForIdle()
            (index to composeRule.routineFormInputtedUnit()) shouldBe (index to before)

            composeRule.routineUnitInput().performTextInput(input)
            composeRule.waitForIdle()

            (index to composeRule.routineFormInputtedUnit()) shouldBe (index to expected)
        }
    }

    private fun setForm(
        goalAmount: Int? = null,
        amountPerRecord: Int? = 1,
    ) {
        composeRule.setContent {
            RoutineTestTheme {
                state = rememberRoutineAddFormState(initialStartDate = START_DATE)
                RoutineAddScaffold(
                    onEvent = {},
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
        }
        composeRule.waitForIdle()
    }

    private companion object {
        val START_DATE = LocalDate(2026, 3, 20)
        const val COUNT = "회"
        const val MILLILITER = "ml"
        const val KILOMETER = "kilometer"
        const val KILOMETERS = "kilometers"
        const val SPACE_KILOMETER = " kilometer"
        const val KOREAN_TEN = "가나다라마바사아자차"
        const val SPACED_WALK = "  분 걷기 "
        const val KILOMETERS_PER_HOUR = "kilometers per hour"
    }
}
