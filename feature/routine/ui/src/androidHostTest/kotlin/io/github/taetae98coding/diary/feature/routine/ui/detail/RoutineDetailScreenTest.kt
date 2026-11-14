package io.github.taetae98coding.diary.feature.routine.ui.detail

import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.assertIsNotFocused
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.isDialog
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import io.github.taetae98coding.diary.compose.routine.ROUTINE_CARD_TEST_TAG
import io.github.taetae98coding.diary.compose.routine.ROUTINE_PROGRESS_BAR_TEST_TAG
import io.github.taetae98coding.diary.core.model.routine.RoutineCycle
import io.github.taetae98coding.diary.core.model.routine.RoutineDetail
import io.github.taetae98coding.diary.core.model.routine.RoutineRecord
import io.github.taetae98coding.diary.core.testing.routine.routine
import io.github.taetae98coding.diary.domain.routine.ROUTINE_AMOUNT_RANGE
import io.github.taetae98coding.diary.domain.routine.RoutineInput
import io.github.taetae98coding.diary.domain.routine.exception.RoutineAmountPerRecordInvalidException
import io.github.taetae98coding.diary.domain.routine.exception.RoutineGoalAmountInvalidException
import io.github.taetae98coding.diary.domain.routine.usecase.DeleteRoutineUseCase
import io.github.taetae98coding.diary.domain.routine.usecase.FindRoutineUseCase
import io.github.taetae98coding.diary.domain.routine.usecase.UpdateRoutineUseCase
import io.github.taetae98coding.diary.feature.routine.ui.detail.record.RoutineDetailRecordEffect
import io.github.taetae98coding.diary.feature.routine.ui.form.ROUTINE_FORM_DEFAULT_CYCLE_WEEKDAYS
import io.github.taetae98coding.diary.feature.routine.ui.form.ROUTINE_FORM_DEFAULT_CYCLE_WEEKENDS
import io.github.taetae98coding.diary.feature.routine.ui.form.routineInputUnit
import io.github.taetae98coding.diary.feature.routine.ui.form.routineSelectCycle
import io.github.taetae98coding.diary.feature.routine.ui.form.routineUnitInput
import io.github.taetae98coding.diary.feature.routine.ui.resetAndroidUiDispatcher
import io.github.taetae98coding.diary.library.fixture.kotlinUuid
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import kotlinx.coroutines.flow.flowOf
import kotlinx.datetime.LocalDate
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w480dp-h3000dp")
class RoutineDetailScreenTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Before
    fun setUp() {
        resetAndroidUiDispatcher()
    }

    @Test
    fun `TC-b6fd6edb-d8e4-4425-8d5e-73bffaed9f42 조회에 성공하면 저장된 정보로 화면을 채운다`() {
        val environment = RoutineDetailTestEnvironment()
        setScreen(environment)

        composeRule.onNode(routineDetailTopBarTitle(ROUTINE_DETAIL_WATER), useUnmergedTree = true).assertExists()
        composeRule.routineDetailTitleInput().routineDetailEditableText() shouldBe ROUTINE_DETAIL_WATER
        composeRule.routineDetailDescriptionInput().routineDetailEditableText() shouldBe ROUTINE_DETAIL_DESCRIPTION
        composeRule.routineDetailDisplayedColorHex() shouldBe environment.detail.color.routineDetailColorHex()
        composeRule.routineDetailDateButton(LocalDate(2026, 3, 1)).assertExists()
        composeRule.routineDetailSelectedCycle() shouldBe ROUTINE_DETAIL_DEFAULT_CYCLE_WEEKLY
        composeRule.routineDetailInputtedUnit() shouldBe ROUTINE_DETAIL_UNIT_MILLILITER
        composeRule.routineDetailGoalInput().routineDetailEditableText() shouldBe "2000"
        composeRule.routineDetailAmountPerRecordInput().routineDetailEditableText() shouldBe "250"
    }

    @Test
    fun `TC-57287973-6208-4bc5-bc84-aa8dc7fae643 설명, 단위나 목표량이 없으면 비운 채로 채운다`() {
        setScreen(RoutineDetailTestEnvironment(detail = routineDetailOf(description = "", unit = "", goalAmount = null)))

        composeRule.routineDetailDescriptionInput().routineDetailEditableText() shouldBe ""
        composeRule.routineDetailInputtedUnit() shouldBe ""
        composeRule.routineDetailGoalInput().routineDetailEditableText() shouldBe ""
    }

    @Test
    fun `TC-01798d3d-c1a6-4c92-9226-ba67b4b156c5 진입할 때 어느 입력에도 초점을 두지 않는다`() {
        setScreen(RoutineDetailTestEnvironment())

        composeRule.routineDetailTitleInput().assertIsNotFocused()
        composeRule.routineDetailDescriptionInput().assertIsNotFocused()
        composeRule.routineDetailGoalInput().assertIsNotFocused()
        composeRule.routineDetailAmountPerRecordInput().assertIsNotFocused()
        composeRule.routineDetailRecordAmountInput().assertIsNotFocused()
    }

    @Test
    fun `TC-9b3c9a27-1920-4835-a64c-7580d8f0e91f 조회 중에는 조회 중 상태만 보이고 입력과 동작을 실행할 수 없다`() {
        val environment = RoutineDetailTestEnvironment(recordList = listOf(record(date = LocalDate(2026, 3, 5), amount = 250)))
        environment.uiState.value = RoutineDetailUiState.Loading
        setScreen(environment)

        composeRule.onNodeWithContentDescription(ROUTINE_DETAIL_DEFAULT_LOADING).assertExists()
        composeRule.onNode(hasSetTextAction()).assertDoesNotExist()
        composeRule.onNodeWithText(ROUTINE_DETAIL_DEFAULT_TAG_LABEL).assertDoesNotExist()
        composeRule.onNode(routineDetailDeleteButton()).assertDoesNotExist()
        composeRule.onNode(routineDetailUpdateButton()).assertDoesNotExist()
        composeRule.onNode(routineDetailRecordAddButton()).assertDoesNotExist()
        composeRule.routineDetailRecordRowList().shouldBeEmpty()
        composeRule.routineDetailActionNameList() shouldBe listOf(ROUTINE_DETAIL_DEFAULT_NAVIGATE_UP)
    }

    @Test
    fun `TC-32b3f574-fee2-44c6-8c5b-a7c69d761731 조회할 수 없으면 조회 중 상태를 유지하고 오류 안내와 다시 시도를 두지 않는다`() {
        val environment = RoutineDetailTestEnvironment()
        environment.uiState.value = RoutineDetailUiState.Loading
        setScreen(environment)

        composeRule.onNodeWithContentDescription(ROUTINE_DETAIL_DEFAULT_LOADING).assertExists()
        composeRule.routineDetailActionNameList() shouldBe listOf(ROUTINE_DETAIL_DEFAULT_NAVIGATE_UP)
        composeRule.routineDetailVisibleTextList().shouldBeEmpty()
    }

    @Test
    fun `TC-6a0800ae-2768-48c8-ba65-18c8dd1c647b 화면에는 정해진 입력과 동작만 두고 카드 문구는 두지 않는다`() {
        val environment =
            RoutineDetailTestEnvironment(
                detail = routineDetailOf(startDate = LocalDate(2026, 3, 1)),
                recordList = listOf(record(date = LocalDate(2026, 3, 1), amount = 250)),
            )
        // 기록할 날짜의 처음 값이 오늘이므로 오늘을 고정한다.
        routineRecordSetToday(LocalDate(2026, 3, 20))
        try {
            setScreen(environment)
        } finally {
            routineRecordReleaseToday()
        }
        composeRule.routineDetailTitleInput().performTextInput("2")
        composeRule.waitForIdle()
        val color = composeRule.routineDetailDisplayedColorHex()
        val date = routineDetailDisplayText(LocalDate(2026, 3, 1))

        val actionNameList =
            composeRule.routineDetailActionNameList().map { name ->
                when {
                    name.startsWith(color) -> COLOR_INPUT
                    name.startsWith(ROUTINE_DETAIL_DEFAULT_GOAL_LABEL) -> ROUTINE_DETAIL_DEFAULT_GOAL_LABEL
                    name.startsWith(ROUTINE_DETAIL_DEFAULT_AMOUNT_PER_RECORD_LABEL) -> ROUTINE_DETAIL_DEFAULT_AMOUNT_PER_RECORD_LABEL
                    name.startsWith(ROUTINE_DETAIL_DEFAULT_RECORD_AMOUNT_LABEL) -> ROUTINE_DETAIL_DEFAULT_RECORD_AMOUNT_LABEL
                    else -> name
                }
            }

        actionNameList shouldContainExactlyInAnyOrder
            listOf(
                ROUTINE_DETAIL_DEFAULT_NAVIGATE_UP,
                ROUTINE_DETAIL_DEFAULT_FINISH,
                ROUTINE_DETAIL_DEFAULT_DELETE,
                ROUTINE_DETAIL_DEFAULT_TITLE_LABEL,
                ROUTINE_DETAIL_DEFAULT_DESCRIPTION_LABEL,
                ROUTINE_DETAIL_DEFAULT_DESCRIPTION_INPUT_TAB,
                ROUTINE_DETAIL_DEFAULT_DESCRIPTION_PREVIEW_TAB,
                date,
                ROUTINE_DETAIL_DEFAULT_CYCLE_WEEKLY,
                ROUTINE_DETAIL_DEFAULT_UNIT_LABEL,
                ROUTINE_DETAIL_DEFAULT_GOAL_LABEL,
                ROUTINE_DETAIL_DEFAULT_AMOUNT_PER_RECORD_LABEL,
                // 값이 있는 제목, 설명, 단위, 목표량과 한 번에 기록할 양 입력의 지우기 버튼이다.
                ROUTINE_DETAIL_DEFAULT_CLEAR_TEXT,
                ROUTINE_DETAIL_DEFAULT_CLEAR_TEXT,
                ROUTINE_DETAIL_DEFAULT_CLEAR_TEXT,
                ROUTINE_DETAIL_DEFAULT_CLEAR_TEXT,
                ROUTINE_DETAIL_DEFAULT_CLEAR_TEXT,
                COLOR_INPUT,
                ROUTINE_DETAIL_DEFAULT_TAG_LABEL,
                ROUTINE_DETAIL_DEFAULT_UPDATE,
                "$ROUTINE_DETAIL_DEFAULT_RECORD_DATE_PREFIX${routineDetailDisplayText(LocalDate(2026, 3, 20))}",
                ROUTINE_DETAIL_DEFAULT_RECORD_AMOUNT_LABEL,
                ROUTINE_DETAIL_DEFAULT_RECORD_ADD,
                "$ROUTINE_DETAIL_DEFAULT_RECORD_REMOVE_PREFIX$date 250ml",
            )
        composeRule.onNode(hasTestTag(ROUTINE_CARD_TEST_TAG)).assertDoesNotExist()
        composeRule.onNode(hasTestTag(ROUTINE_PROGRESS_BAR_TEST_TAG)).assertDoesNotExist()
        composeRule.routineDetailVisibleTextList().none { text -> text.contains("%") } shouldBe true
    }

    @Test
    fun `TC-a3384a86-8e87-41c2-8068-b39c68afed9e 시작 날짜 고르기는 시작 날짜에서 시작한다`() {
        setRuleScreen()

        composeRule.routineDetailOpenDatePicker(RULE_START_DATE)

        composeRule.routineDetailPickerSelection(RULE_START_DATE).assertExists()
    }

    @Test
    fun `TC-a3384a86-8e87-41c2-8068-b39c68afed9e 시작 날짜를 고르고 확인하면 시작 날짜가 바뀐다`() {
        setRuleScreen()

        composeRule.routineDetailPickDate(from = RULE_START_DATE, to = LocalDate(2026, 2, 1))

        composeRule.routineDetailDateButton(LocalDate(2026, 2, 1)).assertExists()
        composeRule.routineDetailDateButton(RULE_START_DATE).assertDoesNotExist()
    }

    @Test
    fun `TC-a3384a86-8e87-41c2-8068-b39c68afed9e 시작 날짜를 고르고 취소하면 시작 날짜가 그대로다`() {
        setRuleScreen()

        composeRule.routineDetailOpenDatePicker(RULE_START_DATE)
        composeRule.routineDetailTypePickerDate(LocalDate(2026, 2, 1))
        composeRule.routineDetailCancelDatePicker()

        composeRule.routineDetailDateButton(RULE_START_DATE).assertExists()
        composeRule.routineDetailDateButton(LocalDate(2026, 2, 1)).assertDoesNotExist()
    }

    @Test
    fun `TC-a3384a86-8e87-41c2-8068-b39c68afed9e 주기를 매월로 고르면 주기만 바뀐다`() {
        setRuleScreen()

        composeRule.routineSelectCycle(option = ROUTINE_DETAIL_DEFAULT_CYCLE_MONTHLY)

        composeRule.routineDetailSelectedCycle() shouldBe ROUTINE_DETAIL_DEFAULT_CYCLE_MONTHLY
        composeRule.routineDetailInputtedUnit() shouldBe ROUTINE_DETAIL_UNIT_COUNT
        composeRule.routineDetailDateButton(RULE_START_DATE).assertExists()
        composeRule.routineDetailGoalInput().routineDetailEditableText() shouldBe "3"
        composeRule.routineDetailAmountPerRecordInput().routineDetailEditableText() shouldBe "1"
    }

    @Test
    fun `TC-a3384a86-8e87-41c2-8068-b39c68afed9e 주기를 평일만으로 고르면 주기만 바뀐다`() {
        assertOnlyCycleChanged(option = ROUTINE_FORM_DEFAULT_CYCLE_WEEKDAYS)
    }

    @Test
    fun `TC-a3384a86-8e87-41c2-8068-b39c68afed9e 주기를 주말만으로 고르면 주기만 바뀐다`() {
        assertOnlyCycleChanged(option = ROUTINE_FORM_DEFAULT_CYCLE_WEEKENDS)
    }

    private fun assertOnlyCycleChanged(option: String) {
        setRuleScreen()

        composeRule.routineSelectCycle(option = option)

        composeRule.routineDetailSelectedCycle() shouldBe option
        composeRule.routineDetailInputtedUnit() shouldBe ROUTINE_DETAIL_UNIT_COUNT
        composeRule.routineDetailDateButton(RULE_START_DATE).assertExists()
        composeRule.routineDetailGoalInput().routineDetailEditableText() shouldBe "3"
        composeRule.routineDetailAmountPerRecordInput().routineDetailEditableText() shouldBe "1"
    }

    @Test
    fun `TC-a3384a86-8e87-41c2-8068-b39c68afed9e 단위를 모두 지우면 단위만 비고 두 숫자는 그대로다`() {
        setRuleScreen()

        composeRule.routineUnitInput().performTextClearance()
        composeRule.waitForIdle()

        composeRule.routineDetailInputtedUnit() shouldBe ""
        composeRule.routineDetailGoalInput().routineDetailEditableText() shouldBe "3"
        composeRule.routineDetailAmountPerRecordInput().routineDetailEditableText() shouldBe "1"
    }

    @Test
    fun `TC-a3384a86-8e87-41c2-8068-b39c68afed9e 단위를 지우고 긴 글을 붙여 넣으면 앞 10자만 남는다`() {
        setRuleScreen()

        composeRule.routineUnitInput().performTextClearance()
        composeRule.routineUnitInput().performTextInput("kilometers per hour")
        composeRule.waitForIdle()

        composeRule.routineDetailInputtedUnit() shouldBe "kilometers"
    }

    @Test
    fun `TC-a3384a86-8e87-41c2-8068-b39c68afed9e 단위 끝에 줄바꿈을 입력해도 반영하지 않는다`() {
        setRuleScreen()

        composeRule.routineUnitInput().performTextInput("\n")
        composeRule.waitForIdle()

        composeRule.routineDetailInputtedUnit() shouldBe ROUTINE_DETAIL_UNIT_COUNT
    }

    @Test
    fun `TC-a3384a86-8e87-41c2-8068-b39c68afed9e 단위를 ml로 고쳐 적으면 단위만 바뀌고 두 숫자는 그대로다`() {
        setRuleScreen()

        composeRule.routineInputUnit(unit = ROUTINE_DETAIL_UNIT_MILLILITER)

        composeRule.routineDetailInputtedUnit() shouldBe ROUTINE_DETAIL_UNIT_MILLILITER
        composeRule.routineDetailSelectedCycle() shouldBe ROUTINE_DETAIL_DEFAULT_CYCLE_DAILY
        composeRule.routineDetailGoalInput().routineDetailEditableText() shouldBe "3"
        composeRule.routineDetailAmountPerRecordInput().routineDetailEditableText() shouldBe "1"
    }

    @Test
    fun `TC-a3384a86-8e87-41c2-8068-b39c68afed9e 목표량 끝에 a를 입력해도 반영하지 않는다`() {
        setRuleScreen()

        composeRule.routineDetailGoalInput().performTextInput("a")
        composeRule.waitForIdle()

        composeRule.routineDetailGoalInput().routineDetailEditableText() shouldBe "3"
    }

    @Test
    fun `TC-a3384a86-8e87-41c2-8068-b39c68afed9e 목표량을 모두 지우면 비어 있다`() {
        setRuleScreen()

        composeRule.routineDetailGoalInput().performTextClearance()
        composeRule.waitForIdle()

        composeRule.routineDetailGoalInput().routineDetailEditableText() shouldBe ""
    }

    @Test
    fun `TC-a3384a86-8e87-41c2-8068-b39c68afed9e 한 번에 기록할 양에 아홉 자리를 붙여 넣으면 일곱 자리만 남는다`() {
        setRuleScreen()

        composeRule.routineDetailAmountPerRecordInput().performTextClearance()
        composeRule.waitForIdle()
        composeRule.routineDetailAmountPerRecordInput().performTextInput("123456789")
        composeRule.waitForIdle()

        composeRule.routineDetailAmountPerRecordInput().routineDetailEditableText() shouldBe "1234567"
    }

    @Test
    fun `TC-a3384a86-8e87-41c2-8068-b39c68afed9e 한 번에 기록할 양을 모두 지우면 비어 있다`() {
        setRuleScreen()

        composeRule.routineDetailAmountPerRecordInput().performTextClearance()
        composeRule.waitForIdle()

        composeRule.routineDetailAmountPerRecordInput().routineDetailEditableText() shouldBe ""
    }

    private fun setRuleScreen() {
        setScreen(
            RoutineDetailTestEnvironment(
                detail =
                    routineDetailOf(
                        startDate = RULE_START_DATE,
                        cycle = RoutineCycle.DAILY,
                        unit = "회",
                        goalAmount = 3,
                        amountPerRecord = 1,
                    ),
            ),
        )
    }

    @Test
    fun `TC-d587ceb7-35b0-4e7f-b221-a8407127e59e 입력을 바꾸지 않으면 수정 동작이 없다`() {
        assertUpdateAction(isProvided = false) {}
    }

    @Test
    fun `TC-d587ceb7-35b0-4e7f-b221-a8407127e59e 제목 뒤에 공백을 붙이면 수정 동작이 있다`() {
        assertUpdateAction(isProvided = true) { composeRule.routineDetailReplaceText(composeRule.routineDetailTitleInput(), "$ROUTINE_DETAIL_WATER ") }
    }

    @Test
    fun `TC-d587ceb7-35b0-4e7f-b221-a8407127e59e 설명에 줄바꿈을 붙이면 수정 동작이 있다`() {
        assertUpdateAction(isProvided = true) { composeRule.routineDetailReplaceText(composeRule.routineDetailDescriptionInput(), "$ROUTINE_DETAIL_DESCRIPTION\n") }
    }

    @Test
    fun `TC-d587ceb7-35b0-4e7f-b221-a8407127e59e 컬러를 바꾸면 수정 동작이 있다`() {
        assertUpdateAction(isProvided = true) { environment ->
            composeRule.routineDetailChangeColor(
                fromHex = environment.detail.color.routineDetailColorHex(),
                toHex = routineDetailOtherColor(environment.detail.color).routineDetailColorHex(),
            )
        }
    }

    @Test
    fun `TC-d587ceb7-35b0-4e7f-b221-a8407127e59e 시작 날짜를 바꾸면 수정 동작이 있다`() {
        assertUpdateAction(isProvided = true) { composeRule.routineDetailPickDate(from = LocalDate(2026, 3, 1), to = LocalDate(2026, 3, 2)) }
    }

    @Test
    fun `TC-d587ceb7-35b0-4e7f-b221-a8407127e59e 주기를 매주로 바꾸면 수정 동작이 있다`() {
        assertUpdateAction(isProvided = true) {
            composeRule.routineSelectCycle(option = ROUTINE_DETAIL_DEFAULT_CYCLE_WEEKLY)
        }
    }

    @Test
    fun `TC-d587ceb7-35b0-4e7f-b221-a8407127e59e 주기를 평일만으로 바꾸면 수정 동작이 있다`() {
        assertUpdateAction(isProvided = true) {
            composeRule.routineSelectCycle(option = ROUTINE_FORM_DEFAULT_CYCLE_WEEKDAYS)
        }
    }

    @Test
    fun `TC-d587ceb7-35b0-4e7f-b221-a8407127e59e 주기를 주말만으로 바꾸면 수정 동작이 있다`() {
        assertUpdateAction(isProvided = true) {
            composeRule.routineSelectCycle(option = ROUTINE_FORM_DEFAULT_CYCLE_WEEKENDS)
        }
    }

    @Test
    fun `TC-fb67e456-cdd6-4762-93cb-32a38f2dba53 단위 앞에 공백을 더하면 수정 동작이 없다`() {
        assertUnitUpdateAction(storedUnit = "ml", isProvided = false) { composeRule.routineInputUnit(unit = " ml") }
    }

    @Test
    fun `TC-fb67e456-cdd6-4762-93cb-32a38f2dba53 단위 뒤에 공백 두 글자를 더하면 수정 동작이 없다`() {
        assertUnitUpdateAction(storedUnit = "ml", isProvided = false) { composeRule.routineInputUnit(unit = "ml  ") }
    }

    @Test
    fun `TC-fb67e456-cdd6-4762-93cb-32a38f2dba53 단위의 대소문자를 바꾸면 수정 동작이 있다`() {
        assertUnitUpdateAction(storedUnit = "ml", isProvided = true) { composeRule.routineInputUnit(unit = "ML") }
    }

    @Test
    fun `TC-fb67e456-cdd6-4762-93cb-32a38f2dba53 단위의 글자 사이에 공백을 넣으면 수정 동작이 있다`() {
        assertUnitUpdateAction(storedUnit = "ml", isProvided = true) { composeRule.routineInputUnit(unit = "m l") }
    }

    @Test
    fun `TC-fb67e456-cdd6-4762-93cb-32a38f2dba53 단위를 모두 지우면 수정 동작이 있다`() {
        assertUnitUpdateAction(storedUnit = "ml", isProvided = true) { composeRule.routineUnitInput().performTextClearance() }
    }

    @Test
    fun `TC-fb67e456-cdd6-4762-93cb-32a38f2dba53 단위를 바꿨다가 되돌리면 수정 동작이 없다`() {
        assertUnitUpdateAction(storedUnit = "ml", isProvided = false) {
            composeRule.routineInputUnit(unit = "L")
            composeRule.onNode(routineDetailUpdateButton()).assertExists()
            composeRule.routineInputUnit(unit = "ml")
        }
    }

    @Test
    fun `TC-fb67e456-cdd6-4762-93cb-32a38f2dba53 빈 단위에 공백만 입력하면 수정 동작이 없다`() {
        assertUnitUpdateAction(storedUnit = "", isProvided = false) { composeRule.routineInputUnit(unit = "   ") }
    }

    @Test
    fun `TC-fb67e456-cdd6-4762-93cb-32a38f2dba53 빈 단위에 글자를 입력하면 수정 동작이 있다`() {
        assertUnitUpdateAction(storedUnit = "", isProvided = true) { composeRule.routineInputUnit(unit = "회") }
    }

    private fun assertUnitUpdateAction(
        storedUnit: String,
        isProvided: Boolean,
        change: () -> Unit,
    ) {
        setScreen(recordingEnvironment(detail = routineDetailOf(cycle = RoutineCycle.DAILY, unit = storedUnit)))
        composeRule.onNode(routineDetailUpdateButton()).assertDoesNotExist()

        change()
        composeRule.waitForIdle()

        if (isProvided) {
            composeRule.onNode(routineDetailUpdateButton()).assertExists()
        } else {
            composeRule.onNode(routineDetailUpdateButton()).assertDoesNotExist()
        }
    }

    @Test
    fun `TC-d587ceb7-35b0-4e7f-b221-a8407127e59e 단위를 m로 바꾸면 수정 동작이 있다`() {
        assertUpdateAction(isProvided = true) {
            composeRule.routineInputUnit(unit = ROUTINE_DETAIL_UNIT_METER)
        }
    }

    @Test
    fun `TC-d587ceb7-35b0-4e7f-b221-a8407127e59e 목표량을 2500으로 바꾸면 수정 동작이 있다`() {
        assertUpdateAction(isProvided = true) { composeRule.routineDetailReplaceText(composeRule.routineDetailGoalInput(), "2500") }
    }

    @Test
    fun `TC-d587ceb7-35b0-4e7f-b221-a8407127e59e 목표량을 모두 지우면 수정 동작이 있다`() {
        assertUpdateAction(isProvided = true) { composeRule.routineDetailGoalInput().performTextClearance() }
    }

    @Test
    fun `TC-d587ceb7-35b0-4e7f-b221-a8407127e59e 한 번에 기록할 양을 300으로 바꾸면 수정 동작이 있다`() {
        assertUpdateAction(isProvided = true) { composeRule.routineDetailReplaceText(composeRule.routineDetailAmountPerRecordInput(), "300") }
    }

    @Test
    fun `TC-d587ceb7-35b0-4e7f-b221-a8407127e59e 제목을 바꿨다가 되돌리면 수정 동작이 없다`() {
        assertUpdateAction(isProvided = false) {
            composeRule.routineDetailReplaceText(composeRule.routineDetailTitleInput(), ROUTINE_DETAIL_WATER_TWO_LITER)
            composeRule.onNode(routineDetailUpdateButton()).assertExists()
            composeRule.routineDetailReplaceText(composeRule.routineDetailTitleInput(), ROUTINE_DETAIL_WATER)
        }
    }

    @Test
    fun `TC-d587ceb7-35b0-4e7f-b221-a8407127e59e 주기를 매주로 바꿨다가 매일로 되돌리면 수정 동작이 없다`() {
        assertUpdateAction(isProvided = false) {
            composeRule.routineSelectCycle(option = ROUTINE_DETAIL_DEFAULT_CYCLE_WEEKLY)
            composeRule.onNode(routineDetailUpdateButton()).assertExists()
            composeRule.routineSelectCycle(option = ROUTINE_DETAIL_DEFAULT_CYCLE_DAILY)
        }
    }

    @Test
    fun `TC-d587ceb7-35b0-4e7f-b221-a8407127e59e 목표량을 02000으로 바꾸면 수정 동작이 없다`() {
        assertUpdateAction(isProvided = false) { composeRule.routineDetailReplaceText(composeRule.routineDetailGoalInput(), "02000") }
    }

    @Test
    fun `TC-d587ceb7-35b0-4e7f-b221-a8407127e59e 한 번에 기록할 양을 0250으로 바꾸면 수정 동작이 없다`() {
        assertUpdateAction(isProvided = false) { composeRule.routineDetailReplaceText(composeRule.routineDetailAmountPerRecordInput(), "0250") }
    }

    @Test
    fun `TC-d587ceb7-35b0-4e7f-b221-a8407127e59e 목표량을 0으로 바꾸면 수정 동작이 있다`() {
        assertUpdateAction(isProvided = true) { composeRule.routineDetailReplaceText(composeRule.routineDetailGoalInput(), "0") }
    }

    @Test
    fun `TC-d587ceb7-35b0-4e7f-b221-a8407127e59e 한 번에 기록할 양을 모두 지우면 수정 동작이 있다`() {
        assertUpdateAction(isProvided = true) { composeRule.routineDetailAmountPerRecordInput().performTextClearance() }
    }

    @Test
    fun `TC-d587ceb7-35b0-4e7f-b221-a8407127e59e 한 번에 기록할 양을 0으로 바꾸면 수정 동작이 있다`() {
        assertUpdateAction(isProvided = true) { composeRule.routineDetailReplaceText(composeRule.routineDetailAmountPerRecordInput(), "0") }
    }

    @Test
    fun `TC-d587ceb7-35b0-4e7f-b221-a8407127e59e 기록할 날짜를 다른 날짜로 골라도 수정 동작이 없다`() {
        assertUpdateAction(isProvided = false) {
            val before = composeRule.routineDetailRecordDateText()
            val target = listOf(LocalDate(2026, 3, 2), LocalDate(2026, 3, 3)).first { date -> routineDetailDisplayText(date) != before }

            composeRule.routineDetailOpenRecordDatePicker()
            composeRule.routineDetailTypePickerDate(target)
            composeRule.routineDetailConfirmDatePicker()

            composeRule.routineDetailRecordDateText() shouldBe routineDetailDisplayText(target)
        }
    }

    @Test
    fun `TC-d587ceb7-35b0-4e7f-b221-a8407127e59e 기록의 양을 500으로 바꿔도 수정 동작이 없다`() {
        assertUpdateAction(isProvided = false) { composeRule.routineDetailReplaceText(composeRule.routineDetailRecordAmountInput(), "500") }
    }

    @Test
    fun `TC-d587ceb7-35b0-4e7f-b221-a8407127e59e 기록을 넣어도 수정 동작이 없다`() {
        assertUpdateAction(isProvided = false) { environment ->
            composeRule.routineDetailClickRecordAdd()

            verify(exactly = 1) { environment.recordViewModel.add(date = any(), amount = 250) }
            composeRule.routineDetailRecordRowList().size shouldBe 2
        }
    }

    @Test
    fun `TC-d587ceb7-35b0-4e7f-b221-a8407127e59e 기록을 빼도 수정 동작이 없다`() {
        assertUpdateAction(isProvided = false) { environment ->
            composeRule.onNode(routineDetailRecordRemoveButton(date = LocalDate(2026, 3, 5), amountText = AMOUNT_250_ML_TEXT)).performSemanticsAction(SemanticsActions.OnClick)
            composeRule.waitForIdle()

            verify(exactly = 1) { environment.recordViewModel.delete(recordId = any()) }
            composeRule.routineDetailRecordRowList().shouldBeEmpty()
        }
    }

    private fun assertUpdateAction(
        isProvided: Boolean,
        change: (RoutineDetailTestEnvironment) -> Unit,
    ) {
        val environment = recordingEnvironment(detail = routineDetailOf(cycle = RoutineCycle.DAILY))
        setScreen(environment)
        composeRule.onNode(routineDetailUpdateButton()).assertDoesNotExist()

        change(environment)
        composeRule.waitForIdle()

        if (isProvided) {
            composeRule.onNode(routineDetailUpdateButton()).assertExists()
        } else {
            composeRule.onNode(routineDetailUpdateButton()).assertDoesNotExist()
        }
    }

    @Test
    fun `TC-be1e3856-a0b4-4f31-b505-a173d6770206 저장된 목표량이 없고 바꾸지 않으면 수정 동작이 없다`() {
        assertGoalUpdateAction(isProvided = false) {}
    }

    @Test
    fun `TC-be1e3856-a0b4-4f31-b505-a173d6770206 목표량에 5를 입력했다가 모두 지우면 수정 동작이 없다`() {
        assertGoalUpdateAction(isProvided = false) {
            composeRule.routineDetailReplaceText(composeRule.routineDetailGoalInput(), "5")
            composeRule.onNode(routineDetailUpdateButton()).assertExists()
            composeRule.routineDetailGoalInput().performTextClearance()
        }
    }

    @Test
    fun `TC-be1e3856-a0b4-4f31-b505-a173d6770206 목표량에 5를 입력하면 수정 동작이 있다`() {
        assertGoalUpdateAction(isProvided = true) { composeRule.routineDetailReplaceText(composeRule.routineDetailGoalInput(), "5") }
    }

    @Test
    fun `TC-be1e3856-a0b4-4f31-b505-a173d6770206 목표량에 0을 입력하면 수정 동작이 있다`() {
        assertGoalUpdateAction(isProvided = true) { composeRule.routineDetailReplaceText(composeRule.routineDetailGoalInput(), "0") }
    }

    private fun assertGoalUpdateAction(
        isProvided: Boolean,
        change: () -> Unit,
    ) {
        setScreen(RoutineDetailTestEnvironment(detail = routineDetailOf(goalAmount = null)))
        composeRule.routineDetailGoalInput().routineDetailEditableText() shouldBe ""
        composeRule.onNode(routineDetailUpdateButton()).assertDoesNotExist()

        change()
        composeRule.waitForIdle()

        if (isProvided) {
            composeRule.onNode(routineDetailUpdateButton()).assertExists()
        } else {
            composeRule.onNode(routineDetailUpdateButton()).assertDoesNotExist()
        }
    }

    @Test
    fun `TC-7f6e500d-57b9-4534-b922-e86e71d5f800 수정 동작이 제공된 채 기록을 넣거나 빼도 수정 동작은 사라지지 않는다`() {
        val environment = recordingEnvironment(detail = routineDetailOf())
        setScreen(environment)
        composeRule.routineDetailReplaceText(composeRule.routineDetailTitleInput(), ROUTINE_DETAIL_WATER_TWO_LITER)
        composeRule.onNode(routineDetailUpdateButton()).assertExists()

        composeRule.routineDetailClickRecordAdd()
        composeRule.routineDetailRecordRowList().size shouldBe 2
        composeRule.onNode(routineDetailUpdateButton()).assertExists()

        composeRule.onNode(routineDetailRecordRemoveButton(date = LocalDate(2026, 3, 5), amountText = AMOUNT_250_ML_TEXT)).performSemanticsAction(SemanticsActions.OnClick)
        composeRule.waitForIdle()

        composeRule.routineDetailRecordRowList().size shouldBe 1
        composeRule.onNode(routineDetailUpdateButton()).assertExists()
        composeRule.routineDetailTitleInput().routineDetailEditableText() shouldBe ROUTINE_DETAIL_WATER_TWO_LITER
    }

    @Test
    fun `TC-00b425f1-19bc-46da-ae94-8d1dff94c926 수정에 성공하면 화면과 입력을 유지하고 성공 피드백을 제공한다`() {
        val environment =
            RoutineDetailTestEnvironment(
                detail = routineDetailOf(cycle = RoutineCycle.DAILY),
                recordList = listOf(record(date = LocalDate(2026, 3, 5), amount = 250)),
            )
        setScreen(environment)
        composeRule.routineDetailReplaceText(composeRule.routineDetailTitleInput(), ROUTINE_DETAIL_WATER_TWO_LITER)
        composeRule.routineDetailReplaceText(composeRule.routineDetailGoalInput(), "02500")
        composeRule.routineSelectCycle(option = ROUTINE_DETAIL_DEFAULT_CYCLE_WEEKLY)
        composeRule.routineDetailReplaceText(composeRule.routineDetailRecordAmountInput(), "500")
        val updated = environment.detail.copy(title = ROUTINE_DETAIL_WATER_TWO_LITER, goalAmount = 2500, cycle = RoutineCycle.WEEKLY)

        composeRule.routineDetailClickUpdate()
        environment.effect.trySend(RoutineDetailEffect.UpdateSucceeded)
        environment.uiState.value = environment.content(detail = updated)
        composeRule.waitForIdle()

        verify(exactly = 1) { environment.routineViewModel.update(input = updated.routineDetailInput()) }
        composeRule.onNodeWithText(ROUTINE_DETAIL_DEFAULT_UPDATE_SUCCEEDED).assertExists()
        composeRule.onNode(routineDetailTopBarTitle(ROUTINE_DETAIL_WATER_TWO_LITER), useUnmergedTree = true).assertExists()
        composeRule.routineDetailTitleInput().routineDetailEditableText() shouldBe ROUTINE_DETAIL_WATER_TWO_LITER
        composeRule.routineDetailGoalInput().routineDetailEditableText() shouldBe "02500"
        composeRule.routineDetailSelectedCycle() shouldBe ROUTINE_DETAIL_DEFAULT_CYCLE_WEEKLY
        composeRule.routineDetailRecordAmountInput().routineDetailEditableText() shouldBe "500"
        composeRule.routineDetailRecordRowList() shouldBe listOf(routineDetailDisplayText(LocalDate(2026, 3, 5)) to AMOUNT_250_ML_TEXT)
        environment.navigateUpCount shouldBe 0
    }

    @Test
    @Config(qualifiers = "ko-w480dp-h3000dp")
    fun `TC-00b425f1-19bc-46da-ae94-8d1dff94c926 한국어 환경에서 수정 성공 피드백을 제공한다`() {
        val environment = RoutineDetailTestEnvironment()
        setScreen(environment)
        composeRule.routineDetailReplaceText(composeRule.routineDetailTitleInput(), ROUTINE_DETAIL_WATER_TWO_LITER)

        composeRule.onNodeWithContentDescription(ROUTINE_DETAIL_KOREAN_UPDATE).performClick()
        environment.effect.trySend(RoutineDetailEffect.UpdateSucceeded)
        composeRule.waitForIdle()

        composeRule.onNodeWithText(ROUTINE_DETAIL_KOREAN_UPDATE_SUCCEEDED).assertExists()
    }

    @Test
    fun `TC-e3c3280d-7eb8-4450-929e-739edfa80a1a 수정을 처리하는 동안 진행 상태를 표시한다`() {
        val environment = RoutineDetailTestEnvironment()
        environment.uiState.value = environment.content(isUpdateInProgress = true)
        setScreen(environment)
        composeRule.routineDetailReplaceText(composeRule.routineDetailTitleInput(), ROUTINE_DETAIL_WATER_TWO_LITER)

        composeRule.onNode(routineDetailUpdateButton() and hasRoutineDetailProgress(), useUnmergedTree = true).assertExists()

        environment.uiState.value = environment.content()
        composeRule.waitForIdle()
        composeRule.onNode(routineDetailUpdateButton() and hasRoutineDetailProgress(), useUnmergedTree = true).assertDoesNotExist()
    }

    @Test
    fun `TC-96e859a5-ce40-4b61-9c1d-3b568f637051 수정에 실패하면 안내 없이 입력을 유지하고 다시 시도할 수 있다`() {
        val environment = RoutineDetailTestEnvironment()
        setScreen(environment)
        composeRule.routineDetailReplaceText(composeRule.routineDetailTitleInput(), ROUTINE_DETAIL_WATER_TWO_LITER)
        composeRule.routineDetailReplaceText(composeRule.routineDetailGoalInput(), "2500")

        composeRule.routineDetailClickUpdate()
        composeRule.routineDetailClickUpdate()

        verify(exactly = 2) { environment.routineViewModel.update(input = any()) }
        composeRule.routineDetailTitleInput().routineDetailEditableText() shouldBe ROUTINE_DETAIL_WATER_TWO_LITER
        composeRule.routineDetailGoalInput().routineDetailEditableText() shouldBe "2500"
        composeRule.onNode(routineDetailUpdateButton() and hasRoutineDetailProgress(), useUnmergedTree = true).assertDoesNotExist()
        assertNoFeedback()
    }

    @Test
    fun `TC-44303e74-098e-47f2-a3a7-8a1a452c3cbc 제목을 비운 채 수정에 성공하면 화면 제목과 비운 입력, 수정 동작이 그대로다`() {
        assertBlankTitleUpdate(title = "")
    }

    @Test
    fun `TC-44303e74-098e-47f2-a3a7-8a1a452c3cbc 제목을 공백뿐으로 둔 채 수정에 성공하면 화면 제목과 입력, 수정 동작이 그대로다`() {
        assertBlankTitleUpdate(title = "   ")
    }

    private fun assertBlankTitleUpdate(title: String) {
        val environment = RoutineDetailTestEnvironment()
        // 제목을 비운 수정은 저장된 제목을 유지한 채 나머지 정보만 저장한다.
        val updated = environment.detail.copy(description = UPDATED_DESCRIPTION, goalAmount = 3000)
        setScreen(environment)
        composeRule.routineDetailReplaceText(composeRule.routineDetailTitleInput(), title)
        focusDescription()
        composeRule.routineDetailReplaceText(composeRule.routineDetailDescriptionInput(), UPDATED_DESCRIPTION)
        composeRule.routineDetailReplaceText(composeRule.routineDetailGoalInput(), "3000")

        composeRule.routineDetailClickUpdate()
        environment.effect.trySend(RoutineDetailEffect.UpdateSucceeded)
        environment.uiState.value = environment.content(detail = updated)
        composeRule.waitForIdle()

        verify(exactly = 1) { environment.routineViewModel.update(input = updated.routineDetailInput().copy(title = title)) }
        composeRule.onNode(routineDetailTopBarTitle(ROUTINE_DETAIL_WATER), useUnmergedTree = true).assertExists()
        composeRule.routineDetailTitleInput().routineDetailEditableText() shouldBe title
        composeRule.routineDetailDescriptionInput().routineDetailEditableText() shouldBe UPDATED_DESCRIPTION
        composeRule.routineDetailGoalInput().routineDetailEditableText() shouldBe "3000"
        composeRule.onNode(routineDetailUpdateButton()).assertExists()
    }

    @Test
    fun `TC-e33b1117-e485-4dce-9bc4-3608b805442d 시작 날짜를 바꿔 수정해도 벗어난 기록이 그대로 보인다`() {
        assertRecordsAfterUpdate(
            change = { composeRule.routineDetailPickDate(from = LocalDate(2026, 3, 1), to = LocalDate(2026, 3, 10)) },
            updated = { detail -> detail.copy(startDate = LocalDate(2026, 3, 10)) },
            expectedAmountList = listOf(AMOUNT_500_ML_TEXT, AMOUNT_250_ML_TEXT),
        )
    }

    @Test
    fun `TC-e33b1117-e485-4dce-9bc4-3608b805442d 단위를 회로 바꿔 수정하면 기록의 양이 회로 보인다`() {
        assertRecordsAfterUpdate(
            change = { composeRule.routineInputUnit(unit = ROUTINE_DETAIL_UNIT_COUNT) },
            updated = { detail -> detail.copy(unit = "회") },
            expectedAmountList = listOf("500회", "250회"),
        )
    }

    @Test
    fun `TC-e33b1117-e485-4dce-9bc4-3608b805442d 단위를 비워 수정하면 기록의 양이 숫자만 보인다`() {
        assertRecordsAfterUpdate(
            change = {
                composeRule.routineUnitInput().performTextClearance()
                composeRule.waitForIdle()
            },
            updated = { detail -> detail.copy(unit = "") },
            expectedAmountList = listOf("500", "250"),
        )
    }

    @Test
    fun `TC-d607a91a-5717-4a72-8187-7450991c6f5c 주기를 평일만으로 바꿔 수정해도 토요일의 기록이 그대로 보인다`() {
        assertRecordsAfterCycleUpdate(option = ROUTINE_FORM_DEFAULT_CYCLE_WEEKDAYS, cycle = RoutineCycle.WEEKDAYS)
    }

    @Test
    fun `TC-d607a91a-5717-4a72-8187-7450991c6f5c 주기를 주말만으로 바꿔 수정해도 월요일의 기록이 그대로 보인다`() {
        assertRecordsAfterCycleUpdate(option = ROUTINE_FORM_DEFAULT_CYCLE_WEEKENDS, cycle = RoutineCycle.WEEKENDS)
    }

    // 2026-03-07은 토요일, 2026-03-09는 월요일이다.
    private fun assertRecordsAfterCycleUpdate(
        option: String,
        cycle: RoutineCycle,
    ) {
        val rowList =
            listOf(
                routineDetailDisplayText(LocalDate(2026, 3, 9)) to "200ml",
                routineDetailDisplayText(LocalDate(2026, 3, 7)) to "300ml",
            )
        val environment =
            RoutineDetailTestEnvironment(
                detail = routineDetailOf(startDate = LocalDate(2026, 3, 1), cycle = RoutineCycle.DAILY, unit = "ml"),
                recordList =
                    listOf(
                        record(date = LocalDate(2026, 3, 9), amount = 200),
                        record(date = LocalDate(2026, 3, 7), amount = 300),
                    ),
            )
        val updatedDetail = environment.detail.copy(cycle = cycle)
        every { environment.routineViewModel.update(input = any()) } answers {
            environment.effect.trySend(RoutineDetailEffect.UpdateSucceeded)
            environment.uiState.value = environment.content(detail = updatedDetail)
        }
        setScreen(environment)
        composeRule.routineDetailRecordRowList() shouldBe rowList

        composeRule.routineSelectCycle(option = option)
        composeRule.routineDetailClickUpdate()

        verify(exactly = 1) { environment.routineViewModel.update(input = updatedDetail.routineDetailInput()) }
        composeRule.routineDetailRecordRowList() shouldBe rowList
    }

    @Test
    fun `TC-e33b1117-e485-4dce-9bc4-3608b805442d 주기와 목표량을 바꿔 수정해도 기록이 그대로 보인다`() {
        assertRecordsAfterUpdate(
            change = {
                composeRule.routineSelectCycle(option = ROUTINE_DETAIL_DEFAULT_CYCLE_MONTHLY)
                composeRule.routineDetailReplaceText(composeRule.routineDetailGoalInput(), "9000")
            },
            updated = { detail -> detail.copy(cycle = RoutineCycle.MONTHLY, goalAmount = 9000) },
            expectedAmountList = listOf(AMOUNT_500_ML_TEXT, AMOUNT_250_ML_TEXT),
        )
    }

    private fun assertRecordsAfterUpdate(
        change: () -> Unit,
        updated: (RoutineDetail) -> RoutineDetail,
        expectedAmountList: List<String>,
    ) {
        val dateTextList = listOf(routineDetailDisplayText(LocalDate(2026, 3, 20)), routineDetailDisplayText(LocalDate(2026, 3, 5)))
        val environment =
            RoutineDetailTestEnvironment(
                detail = routineDetailOf(cycle = RoutineCycle.DAILY),
                recordList =
                    listOf(
                        record(date = LocalDate(2026, 3, 20), amount = 500),
                        record(date = LocalDate(2026, 3, 5), amount = 250),
                    ),
            )
        val updatedDetail = updated(environment.detail)
        every { environment.routineViewModel.update(input = any()) } answers {
            environment.effect.trySend(RoutineDetailEffect.UpdateSucceeded)
            environment.uiState.value = environment.content(detail = updatedDetail)
        }
        setScreen(environment)
        composeRule.routineDetailRecordRowList() shouldBe dateTextList.zip(listOf(AMOUNT_500_ML_TEXT, AMOUNT_250_ML_TEXT))

        change()
        composeRule.routineDetailClickUpdate()

        verify(exactly = 1) { environment.routineViewModel.update(input = updatedDetail.routineDetailInput()) }
        composeRule.routineDetailRecordRowList() shouldBe dateTextList.zip(expectedAmountList)
    }

    @Test
    fun `TC-1f80875b-6ba8-4b49-a740-e89ff4114c08 목표량이 0인 채 수정하면 수정하지 않고 목표량 입력을 알린다`() {
        assertGoalInvalid(goal = "0")
    }

    @Test
    fun `TC-1f80875b-6ba8-4b49-a740-e89ff4114c08 목표량이 000인 채 수정하면 수정하지 않고 목표량 입력을 알린다`() {
        assertGoalInvalid(goal = "000")
    }

    private fun assertGoalInvalid(goal: String) {
        val environment = RoutineDetailTestEnvironment()
        val input = slot<RoutineInput>()
        every { environment.routineViewModel.update(input = capture(input)) } answers { environment.effect.trySend(RoutineDetailEffect.GoalAmountInvalid) }
        setScreen(environment)
        composeRule.routineDetailReplaceText(composeRule.routineDetailTitleInput(), ROUTINE_DETAIL_WATER_TWO_LITER)
        composeRule.routineDetailReplaceText(composeRule.routineDetailGoalInput(), goal)
        focusDescription()

        composeRule.routineDetailClickUpdate()

        // 목표량 0은 유효 범위 밖이라 수정이 성립하지 않는다.
        input.captured.goalAmount shouldBe 0
        composeRule.onNodeWithText(ROUTINE_DETAIL_DEFAULT_GOAL_INVALID).assertExists()
        composeRule.routineDetailGoalInput().assertIsFocused()
        composeRule.routineDetailTitleInput().routineDetailEditableText() shouldBe ROUTINE_DETAIL_WATER_TWO_LITER
        composeRule.routineDetailGoalInput().routineDetailEditableText() shouldBe goal
        composeRule.onNodeWithText(ROUTINE_DETAIL_DEFAULT_UPDATE_SUCCEEDED).assertDoesNotExist()
        composeRule.onNode(routineDetailTopBarTitle(ROUTINE_DETAIL_WATER), useUnmergedTree = true).assertExists()
    }

    @Test
    @Config(qualifiers = "ko-w480dp-h3000dp")
    fun `TC-1f80875b-6ba8-4b49-a740-e89ff4114c08 한국어 환경에서 목표량 입력을 알린다`() {
        val environment = RoutineDetailTestEnvironment()
        every { environment.routineViewModel.update(input = any()) } answers { environment.effect.trySend(RoutineDetailEffect.GoalAmountInvalid) }
        setScreen(environment)
        composeRule.routineDetailReplaceText(composeRule.routineDetailTitleInput(), ROUTINE_DETAIL_WATER_TWO_LITER)

        composeRule.onNodeWithContentDescription(ROUTINE_DETAIL_KOREAN_UPDATE).performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithText(ROUTINE_DETAIL_KOREAN_GOAL_INVALID).assertExists()
    }

    @Test
    fun `TC-62bb7013-b5d5-4f19-8f1b-8fb7dcf039d9 목표량을 비운 수정은 목표량 없이 요청되고 성공 피드백을 제공한다`() {
        val environment = RoutineDetailTestEnvironment()
        val updated = environment.detail.copy(goalAmount = null)
        every { environment.routineViewModel.update(input = any()) } answers {
            environment.effect.trySend(RoutineDetailEffect.UpdateSucceeded)
            environment.uiState.value = environment.content(detail = updated)
        }
        setScreen(environment)
        composeRule.routineDetailGoalInput().performTextClearance()
        composeRule.waitForIdle()

        composeRule.routineDetailClickUpdate()

        verify(exactly = 1) { environment.routineViewModel.update(input = updated.routineDetailInput()) }
        composeRule.onNodeWithText(ROUTINE_DETAIL_DEFAULT_UPDATE_SUCCEEDED).assertExists()
        composeRule.onNodeWithText(ROUTINE_DETAIL_DEFAULT_GOAL_INVALID).assertDoesNotExist()
        composeRule.routineDetailGoalInput().routineDetailEditableText() shouldBe ""
        composeRule.onNode(routineDetailUpdateButton()).assertDoesNotExist()
    }

    @Test
    fun `TC-9f029fe0-6e88-4954-890e-dcacc8f860f6 한 번에 기록할 양이 빈 채 수정하면 수정하지 않고 그 입력을 알린다`() {
        assertAmountPerRecordInvalid(amountPerRecord = "")
    }

    @Test
    fun `TC-9f029fe0-6e88-4954-890e-dcacc8f860f6 한 번에 기록할 양이 0인 채 수정하면 수정하지 않고 그 입력을 알린다`() {
        assertAmountPerRecordInvalid(amountPerRecord = "0")
    }

    @Test
    fun `TC-9f029fe0-6e88-4954-890e-dcacc8f860f6 한 번에 기록할 양이 00인 채 수정하면 수정하지 않고 그 입력을 알린다`() {
        assertAmountPerRecordInvalid(amountPerRecord = "00")
    }

    private fun assertAmountPerRecordInvalid(amountPerRecord: String) {
        val environment = RoutineDetailTestEnvironment()
        val input = slot<RoutineInput>()
        every { environment.routineViewModel.update(input = capture(input)) } answers { environment.effect.trySend(RoutineDetailEffect.AmountPerRecordInvalid) }
        setScreen(environment)
        composeRule.routineDetailReplaceText(composeRule.routineDetailTitleInput(), ROUTINE_DETAIL_WATER_TWO_LITER)
        composeRule.routineDetailReplaceText(composeRule.routineDetailAmountPerRecordInput(), amountPerRecord)
        focusDescription()

        composeRule.routineDetailClickUpdate()

        // 비어 있거나 0인 한 번에 기록할 양은 유효 범위 밖의 0으로 넘겨 수정이 성립하지 않는다.
        input.captured.amountPerRecord shouldBe 0
        composeRule.onNodeWithText(ROUTINE_DETAIL_DEFAULT_AMOUNT_PER_RECORD_INVALID).assertExists()
        composeRule.routineDetailAmountPerRecordInput().assertIsFocused()
        composeRule.routineDetailTitleInput().routineDetailEditableText() shouldBe ROUTINE_DETAIL_WATER_TWO_LITER
        composeRule.routineDetailAmountPerRecordInput().routineDetailEditableText() shouldBe amountPerRecord
        composeRule.onNodeWithText(ROUTINE_DETAIL_DEFAULT_UPDATE_SUCCEEDED).assertDoesNotExist()
        composeRule.onNode(routineDetailTopBarTitle(ROUTINE_DETAIL_WATER), useUnmergedTree = true).assertExists()
    }

    @Test
    @Config(qualifiers = "ko-w480dp-h3000dp")
    fun `TC-9f029fe0-6e88-4954-890e-dcacc8f860f6 한국어 환경에서 한 번에 기록할 양 입력을 알린다`() {
        val environment = RoutineDetailTestEnvironment()
        every { environment.routineViewModel.update(input = any()) } answers { environment.effect.trySend(RoutineDetailEffect.AmountPerRecordInvalid) }
        setScreen(environment)
        composeRule.routineDetailReplaceText(composeRule.routineDetailTitleInput(), ROUTINE_DETAIL_WATER_TWO_LITER)

        composeRule.onNodeWithContentDescription(ROUTINE_DETAIL_KOREAN_UPDATE).performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithText(ROUTINE_DETAIL_KOREAN_AMOUNT_PER_RECORD_INVALID).assertExists()
    }

    @Test
    fun `TC-bb687a80-1806-4e35-a60a-950847ffc563 목표량이 0이고 한 번에 기록할 양이 비어 있으면 목표량 미입력만 알린다`() {
        assertFirstInvalidOnly(title = ROUTINE_DETAIL_WATER, goalAmount = "0", amountPerRecord = "", isGoalInvalid = true)
    }

    @Test
    fun `TC-bb687a80-1806-4e35-a60a-950847ffc563 목표량과 한 번에 기록할 양이 모두 0이면 목표량 미입력만 알린다`() {
        assertFirstInvalidOnly(title = ROUTINE_DETAIL_WATER, goalAmount = "0", amountPerRecord = "0", isGoalInvalid = true)
    }

    @Test
    fun `TC-bb687a80-1806-4e35-a60a-950847ffc563 제목을 비우고 목표량이 0이면 목표량 미입력만 알린다`() {
        assertFirstInvalidOnly(title = "", goalAmount = "0", amountPerRecord = "250", isGoalInvalid = true)
    }

    @Test
    fun `TC-bb687a80-1806-4e35-a60a-950847ffc563 제목과 한 번에 기록할 양을 비우면 한 번에 기록할 양 미입력만 알린다`() {
        assertFirstInvalidOnly(title = "", goalAmount = "2000", amountPerRecord = "", isGoalInvalid = false)
    }

    @Test
    fun `TC-bb687a80-1806-4e35-a60a-950847ffc563 제목을 비우고 두 숫자가 모두 0이면 목표량 미입력만 알린다`() {
        assertFirstInvalidOnly(title = "", goalAmount = "0", amountPerRecord = "0", isGoalInvalid = true)
    }

    private fun assertFirstInvalidOnly(
        title: String,
        goalAmount: String,
        amountPerRecord: String,
        isGoalInvalid: Boolean,
    ) {
        val environment = RoutineDetailTestEnvironment()
        val inputList = mutableListOf<RoutineInput>()
        val findRoutineUseCase = mockk<FindRoutineUseCase>()
        every { findRoutineUseCase(parameter = environment.id) } returns flowOf(Result.success(Arb.routine(isDeleted = false, detail = environment.detail).next().copy(id = environment.id)))
        val viewModel =
            RoutineDetailViewModel(
                id = environment.id,
                updateRoutineUseCase = recordingUpdateUseCase(inputList),
                finishRoutineUseCase = mockk(),
                restartRoutineUseCase = mockk(),
                deleteRoutineUseCase = mockk<DeleteRoutineUseCase>(),
                findRoutineUseCase = findRoutineUseCase,
            )
        composeRule.setContent { RoutineDetailTestScreen(environment = environment, routineViewModel = viewModel) }
        composeRule.waitForIdle()
        composeRule.routineDetailReplaceText(composeRule.routineDetailTitleInput(), title)
        composeRule.routineDetailReplaceText(composeRule.routineDetailGoalInput(), goalAmount)
        composeRule.routineDetailReplaceText(composeRule.routineDetailAmountPerRecordInput(), amountPerRecord)
        focusDescription()

        composeRule.routineDetailClickUpdate()

        inputList.size shouldBe 1
        if (isGoalInvalid) {
            composeRule.onNodeWithText(ROUTINE_DETAIL_DEFAULT_GOAL_INVALID).assertExists()
            composeRule.onNodeWithText(ROUTINE_DETAIL_DEFAULT_AMOUNT_PER_RECORD_INVALID).assertDoesNotExist()
            composeRule.routineDetailGoalInput().assertIsFocused()
        } else {
            composeRule.onNodeWithText(ROUTINE_DETAIL_DEFAULT_AMOUNT_PER_RECORD_INVALID).assertExists()
            composeRule.onNodeWithText(ROUTINE_DETAIL_DEFAULT_GOAL_INVALID).assertDoesNotExist()
            composeRule.routineDetailAmountPerRecordInput().assertIsFocused()
        }
        composeRule.onNodeWithText(ROUTINE_DETAIL_DEFAULT_UPDATE_SUCCEEDED).assertDoesNotExist()
        composeRule.onNode(routineDetailTopBarTitle(ROUTINE_DETAIL_WATER), useUnmergedTree = true).assertExists()
    }

    // 수정 성립 조건은 UpdateRoutineUseCase가 판정하므로, 받은 입력을 기록하고 같은 순서로 거절 사유를 돌려준다.
    private fun recordingUpdateUseCase(inputList: MutableList<RoutineInput>): UpdateRoutineUseCase {
        val useCase = mockk<UpdateRoutineUseCase>()
        coEvery { useCase(parameter = any()) } answers {
            val input = firstArg<UpdateRoutineUseCase.Parameter>().input
            val goalAmount = input.goalAmount
            inputList.add(input)
            when {
                goalAmount != null && goalAmount !in ROUTINE_AMOUNT_RANGE -> Result.failure(RoutineGoalAmountInvalidException())
                input.amountPerRecord !in ROUTINE_AMOUNT_RANGE -> Result.failure(RoutineAmountPerRecordInvalidException())
                else -> Result.success(1)
            }
        }
        return useCase
    }

    // 저장된 설명이 있으면 설명 입력은 미리보기로 열리므로, 입력 탭으로 바꾼 뒤 설명 입력에 초점을 둔다.
    private fun focusDescription() {
        composeRule.onNodeWithContentDescription(ROUTINE_DETAIL_DEFAULT_DESCRIPTION_INPUT_TAB).performClick()
        composeRule.waitForIdle()
        composeRule.routineDetailDescriptionInput().performSemanticsAction(SemanticsActions.RequestFocus)
        composeRule.waitForIdle()
        composeRule.routineDetailDescriptionInput().assertIsFocused()
    }

    @Test
    fun `TC-adadec35-debe-400d-bc82-7895002dc187 삭제를 선택하면 되묻지 않고 한 번 요청하고 성공하면 한 번 돌아간다`() {
        val environment = RoutineDetailTestEnvironment()
        every { environment.routineViewModel.delete() } answers { environment.effect.trySend(RoutineDetailEffect.DeleteSucceeded) }
        setScreen(environment)

        composeRule.routineDetailClickDelete()

        composeRule.onNode(isDialog()).assertDoesNotExist()
        verify(exactly = 1) { environment.routineViewModel.delete() }
        environment.navigateUpCount shouldBe 1
    }

    @Test
    fun `TC-fee4712b-18ab-4c63-b78e-02c062cacce9 삭제를 처리하는 동안 진행 상태를 표시하고 수정과 기록 넣기는 실행할 수 있다`() {
        val environment = RoutineDetailTestEnvironment()
        environment.uiState.value = environment.content(isDeleteInProgress = true)
        setScreen(environment)
        composeRule.routineDetailReplaceText(composeRule.routineDetailTitleInput(), ROUTINE_DETAIL_WATER_TWO_LITER)

        composeRule.onNode(routineDetailDeleteButton() and hasRoutineDetailProgress(), useUnmergedTree = true).assertExists()
        composeRule.routineDetailClickUpdate()
        composeRule.routineDetailClickRecordAdd()

        verify(exactly = 1) { environment.routineViewModel.update(input = any()) }
        verify(exactly = 1) { environment.recordViewModel.add(date = any(), amount = 250) }

        environment.uiState.value = environment.content()
        composeRule.waitForIdle()
        composeRule.onNode(routineDetailDeleteButton() and hasRoutineDetailProgress(), useUnmergedTree = true).assertDoesNotExist()
    }

    @Test
    fun `TC-69bd05c3-0cda-4460-b3e0-bb68e7ce106c 삭제에 실패하면 화면에 머무르고 입력을 유지하며 다시 삭제할 수 있다`() {
        val environment = RoutineDetailTestEnvironment()
        setScreen(environment)
        composeRule.routineDetailReplaceText(composeRule.routineDetailTitleInput(), ROUTINE_DETAIL_WATER_TWO_LITER)
        composeRule.routineDetailReplaceText(composeRule.routineDetailRecordAmountInput(), "500")

        composeRule.routineDetailClickDelete()
        composeRule.routineDetailClickDelete()

        verify(exactly = 2) { environment.routineViewModel.delete() }
        environment.navigateUpCount shouldBe 0
        composeRule.routineDetailTitleInput().routineDetailEditableText() shouldBe ROUTINE_DETAIL_WATER_TWO_LITER
        composeRule.routineDetailRecordAmountInput().routineDetailEditableText() shouldBe "500"
        composeRule.onNode(routineDetailDeleteButton() and hasRoutineDetailProgress(), useUnmergedTree = true).assertDoesNotExist()
        assertNoFeedback()
    }

    @Test
    fun `TC-556ffd48-fe0b-4afa-a1ed-8fd56c27b0be 저장된 내용이 다른 경로에서 바뀌면 화면 제목만 갱신한다`() {
        val environment = RoutineDetailTestEnvironment()
        setScreen(environment)
        composeRule.routineDetailReplaceText(composeRule.routineDetailTitleInput(), MY_TITLE)
        composeRule.routineDetailReplaceText(composeRule.routineDetailGoalInput(), "3000")
        composeRule.routineDetailReplaceText(composeRule.routineDetailRecordAmountInput(), "500")

        environment.uiState.value = environment.content(detail = environment.detail.copy(title = OTHER_TITLE, goalAmount = 5000, amountPerRecord = 300))
        composeRule.waitForIdle()

        composeRule.onNode(routineDetailTopBarTitle(OTHER_TITLE), useUnmergedTree = true).assertExists()
        composeRule.routineDetailTitleInput().routineDetailEditableText() shouldBe MY_TITLE
        composeRule.routineDetailGoalInput().routineDetailEditableText() shouldBe "3000"
        composeRule.routineDetailAmountPerRecordInput().routineDetailEditableText() shouldBe "250"
        composeRule.routineDetailRecordAmountInput().routineDetailEditableText() shouldBe "500"
    }

    @Test
    fun `TC-7aa9b736-ef04-4bd0-932d-c152ce7a3c26 대상이 삭제 상태가 되어도 내용 표시와 고친 입력을 유지하고 동작을 실행할 수 있다`() {
        val environment = recordingEnvironment(detail = routineDetailOf())
        setScreen(environment)
        composeRule.routineDetailReplaceText(composeRule.routineDetailTitleInput(), ROUTINE_DETAIL_WATER_TWO_LITER)
        composeRule.routineDetailReplaceText(composeRule.routineDetailRecordAmountInput(), "500")

        // 삭제 상태가 된 루틴도 같은 저장 내용으로 다시 전달된다.
        environment.uiState.value = environment.content(detail = environment.detail.copy())
        composeRule.waitForIdle()

        composeRule.onNodeWithContentDescription(ROUTINE_DETAIL_DEFAULT_LOADING).assertDoesNotExist()
        composeRule.routineDetailTitleInput().routineDetailEditableText() shouldBe ROUTINE_DETAIL_WATER_TWO_LITER
        composeRule.routineDetailRecordAmountInput().routineDetailEditableText() shouldBe "500"
        composeRule.routineDetailClickUpdate()
        composeRule.routineDetailClickDelete()
        composeRule.routineDetailClickRecordAdd()
        composeRule.onNode(routineDetailRecordRemoveButton(date = LocalDate(2026, 3, 5), amountText = AMOUNT_250_ML_TEXT)).performSemanticsAction(SemanticsActions.OnClick)
        composeRule.waitForIdle()
        composeRule.onNodeWithText(ROUTINE_DETAIL_DEFAULT_TAG_LABEL).assertExists()
        verify(exactly = 1) { environment.routineViewModel.update(input = any()) }
        verify(exactly = 1) { environment.routineViewModel.delete() }
        verify(exactly = 1) { environment.recordViewModel.add(date = any(), amount = 500) }
        verify(exactly = 1) { environment.recordViewModel.delete(recordId = any()) }
    }

    private fun assertNoFeedback() {
        composeRule.onNodeWithText(ROUTINE_DETAIL_DEFAULT_UPDATE_SUCCEEDED).assertDoesNotExist()
        composeRule.onNodeWithText(ROUTINE_DETAIL_DEFAULT_GOAL_INVALID).assertDoesNotExist()
        composeRule.onNodeWithText(ROUTINE_DETAIL_DEFAULT_AMOUNT_PER_RECORD_INVALID).assertDoesNotExist()
    }

    // 2026-03-05 250의 기록이 하나 있고, 기록을 넣거나 빼면 바로 저장되어 기록 내역에 반영된다.
    private fun recordingEnvironment(detail: RoutineDetail): RoutineDetailTestEnvironment {
        var recordList = listOf(record(date = LocalDate(2026, 3, 5), amount = 250))
        val environment = RoutineDetailTestEnvironment(detail = detail, recordList = recordList)

        every { environment.recordViewModel.add(date = any(), amount = any()) } answers {
            recordList = listOf(record(date = firstArg(), amount = secondArg())) + recordList
            environment.recordPagingData.value = routineDetailRecordPagingDataOf(recordList)
            environment.recordEffect.trySend(RoutineDetailRecordEffect.AddSucceeded)
        }
        every { environment.recordViewModel.delete(recordId = any()) } answers {
            recordList = recordList.filterNot { record -> record.id == firstArg() }
            environment.recordPagingData.value = routineDetailRecordPagingDataOf(recordList)
        }
        return environment
    }

    private fun record(
        date: LocalDate,
        amount: Int,
    ): RoutineRecord = routineDetailRecordOf(routineId = Arb.kotlinUuid().next(), date = date, amount = amount)

    private fun setScreen(environment: RoutineDetailTestEnvironment) {
        composeRule.setContent { RoutineDetailTestScreen(environment = environment) }
        composeRule.waitForIdle()
    }

    private companion object {
        const val COLOR_INPUT = "ColorInput"
        const val MY_TITLE = "내 제목"
        const val OTHER_TITLE = "다른 제목"
        const val UPDATED_DESCRIPTION = "하루 3L"
        val RULE_START_DATE = LocalDate(2026, 1, 15)
    }
}

private const val AMOUNT_250_ML_TEXT = "250ml"

private const val AMOUNT_500_ML_TEXT = "500ml"
