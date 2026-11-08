package io.github.taetae98coding.diary.feature.dday.ui.detail

import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertIsNotFocused
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTextReplacement
import io.github.taetae98coding.diary.core.model.dday.DDayOverflowPolicy
import io.github.taetae98coding.diary.domain.dday.DDayInput
import io.github.taetae98coding.diary.domain.dday.repeat.DDayRepeatInput
import io.github.taetae98coding.diary.domain.dday.repeat.DDayRepeatUnit
import io.github.taetae98coding.diary.domain.dday.repeat.toRepeatSetOrNull
import io.github.taetae98coding.diary.feature.dday.ui.form.DDAY_FORM_DEFAULT_COUNTS_BASE_DAY_LABEL
import io.github.taetae98coding.diary.feature.dday.ui.form.DDAY_FORM_DEFAULT_DESCRIPTION_LABEL
import io.github.taetae98coding.diary.feature.dday.ui.form.DDAY_FORM_DEFAULT_END_DATE_CLEAR
import io.github.taetae98coding.diary.feature.dday.ui.form.DDAY_FORM_DEFAULT_NAVIGATE_UP
import io.github.taetae98coding.diary.feature.dday.ui.form.DDAY_FORM_DEFAULT_OVERFLOW_CLAMP
import io.github.taetae98coding.diary.feature.dday.ui.form.DDAY_FORM_DEFAULT_OVERFLOW_NEXT_MONTH
import io.github.taetae98coding.diary.feature.dday.ui.form.DDAY_FORM_DEFAULT_OVERFLOW_SKIP
import io.github.taetae98coding.diary.feature.dday.ui.form.DDAY_FORM_DEFAULT_REPEAT_ADD
import io.github.taetae98coding.diary.feature.dday.ui.form.DDAY_FORM_DEFAULT_REPEAT_BLANK
import io.github.taetae98coding.diary.feature.dday.ui.form.DDAY_FORM_DEFAULT_TAG_LABEL
import io.github.taetae98coding.diary.feature.dday.ui.form.DDAY_FORM_DEFAULT_TITLE_LABEL
import io.github.taetae98coding.diary.feature.dday.ui.form.DDAY_FORM_DEFAULT_UNIT_DAY
import io.github.taetae98coding.diary.feature.dday.ui.form.DDAY_FORM_DEFAULT_UNIT_MONTH
import io.github.taetae98coding.diary.feature.dday.ui.form.DDAY_FORM_DEFAULT_UNIT_YEAR
import io.github.taetae98coding.diary.feature.dday.ui.form.ddayActionNameList
import io.github.taetae98coding.diary.feature.dday.ui.form.ddayAddRepeat
import io.github.taetae98coding.diary.feature.dday.ui.form.ddayAddRepeatOf
import io.github.taetae98coding.diary.feature.dday.ui.form.ddayCancelDatePicker
import io.github.taetae98coding.diary.feature.dday.ui.form.ddayConfirmDatePicker
import io.github.taetae98coding.diary.feature.dday.ui.form.ddayDateButton
import io.github.taetae98coding.diary.feature.dday.ui.form.ddayDescriptionInput
import io.github.taetae98coding.diary.feature.dday.ui.form.ddayDisplayText
import io.github.taetae98coding.diary.feature.dday.ui.form.ddayDisplayedColorHex
import io.github.taetae98coding.diary.feature.dday.ui.form.ddayEditableText
import io.github.taetae98coding.diary.feature.dday.ui.form.ddayEndDateNotSetButton
import io.github.taetae98coding.diary.feature.dday.ui.form.ddayInputRepeatNumber
import io.github.taetae98coding.diary.feature.dday.ui.form.ddayIsCountsBaseDayOn
import io.github.taetae98coding.diary.feature.dday.ui.form.ddayOpenDatePicker
import io.github.taetae98coding.diary.feature.dday.ui.form.ddayOpenEndDatePickerNotSet
import io.github.taetae98coding.diary.feature.dday.ui.form.ddayOverflowPolicyButton
import io.github.taetae98coding.diary.feature.dday.ui.form.ddayPickDate
import io.github.taetae98coding.diary.feature.dday.ui.form.ddayPickerDayCell
import io.github.taetae98coding.diary.feature.dday.ui.form.ddayPickerSelection
import io.github.taetae98coding.diary.feature.dday.ui.form.ddayRemoveRepeat
import io.github.taetae98coding.diary.feature.dday.ui.form.ddayRepeatList
import io.github.taetae98coding.diary.feature.dday.ui.form.ddayRepeatNumberInput
import io.github.taetae98coding.diary.feature.dday.ui.form.ddaySelectOverflowPolicy
import io.github.taetae98coding.diary.feature.dday.ui.form.ddaySelectRepeatUnit
import io.github.taetae98coding.diary.feature.dday.ui.form.ddayTitleInput
import io.github.taetae98coding.diary.feature.dday.ui.form.ddayToggleCountsBaseDay
import io.github.taetae98coding.diary.feature.dday.ui.form.ddayTypePickerDate
import io.github.taetae98coding.diary.feature.dday.ui.form.ddayVisibleTextList
import io.github.taetae98coding.diary.feature.dday.ui.resetAndroidUiDispatcher
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.slot
import io.mockk.verify
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w480dp-h1200dp")
class DDayDetailScreenTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Before
    fun setUp() {
        resetAndroidUiDispatcher()
    }

    @Test
    fun `TC-2d477ce7-412d-4170-a861-e1750504a898 조회에 성공하면 저장된 정보로 화면을 채운다`() {
        val environment = DDayDetailTestEnvironment()
        setScreen(environment)

        composeRule.onNode(ddayDetailTopBarTitle(DDAY_DETAIL_MARRIAGE), useUnmergedTree = true).assertExists()
        composeRule.ddayTitleInput().ddayEditableText() shouldBe DDAY_DETAIL_MARRIAGE
        composeRule.ddayDescriptionInput().ddayEditableText() shouldBe DDAY_DETAIL_US
        composeRule.ddayDisplayedColorHex() shouldBe environment.detail.color.colorHex()
        composeRule.ddayDateButton(LocalDate(2025, 1, 15)).assertExists()
        composeRule.ddayIsCountsBaseDayOn() shouldBe false
        composeRule.ddayRepeatList() shouldBe listOf("100" to DDAY_FORM_DEFAULT_UNIT_DAY)
        composeRule.ddayDateButton(LocalDate(2030, 1, 15)).assertExists()
        composeRule.ddayOverflowPolicyButton(DDAY_FORM_DEFAULT_OVERFLOW_SKIP).assertExists()
    }

    @Test
    fun `TC-8572740e-166e-4442-9c18-4f8b68a68f2b 24개월은 2 년으로 채운다`() {
        assertRepeatFilled(setOf(month(24)), listOf("2" to DDAY_FORM_DEFAULT_UNIT_YEAR))
    }

    @Test
    fun `TC-8572740e-166e-4442-9c18-4f8b68a68f2b 6개월은 6 개월로 채운다`() {
        assertRepeatFilled(setOf(month(6)), listOf("6" to DDAY_FORM_DEFAULT_UNIT_MONTH))
    }

    @Test
    fun `TC-8572740e-166e-4442-9c18-4f8b68a68f2b 12개월과 100일은 일, 년 순으로 채운다`() {
        assertRepeatFilled(setOf(month(12), day(100)), listOf("100" to DDAY_FORM_DEFAULT_UNIT_DAY, "1" to DDAY_FORM_DEFAULT_UNIT_YEAR))
    }

    @Test
    fun `TC-8572740e-166e-4442-9c18-4f8b68a68f2b 여러 간격을 단위 순과 숫자 순으로 채운다`() {
        assertRepeatFilled(
            setOf(month(24), month(1), day(100), month(12), day(10)),
            listOf(
                "10" to DDAY_FORM_DEFAULT_UNIT_DAY,
                "100" to DDAY_FORM_DEFAULT_UNIT_DAY,
                "1" to DDAY_FORM_DEFAULT_UNIT_MONTH,
                "1" to DDAY_FORM_DEFAULT_UNIT_YEAR,
                "2" to DDAY_FORM_DEFAULT_UNIT_YEAR,
            ),
        )
    }

    @Test
    fun `TC-8572740e-166e-4442-9c18-4f8b68a68f2b 18개월과 36개월은 18 개월, 3 년으로 채운다`() {
        assertRepeatFilled(setOf(month(18), month(36)), listOf("18" to DDAY_FORM_DEFAULT_UNIT_MONTH, "3" to DDAY_FORM_DEFAULT_UNIT_YEAR))
    }

    @Test
    fun `TC-8572740e-166e-4442-9c18-4f8b68a68f2b 반복 간격이 없으면 간격을 두지 않는다`() {
        assertRepeatFilled(emptySet(), emptyList())
    }

    private fun assertRepeatFilled(
        repeats: Set<DateTimeUnit.DateBased>,
        expected: List<Pair<String, String>>,
    ) {
        setScreen(DDayDetailTestEnvironment(detail = ddayDetailOf(repeats = repeats)))

        composeRule.ddayRepeatList() shouldBe expected
    }

    @Test
    fun `TC-a806016d-0489-4057-a8e6-cca94d455c50 설명이나 종료일이 없으면 빈 설명과 고르지 않은 종료일로 채운다`() {
        setScreen(DDayDetailTestEnvironment(detail = ddayDetailOf(description = "", endDate = null)))

        composeRule.ddayDescriptionInput().ddayEditableText() shouldBe ""
        composeRule.ddayEndDateNotSetButton().assertExists()
        composeRule.onNodeWithContentDescription(DDAY_FORM_DEFAULT_END_DATE_CLEAR).assertDoesNotExist()
    }

    @Test
    fun `TC-b891760f-20d2-4a64-a18d-b74c0fc13761 진입할 때 어느 입력에도 초점을 두지 않는다`() {
        setScreen(DDayDetailTestEnvironment(detail = ddayDetailOf(repeats = setOf(day(100), month(12)))))

        composeRule.ddayTitleInput().assertIsNotFocused()
        composeRule.ddayDescriptionInput().assertIsNotFocused()
        composeRule.ddayRepeatNumberInput(1).assertIsNotFocused()
        composeRule.ddayRepeatNumberInput(2).assertIsNotFocused()
    }

    @Test
    fun `TC-1a600949-d45a-44f9-b294-98e25e4d5d32 조회 중에는 조회 중 상태만 보이고 입력과 동작을 실행할 수 없다`() {
        val environment = DDayDetailTestEnvironment()
        environment.uiState.value = DDayDetailUiState.Loading
        setScreen(environment)

        composeRule.onNodeWithContentDescription(DDAY_DETAIL_DEFAULT_LOADING).assertExists()
        composeRule.onNode(hasSetTextAction()).assertDoesNotExist()
        composeRule.onNodeWithText(DDAY_FORM_DEFAULT_TAG_LABEL).assertDoesNotExist()
        composeRule.onNode(ddayDetailDeleteButton()).assertDoesNotExist()
        composeRule.onNode(ddayDetailUpdateButton()).assertDoesNotExist()
        composeRule.ddayActionNameList() shouldBe listOf(DDAY_FORM_DEFAULT_NAVIGATE_UP)
    }

    @Test
    fun `TC-0759207b-2d41-48bc-a6e0-009446d644b0 조회할 수 없으면 조회 중 상태를 유지하고 오류 안내와 다시 시도를 두지 않는다`() {
        val environment = DDayDetailTestEnvironment()
        environment.uiState.value = DDayDetailUiState.Loading
        setScreen(environment)

        composeRule.onNodeWithContentDescription(DDAY_DETAIL_DEFAULT_LOADING).assertExists()
        composeRule.ddayActionNameList() shouldBe listOf(DDAY_FORM_DEFAULT_NAVIGATE_UP)
        composeRule.ddayVisibleTextList().shouldBeEmpty()
    }

    @Test
    fun `TC-1671fa4b-86cc-4142-b9d8-7f70a72168c9 화면에는 정해진 입력과 동작만 두고 카드 문구, 기념일과 프리셋은 두지 않는다`() {
        val environment = DDayDetailTestEnvironment()
        setScreen(environment)
        composeRule.ddayTitleInput().performTextInput("2")
        composeRule.waitForIdle()
        val color = composeRule.ddayDisplayedColorHex()

        val actionNameList = composeRule.ddayActionNameList().map { name -> if (name.startsWith(color)) COLOR_INPUT else name }

        actionNameList shouldContainExactlyInAnyOrder
            listOf(
                DDAY_FORM_DEFAULT_NAVIGATE_UP,
                DDAY_DETAIL_DEFAULT_DELETE,
                DDAY_FORM_DEFAULT_TITLE_LABEL,
                DDAY_FORM_DEFAULT_DESCRIPTION_LABEL,
                DEFAULT_DESCRIPTION_INPUT_TAB,
                DEFAULT_DESCRIPTION_PREVIEW_TAB,
                COLOR_INPUT,
                ddayDisplayText(LocalDate(2025, 1, 15)),
                DDAY_FORM_DEFAULT_COUNTS_BASE_DAY_LABEL,
                ddayDisplayText(LocalDate(2030, 1, 15)),
                DDAY_FORM_DEFAULT_END_DATE_CLEAR,
                DDAY_FORM_DEFAULT_OVERFLOW_SKIP,
                DDAY_FORM_DEFAULT_REPEAT_ADD,
                "Interval 1 number",
                "Interval 1 unit",
                "Remove interval 1",
                // 값이 있는 제목, 설명과 간격 숫자 입력의 지우기 버튼이다.
                DEFAULT_CLEAR_TEXT,
                DEFAULT_CLEAR_TEXT,
                DEFAULT_CLEAR_TEXT,
                DDAY_FORM_DEFAULT_TAG_LABEL,
                DDAY_DETAIL_DEFAULT_UPDATE,
            )
        listOf("Couple", "Exam", "Baby", "Custom").forEach { preset -> composeRule.onNodeWithText(preset).assertDoesNotExist() }
    }

    @Test
    fun `TC-4e058e08-10f5-4c20-a70f-5b31611db14c 기준일 고르기는 기준일에서 시작한다`() {
        setRuleScreen()

        composeRule.ddayOpenDatePicker(RULE_BASE_DATE)

        composeRule.ddayPickerSelection(RULE_BASE_DATE).assertExists()
    }

    @Test
    fun `TC-4e058e08-10f5-4c20-a70f-5b31611db14c 기준일을 고르고 확인하면 기준일이 바뀐다`() {
        setRuleScreen()

        composeRule.ddayPickDate(from = RULE_BASE_DATE, to = LocalDate(2026, 2, 1))

        composeRule.ddayDateButton(LocalDate(2026, 2, 1)).assertExists()
        composeRule.ddayDateButton(RULE_BASE_DATE).assertDoesNotExist()
    }

    @Test
    fun `TC-4e058e08-10f5-4c20-a70f-5b31611db14c 기준일을 고르고 취소하면 기준일이 그대로다`() {
        setRuleScreen()

        composeRule.ddayOpenDatePicker(RULE_BASE_DATE)
        composeRule.ddayTypePickerDate(LocalDate(2026, 2, 1))
        composeRule.ddayCancelDatePicker()

        composeRule.ddayDateButton(RULE_BASE_DATE).assertExists()
        composeRule.ddayDateButton(LocalDate(2026, 2, 1)).assertDoesNotExist()
    }

    @Test
    fun `TC-4e058e08-10f5-4c20-a70f-5b31611db14c 기준일을 종료일보다 뒤로 고르면 종료일도 같은 날이 된다`() {
        setRuleScreen()

        composeRule.ddayPickDate(from = RULE_BASE_DATE, to = LocalDate(2026, 7, 10))

        composeRule.onAllNodesWithText(ddayDisplayText(LocalDate(2026, 7, 10))).fetchSemanticsNodes().size shouldBe 2
        composeRule.ddayDateButton(RULE_END_DATE).assertDoesNotExist()
    }

    @Test
    fun `TC-4e058e08-10f5-4c20-a70f-5b31611db14c 종료일 고르기는 종료일에서 시작하고 기준일 앞은 고를 수 없다`() {
        setRuleScreen()

        composeRule.ddayOpenDatePicker(RULE_END_DATE)
        composeRule.ddayPickerSelection(RULE_END_DATE).assertExists()
        repeat(MONTHS_FROM_JANUARY_TO_JUNE) {
            composeRule.onNodeWithContentDescription(DEFAULT_PREVIOUS_MONTH).performClick()
            composeRule.waitForIdle()
        }

        composeRule.ddayPickerDayCell(LocalDate(2026, 1, 14)).assertIsNotEnabled()
    }

    @Test
    fun `TC-4e058e08-10f5-4c20-a70f-5b31611db14c 종료일을 지운 뒤 고르기는 기준일에서 시작한다`() {
        setRuleScreen()

        composeRule.onNodeWithContentDescription(DDAY_FORM_DEFAULT_END_DATE_CLEAR).performSemanticsAction(SemanticsActions.OnClick)
        composeRule.waitForIdle()
        composeRule.ddayEndDateNotSetButton().assertExists()
        composeRule.ddayOpenEndDatePickerNotSet()

        composeRule.ddayPickerSelection(RULE_BASE_DATE).assertExists()
    }

    @Test
    fun `TC-4e058e08-10f5-4c20-a70f-5b31611db14c 기준일을 1일로 세기를 바꾸면 바뀐 값으로 표시된다`() {
        setRuleScreen()
        val before = composeRule.ddayIsCountsBaseDayOn()

        composeRule.ddayToggleCountsBaseDay()

        composeRule.ddayIsCountsBaseDayOn() shouldBe !before
    }

    @Test
    fun `TC-4e058e08-10f5-4c20-a70f-5b31611db14c 간격을 추가하면 끝에 빈 일 간격이 놓이고 초점은 옮겨지지 않는다`() {
        setRuleScreen()

        composeRule.ddayAddRepeat()

        composeRule.ddayRepeatList() shouldBe listOf("100" to DDAY_FORM_DEFAULT_UNIT_DAY, "" to DDAY_FORM_DEFAULT_UNIT_DAY)
        composeRule.ddayRepeatNumberInput(2).assertIsNotFocused()
        composeRule.ddayTitleInput().assertIsNotFocused()
    }

    @Test
    fun `TC-4e058e08-10f5-4c20-a70f-5b31611db14c 100 일 간격을 삭제하면 간격이 하나도 없다`() {
        setRuleScreen()

        composeRule.ddayRemoveRepeat(position = 1)

        composeRule.ddayRepeatList().shouldBeEmpty()
    }

    @Test
    fun `TC-4e058e08-10f5-4c20-a70f-5b31611db14c 100 일의 단위를 개월로 바꾸면 100 개월이 된다`() {
        setRuleScreen()

        composeRule.ddaySelectRepeatUnit(position = 1, unitLabel = DDAY_FORM_DEFAULT_UNIT_MONTH)

        composeRule.ddayRepeatList() shouldBe listOf("100" to DDAY_FORM_DEFAULT_UNIT_MONTH)
    }

    @Test
    fun `TC-4e058e08-10f5-4c20-a70f-5b31611db14c 숫자 끝에 a를 입력해도 반영하지 않는다`() {
        setRuleScreen()

        composeRule.ddayRepeatNumberInput(1).performTextInput("a")
        composeRule.waitForIdle()

        composeRule.ddayRepeatList() shouldBe listOf("100" to DDAY_FORM_DEFAULT_UNIT_DAY)
    }

    @Test
    fun `TC-4e058e08-10f5-4c20-a70f-5b31611db14c 달력에 없는 날짜 처리를 다음 달 1일로 미룸으로 고를 수 있다`() {
        setRuleScreen()

        composeRule.ddaySelectOverflowPolicy(currentLabel = DDAY_FORM_DEFAULT_OVERFLOW_CLAMP, label = DDAY_FORM_DEFAULT_OVERFLOW_NEXT_MONTH)

        composeRule.ddayOverflowPolicyButton(DDAY_FORM_DEFAULT_OVERFLOW_NEXT_MONTH).assertExists()
    }

    private fun setRuleScreen() {
        setScreen(
            DDayDetailTestEnvironment(
                detail =
                    ddayDetailOf(
                        baseDate = RULE_BASE_DATE,
                        countsBaseDay = true,
                        repeats = setOf(day(100)),
                        endDate = RULE_END_DATE,
                        overflowPolicy = DDayOverflowPolicy.CLAMP_TO_MONTH_END,
                    ),
            ),
        )
    }

    @Test
    fun `TC-caea6e29-3c1e-4196-9833-aef1ea981f52 입력을 바꾸지 않으면 수정 동작이 없다`() {
        assertUpdateAction(isProvided = false) {}
    }

    @Test
    fun `TC-caea6e29-3c1e-4196-9833-aef1ea981f52 제목 뒤에 공백을 붙이면 수정 동작이 있다`() {
        assertUpdateAction(isProvided = true) { composeRule.ddayTitleInput().performTextReplacement("$DDAY_DETAIL_MARRIAGE ") }
    }

    @Test
    fun `TC-caea6e29-3c1e-4196-9833-aef1ea981f52 설명에 줄바꿈을 붙이면 수정 동작이 있다`() {
        assertUpdateAction(isProvided = true) { composeRule.ddayDescriptionInput().performTextReplacement("$DDAY_DETAIL_US\n") }
    }

    @Test
    fun `TC-caea6e29-3c1e-4196-9833-aef1ea981f52 컬러를 바꾸면 수정 동작이 있다`() {
        assertUpdateAction(isProvided = true) { detail ->
            composeRule.ddayDetailChangeColor(fromHex = detail.color.colorHex(), toHex = ddayDetailOtherColor(detail.color).colorHex())
        }
    }

    @Test
    fun `TC-caea6e29-3c1e-4196-9833-aef1ea981f52 기준일을 바꾸면 수정 동작이 있다`() {
        assertUpdateAction(isProvided = true) { composeRule.ddayPickDate(from = LocalDate(2025, 1, 15), to = LocalDate(2025, 1, 16)) }
    }

    @Test
    fun `TC-caea6e29-3c1e-4196-9833-aef1ea981f52 기준일을 1일로 세기를 끄면 수정 동작이 있다`() {
        assertUpdateAction(isProvided = true) { composeRule.ddayToggleCountsBaseDay() }
    }

    @Test
    fun `TC-caea6e29-3c1e-4196-9833-aef1ea981f52 종료일을 고르면 수정 동작이 있다`() {
        assertUpdateAction(isProvided = true) {
            composeRule.ddayOpenEndDatePickerNotSet()
            composeRule.ddayConfirmDatePicker()
        }
    }

    @Test
    fun `TC-caea6e29-3c1e-4196-9833-aef1ea981f52 달력에 없는 날짜 처리를 바꾸면 수정 동작이 있다`() {
        assertUpdateAction(isProvided = true) {
            composeRule.ddaySelectOverflowPolicy(currentLabel = DDAY_FORM_DEFAULT_OVERFLOW_CLAMP, label = DDAY_FORM_DEFAULT_OVERFLOW_SKIP)
        }
    }

    @Test
    fun `TC-caea6e29-3c1e-4196-9833-aef1ea981f52 1 년을 2 년으로 바꾸면 수정 동작이 있다`() {
        assertUpdateAction(isProvided = true) { composeRule.ddayInputRepeatNumber(position = 2, number = "2") }
    }

    @Test
    fun `TC-caea6e29-3c1e-4196-9833-aef1ea981f52 1 년 간격을 삭제하면 수정 동작이 있다`() {
        assertUpdateAction(isProvided = true) { composeRule.ddayRemoveRepeat(position = 2) }
    }

    @Test
    fun `TC-caea6e29-3c1e-4196-9833-aef1ea981f52 제목을 바꿨다가 되돌리면 수정 동작이 없다`() {
        assertUpdateAction(isProvided = false) {
            composeRule.ddayTitleInput().performTextReplacement(DDAY_DETAIL_ANNIVERSARY)
            composeRule.waitForIdle()
            composeRule.onNode(ddayDetailUpdateButton()).assertExists()
            composeRule.ddayTitleInput().performTextReplacement(DDAY_DETAIL_MARRIAGE)
        }
    }

    @Test
    fun `TC-caea6e29-3c1e-4196-9833-aef1ea981f52 간격 순서만 바꾸면 수정 동작이 없다`() {
        assertUpdateAction(isProvided = false) {
            composeRule.ddayRemoveRepeat(position = 1)
            composeRule.ddayAddRepeatOf(number = "100", unitLabel = DDAY_FORM_DEFAULT_UNIT_DAY)
            composeRule.ddayRepeatList() shouldBe listOf("1" to DDAY_FORM_DEFAULT_UNIT_YEAR, "100" to DDAY_FORM_DEFAULT_UNIT_DAY)
        }
    }

    @Test
    fun `TC-caea6e29-3c1e-4196-9833-aef1ea981f52 1 년을 12 개월로 바꾸면 수정 동작이 없다`() {
        assertUpdateAction(isProvided = false) {
            composeRule.ddayInputRepeatNumber(position = 2, number = "12")
            composeRule.ddaySelectRepeatUnit(position = 2, unitLabel = DDAY_FORM_DEFAULT_UNIT_MONTH)
        }
    }

    @Test
    fun `TC-caea6e29-3c1e-4196-9833-aef1ea981f52 0100 일을 더하면 수정 동작이 없다`() {
        assertUpdateAction(isProvided = false) { composeRule.ddayAddRepeatOf(number = "0100", unitLabel = DDAY_FORM_DEFAULT_UNIT_DAY) }
    }

    @Test
    fun `TC-caea6e29-3c1e-4196-9833-aef1ea981f52 100 일을 한 번 더 더하면 수정 동작이 없다`() {
        assertUpdateAction(isProvided = false) { composeRule.ddayAddRepeatOf(number = "100", unitLabel = DDAY_FORM_DEFAULT_UNIT_DAY) }
    }

    @Test
    fun `TC-caea6e29-3c1e-4196-9833-aef1ea981f52 숫자가 빈 간격을 더하면 수정 동작이 있다`() {
        assertUpdateAction(isProvided = true) { composeRule.ddayAddRepeat() }
    }

    @Test
    fun `TC-caea6e29-3c1e-4196-9833-aef1ea981f52 0 일 간격을 더하면 수정 동작이 있다`() {
        assertUpdateAction(isProvided = true) { composeRule.ddayAddRepeatOf(number = "0", unitLabel = DDAY_FORM_DEFAULT_UNIT_DAY) }
    }

    // 제목 결혼, 설명 우리, 기준일 2025-01-15, 1일로 세기 켬, 100일과 12개월, 종료일 없음, 말일로 당김으로 저장된 디데이다.
    private fun assertUpdateAction(
        isProvided: Boolean,
        change: (io.github.taetae98coding.diary.core.model.dday.DDayDetail) -> Unit,
    ) {
        val detail =
            ddayDetailOf(
                countsBaseDay = true,
                repeats = setOf(day(100), month(12)),
                endDate = null,
                overflowPolicy = DDayOverflowPolicy.CLAMP_TO_MONTH_END,
            )
        setScreen(DDayDetailTestEnvironment(detail = detail))
        composeRule.ddayRepeatList() shouldBe listOf("100" to DDAY_FORM_DEFAULT_UNIT_DAY, "1" to DDAY_FORM_DEFAULT_UNIT_YEAR)
        composeRule.onNode(ddayDetailUpdateButton()).assertDoesNotExist()

        change(detail)
        composeRule.waitForIdle()

        if (isProvided) {
            composeRule.onNode(ddayDetailUpdateButton()).assertExists()
        } else {
            composeRule.onNode(ddayDetailUpdateButton()).assertDoesNotExist()
        }
    }

    @Test
    fun `TC-05982e66-411b-43ea-8479-c994c91a3383 수정에 성공하면 화면과 입력을 유지하고 성공 피드백을 제공한다`() {
        val environment = DDayDetailTestEnvironment(detail = ddayDetailOf(repeats = setOf(day(100), month(12))))
        setScreen(environment)
        composeRule.ddayTitleInput().performTextReplacement(DDAY_DETAIL_ANNIVERSARY)
        composeRule.ddayInputRepeatNumber(position = 2, number = "12")
        composeRule.ddaySelectRepeatUnit(position = 2, unitLabel = DDAY_FORM_DEFAULT_UNIT_MONTH)
        composeRule.ddayPickDate(from = LocalDate(2025, 1, 15), to = LocalDate(2025, 2, 1))
        val input =
            expectedInput(environment, title = DDAY_DETAIL_ANNIVERSARY, baseDate = LocalDate(2025, 2, 1)).copy(
                repeatList = listOf(DDayRepeatInput(number = 100, unit = DDayRepeatUnit.DAY), DDayRepeatInput(number = 12, unit = DDayRepeatUnit.MONTH)),
            )

        composeRule.ddayDetailClickUpdate()
        environment.effect.trySend(DDayDetailEffect.UpdateSucceeded)
        environment.uiState.value =
            environment.content(detail = environment.detail.copy(title = DDAY_DETAIL_ANNIVERSARY, baseDate = LocalDate(2025, 2, 1)))
        composeRule.waitForIdle()

        verify(exactly = 1) { environment.ddayViewModel.update(input = input) }
        composeRule.onNodeWithText(DDAY_DETAIL_DEFAULT_UPDATE_SUCCEEDED).assertExists()
        composeRule.onNode(ddayDetailTopBarTitle(DDAY_DETAIL_ANNIVERSARY), useUnmergedTree = true).assertExists()
        composeRule.ddayTitleInput().ddayEditableText() shouldBe DDAY_DETAIL_ANNIVERSARY
        composeRule.ddayRepeatList() shouldBe listOf("100" to DDAY_FORM_DEFAULT_UNIT_DAY, "12" to DDAY_FORM_DEFAULT_UNIT_MONTH)
        composeRule.ddayDateButton(LocalDate(2025, 2, 1)).assertExists()
        environment.navigateUpCount shouldBe 0
    }

    @Test
    @Config(qualifiers = "ko-w480dp-h1200dp")
    fun `TC-05982e66-411b-43ea-8479-c994c91a3383 한국어 환경에서 수정 성공 피드백을 제공한다`() {
        val environment = DDayDetailTestEnvironment()
        setScreen(environment)
        composeRule.ddayTitleInput().performTextReplacement(DDAY_DETAIL_ANNIVERSARY)
        composeRule.waitForIdle()

        composeRule.onNodeWithContentDescription(DDAY_DETAIL_KOREAN_UPDATE).performClick()
        environment.effect.trySend(DDayDetailEffect.UpdateSucceeded)
        composeRule.waitForIdle()

        composeRule.onNodeWithText(DDAY_DETAIL_KOREAN_UPDATE_SUCCEEDED).assertExists()
    }

    @Test
    fun `TC-8058eda4-7002-4560-90d2-d83371015b28 수정을 처리하는 동안 진행 상태를 표시한다`() {
        val environment = DDayDetailTestEnvironment()
        environment.uiState.value = environment.content(isUpdateInProgress = true)
        setScreen(environment)
        composeRule.ddayTitleInput().performTextReplacement(DDAY_DETAIL_ANNIVERSARY)
        composeRule.waitForIdle()

        composeRule.onNode(ddayDetailUpdateButton() and hasDDayDetailProgress(), useUnmergedTree = true).assertExists()

        environment.uiState.value = environment.content()
        composeRule.waitForIdle()
        composeRule.onNode(ddayDetailUpdateButton() and hasDDayDetailProgress(), useUnmergedTree = true).assertDoesNotExist()
    }

    @Test
    fun `TC-84bfc631-b300-415b-a493-c3d60b58af87 수정에 실패하면 안내 없이 입력을 유지하고 다시 시도할 수 있다`() {
        val environment = DDayDetailTestEnvironment()
        setScreen(environment)
        composeRule.ddayTitleInput().performTextReplacement(DDAY_DETAIL_ANNIVERSARY)
        composeRule.ddayInputRepeatNumber(position = 1, number = "200")

        composeRule.ddayDetailClickUpdate()
        composeRule.ddayDetailClickUpdate()

        verify(exactly = 2) { environment.ddayViewModel.update(input = any()) }
        composeRule.ddayTitleInput().ddayEditableText() shouldBe DDAY_DETAIL_ANNIVERSARY
        composeRule.ddayRepeatList() shouldBe listOf("200" to DDAY_FORM_DEFAULT_UNIT_DAY)
        composeRule.onNode(ddayDetailUpdateButton() and hasDDayDetailProgress(), useUnmergedTree = true).assertDoesNotExist()
        composeRule.onNodeWithText(DDAY_DETAIL_DEFAULT_UPDATE_SUCCEEDED).assertDoesNotExist()
        composeRule.onNodeWithText(DDAY_FORM_DEFAULT_REPEAT_BLANK).assertDoesNotExist()
    }

    @Test
    fun `TC-cbe821d7-7fa6-48cb-91b1-8847d83889e4 제목을 비운 채 수정하면 기존 제목을 유지하고 수정 동작이 계속 제공된다`() {
        assertBlankTitleUpdate(title = "")
    }

    @Test
    fun `TC-cbe821d7-7fa6-48cb-91b1-8847d83889e4 제목을 공백뿐으로 수정하면 기존 제목을 유지하고 수정 동작이 계속 제공된다`() {
        assertBlankTitleUpdate(title = "   ")
    }

    private fun assertBlankTitleUpdate(title: String) {
        val environment = DDayDetailTestEnvironment()
        setScreen(environment)
        composeRule.ddayTitleInput().performTextReplacement(title)
        composeRule.ddayDescriptionInput().performTextReplacement(US_TWO)
        composeRule.ddayPickDate(from = LocalDate(2025, 1, 15), to = LocalDate(2025, 2, 1))

        composeRule.ddayDetailClickUpdate()
        environment.effect.trySend(DDayDetailEffect.UpdateSucceeded)
        environment.uiState.value = environment.content(detail = environment.detail.copy(description = US_TWO, baseDate = LocalDate(2025, 2, 1)))
        composeRule.waitForIdle()

        verify(exactly = 1) {
            environment.ddayViewModel.update(input = expectedInput(environment, title = title, description = US_TWO, baseDate = LocalDate(2025, 2, 1)))
        }
        composeRule.onNode(ddayDetailTopBarTitle(DDAY_DETAIL_MARRIAGE), useUnmergedTree = true).assertExists()
        composeRule.ddayTitleInput().ddayEditableText() shouldBe title
        composeRule.onNode(ddayDetailUpdateButton()).assertExists()
    }

    @Test
    fun `TC-55d21aa4-9b96-4c9b-be03-ffc5326d5277 숫자가 빈 일 간격을 더하면 수정하지 않고 숫자 입력을 알린다`() {
        assertRepeatBlank(title = DDAY_DETAIL_ANNIVERSARY, number = "", unitLabel = DDAY_FORM_DEFAULT_UNIT_DAY)
    }

    @Test
    fun `TC-55d21aa4-9b96-4c9b-be03-ffc5326d5277 0 개월 간격을 더하면 수정하지 않고 숫자 입력을 알린다`() {
        assertRepeatBlank(title = DDAY_DETAIL_ANNIVERSARY, number = "0", unitLabel = DDAY_FORM_DEFAULT_UNIT_MONTH)
    }

    @Test
    fun `TC-55d21aa4-9b96-4c9b-be03-ffc5326d5277 제목을 비우고 숫자가 빈 년 간격을 더하면 숫자 입력을 알린다`() {
        assertRepeatBlank(title = "", number = "", unitLabel = DDAY_FORM_DEFAULT_UNIT_YEAR)
    }

    private fun assertRepeatBlank(
        title: String,
        number: String,
        unitLabel: String,
    ) {
        val environment = DDayDetailTestEnvironment()
        val input = slot<DDayInput>()
        every { environment.ddayViewModel.update(input = capture(input)) } answers { environment.effect.trySend(DDayDetailEffect.RepeatBlank) }
        setScreen(environment)
        composeRule.ddayTitleInput().performTextReplacement(title)
        composeRule.ddayAddRepeatOf(number = number, unitLabel = unitLabel)
        // 저장된 설명이 있으면 설명 입력은 미리보기로 열리므로, 입력 탭으로 바꾼 뒤 설명 입력에 초점을 둔다.
        composeRule.onNodeWithContentDescription(DEFAULT_DESCRIPTION_INPUT_TAB).performClick()
        composeRule.waitForIdle()
        composeRule.ddayDescriptionInput().performSemanticsAction(SemanticsActions.RequestFocus)
        composeRule.waitForIdle()
        composeRule.ddayDescriptionInput().assertIsFocused()

        composeRule.ddayDetailClickUpdate()

        input.captured.repeatList.toRepeatSetOrNull() shouldBe null
        composeRule.onNodeWithText(DDAY_FORM_DEFAULT_REPEAT_BLANK).assertExists()
        composeRule.ddayDescriptionInput().assertIsFocused()
        composeRule.ddayTitleInput().ddayEditableText() shouldBe title
        composeRule.ddayRepeatList() shouldBe listOf("100" to DDAY_FORM_DEFAULT_UNIT_DAY, number to unitLabel)
        composeRule.onNodeWithText(DDAY_DETAIL_DEFAULT_UPDATE_SUCCEEDED).assertDoesNotExist()
    }

    @Test
    fun `TC-be5db26f-c4e4-4296-b736-96ad9ef9f704 삭제를 선택하면 되묻지 않고 한 번 요청하고 성공하면 한 번 돌아간다`() {
        val environment = DDayDetailTestEnvironment()
        every { environment.ddayViewModel.delete() } answers { environment.effect.trySend(DDayDetailEffect.DeleteSucceeded) }
        setScreen(environment)

        composeRule.ddayDetailClickDelete()

        composeRule
            .onNode(
                androidx.compose.ui.test
                    .isDialog(),
            ).assertDoesNotExist()
        verify(exactly = 1) { environment.ddayViewModel.delete() }
        environment.navigateUpCount shouldBe 1
    }

    @Test
    fun `TC-2dab4b33-65f9-4c09-8c0b-cda971982bbf 삭제를 처리하는 동안 진행 상태를 표시하고 수정은 실행할 수 있다`() {
        val environment = DDayDetailTestEnvironment()
        environment.uiState.value = environment.content(isDeleteInProgress = true)
        setScreen(environment)
        composeRule.ddayTitleInput().performTextReplacement(DDAY_DETAIL_ANNIVERSARY)
        composeRule.waitForIdle()

        composeRule.onNode(ddayDetailDeleteButton() and hasDDayDetailProgress(), useUnmergedTree = true).assertExists()
        composeRule.ddayDetailClickUpdate()

        verify(exactly = 1) { environment.ddayViewModel.update(input = any()) }
    }

    @Test
    fun `TC-152d3bcd-bb23-4bee-a126-61e8a693f90b 삭제에 실패하면 화면에 머무르고 입력을 유지하며 다시 삭제할 수 있다`() {
        val environment = DDayDetailTestEnvironment()
        setScreen(environment)
        composeRule.ddayTitleInput().performTextReplacement(DDAY_DETAIL_ANNIVERSARY)
        composeRule.waitForIdle()

        composeRule.ddayDetailClickDelete()
        composeRule.ddayDetailClickDelete()

        verify(exactly = 2) { environment.ddayViewModel.delete() }
        environment.navigateUpCount shouldBe 0
        composeRule.ddayTitleInput().ddayEditableText() shouldBe DDAY_DETAIL_ANNIVERSARY
        composeRule.onNode(ddayDetailDeleteButton() and hasDDayDetailProgress(), useUnmergedTree = true).assertDoesNotExist()
        composeRule.onNodeWithText(DDAY_DETAIL_DEFAULT_UPDATE_SUCCEEDED).assertDoesNotExist()
    }

    @Test
    fun `TC-ba4397d4-64ba-432d-8886-2ae6fa3fe0ff 저장된 내용이 다른 경로에서 바뀌면 화면 제목만 갱신한다`() {
        val environment = DDayDetailTestEnvironment()
        setScreen(environment)
        composeRule.ddayTitleInput().performTextReplacement(MY_TITLE)
        composeRule.ddayInputRepeatNumber(position = 1, number = "200")

        environment.uiState.value =
            environment.content(
                detail = environment.detail.copy(title = OTHER_TITLE, baseDate = LocalDate(2024, 6, 1), repeats = setOf(month(3))),
            )
        composeRule.waitForIdle()

        composeRule.onNode(ddayDetailTopBarTitle(OTHER_TITLE), useUnmergedTree = true).assertExists()
        composeRule.ddayTitleInput().ddayEditableText() shouldBe MY_TITLE
        composeRule.ddayDateButton(LocalDate(2025, 1, 15)).assertExists()
        composeRule.ddayRepeatList() shouldBe listOf("200" to DDAY_FORM_DEFAULT_UNIT_DAY)
    }

    @Test
    fun `TC-04c77513-b2ef-476a-80c1-62cf9b48248d 입력을 고치지 않았어도 저장된 내용이 바뀌면 처음 채운 값을 유지하고 수정 동작을 제공한다`() {
        val environment = DDayDetailTestEnvironment()
        setScreen(environment)
        composeRule.onNode(ddayDetailUpdateButton()).assertDoesNotExist()

        environment.uiState.value =
            environment.content(
                detail = environment.detail.copy(description = OTHER_TITLE, baseDate = LocalDate(2024, 6, 1), repeats = setOf(month(3))),
            )
        composeRule.waitForIdle()

        composeRule.ddayDescriptionInput().ddayEditableText() shouldBe DDAY_DETAIL_US
        composeRule.ddayDateButton(LocalDate(2025, 1, 15)).assertExists()
        composeRule.ddayRepeatList() shouldBe listOf("100" to DDAY_FORM_DEFAULT_UNIT_DAY)
        composeRule.onNode(ddayDetailUpdateButton()).assertExists()
    }

    @Test
    fun `TC-0ea235fb-fad7-4e8a-ae24-dc4f41180732 대상이 삭제 상태가 되어도 내용 표시와 고친 입력을 유지하고 동작을 실행할 수 있다`() {
        val environment = DDayDetailTestEnvironment()
        setScreen(environment)
        composeRule.ddayTitleInput().performTextReplacement(DDAY_DETAIL_ANNIVERSARY)
        composeRule.waitForIdle()

        // 삭제 상태가 된 디데이도 같은 저장 내용으로 다시 전달된다.
        environment.uiState.value = environment.content(detail = environment.detail.copy())
        composeRule.waitForIdle()

        composeRule.onNodeWithContentDescription(DDAY_DETAIL_DEFAULT_LOADING).assertDoesNotExist()
        composeRule.ddayTitleInput().ddayEditableText() shouldBe DDAY_DETAIL_ANNIVERSARY
        composeRule.ddayDetailClickUpdate()
        composeRule.ddayDetailClickDelete()
        composeRule.onNodeWithText(DDAY_FORM_DEFAULT_TAG_LABEL).assertExists()
        verify(exactly = 1) { environment.ddayViewModel.update(input = any()) }
        verify(exactly = 1) { environment.ddayViewModel.delete() }
    }

    @Test
    fun `TC-1bb084c7-047c-40cc-9d22-ca00c1e3843e 종료일을 고르면 입력한 그대로의 내용으로 수정을 요청한다`() {
        assertUpdateInput(isEndDateCleared = false)
    }

    @Test
    fun `TC-1bb084c7-047c-40cc-9d22-ca00c1e3843e 종료일을 지우면 종료일 없이 수정을 요청한다`() {
        assertUpdateInput(isEndDateCleared = true)
    }

    private fun assertUpdateInput(isEndDateCleared: Boolean) {
        val environment = DDayDetailTestEnvironment(detail = ddayDetailOf(countsBaseDay = true, endDate = LocalDate(2029, 3, 3)))
        val colorB = ddayDetailOtherColor(environment.detail.color)
        setScreen(environment)
        composeRule.ddayTitleInput().performTextReplacement(SPACED_TITLE)
        composeRule.ddayDescriptionInput().performTextReplacement(MULTILINE_DESCRIPTION)
        composeRule.ddayDetailChangeColor(fromHex = environment.detail.color.colorHex(), toHex = colorB.colorHex())
        composeRule.ddayPickDate(from = LocalDate(2025, 1, 15), to = LocalDate(2025, 2, 1))
        composeRule.ddayToggleCountsBaseDay()
        if (isEndDateCleared) {
            composeRule.onNodeWithContentDescription(DDAY_FORM_DEFAULT_END_DATE_CLEAR).performSemanticsAction(SemanticsActions.OnClick)
            composeRule.waitForIdle()
        } else {
            composeRule.ddayPickDate(from = LocalDate(2029, 3, 3), to = LocalDate(2030, 1, 15))
        }
        composeRule.ddaySelectOverflowPolicy(currentLabel = DDAY_FORM_DEFAULT_OVERFLOW_SKIP, label = DDAY_FORM_DEFAULT_OVERFLOW_NEXT_MONTH)

        composeRule.ddayDetailClickUpdate()

        verify(exactly = 1) {
            environment.ddayViewModel.update(
                input =
                    DDayInput(
                        title = SPACED_TITLE,
                        description = MULTILINE_DESCRIPTION,
                        color = colorB,
                        baseDate = LocalDate(2025, 2, 1),
                        countsBaseDay = false,
                        repeatList = listOf(DDayRepeatInput(number = 100, unit = DDayRepeatUnit.DAY)),
                        endDate = if (isEndDateCleared) null else LocalDate(2030, 1, 15),
                        overflowPolicy = DDayOverflowPolicy.NEXT_MONTH_FIRST_DAY,
                    ),
            )
        }
    }

    @Test
    fun `TC-bc7c4599-5f11-4d6e-92f4-ceb456234b4c 고친 간격 모음이 기록될 값으로 수정을 요청한다`() {
        val environment = DDayDetailTestEnvironment(detail = ddayDetailOf(repeats = setOf(day(100), month(12))))
        val inputList = mutableListOf<DDayInput>()
        every { environment.ddayViewModel.update(input = capture(inputList)) } returns Unit
        setScreen(environment)
        val caseList =
            listOf(
                listOf("2" to DDAY_FORM_DEFAULT_UNIT_YEAR) to setOf(month(24)),
                listOf("100" to DDAY_FORM_DEFAULT_UNIT_DAY, "1" to DDAY_FORM_DEFAULT_UNIT_YEAR, "3" to DDAY_FORM_DEFAULT_UNIT_MONTH) to
                    setOf(day(100), month(12), month(3)),
                listOf("0050" to DDAY_FORM_DEFAULT_UNIT_DAY) to setOf(day(50)),
                listOf(
                    "1" to DDAY_FORM_DEFAULT_UNIT_YEAR,
                    "12" to DDAY_FORM_DEFAULT_UNIT_MONTH,
                    "100" to DDAY_FORM_DEFAULT_UNIT_DAY,
                    "100" to DDAY_FORM_DEFAULT_UNIT_DAY,
                ) to setOf(month(12), day(100)),
                emptyList<Pair<String, String>>() to emptySet(),
            )

        caseList.forEach { (repeatList, _) ->
            repeat(composeRule.ddayRepeatList().size) { composeRule.ddayRemoveRepeat(position = 1) }
            repeatList.forEach { (number, unit) -> composeRule.ddayAddRepeatOf(number = number, unitLabel = unit) }
            // 기록 값이 저장된 값과 같아 수정 동작이 없는 경우에도 같은 요청 경로를 쓰도록 제목을 바꿔 둔다.
            composeRule.ddayTitleInput().performTextReplacement(DDAY_DETAIL_ANNIVERSARY + inputList.size)
            composeRule.waitForIdle()
            composeRule.ddayDetailClickUpdate()
        }

        inputList.map { input -> input.repeatList.toRepeatSetOrNull() } shouldBe caseList.map { (_, record) -> record }
    }

    @Test
    fun `TC-7d307869-9394-4adf-879d-6c68f3d11a1c 화면이 재생성되어도 고친 입력과 수정 동작을 유지한다`() {
        assertEditRestored(restore = RestoreKind.RECREATE)
    }

    @Test
    fun `TC-7d307869-9394-4adf-879d-6c68f3d11a1c 백그라운드에 다녀와도 고친 입력과 수정 동작을 유지한다`() {
        assertEditRestored(restore = RestoreKind.BACKGROUND)
    }

    @Test
    fun `TC-7d307869-9394-4adf-879d-6c68f3d11a1c 메모리 정리 뒤 복원해도 고친 입력과 수정 동작을 유지한다`() {
        assertEditRestored(restore = RestoreKind.PROCESS_DEATH)
    }

    private fun assertEditRestored(restore: RestoreKind) {
        val environment =
            DDayDetailTestEnvironment(
                detail = ddayDetailOf(countsBaseDay = true, endDate = null, overflowPolicy = DDayOverflowPolicy.CLAMP_TO_MONTH_END),
            )
        val colorB = ddayDetailOtherColor(environment.detail.color)
        val restorationTester = StateRestorationTester(composeRule)
        val lifecycleOwner = androidx.lifecycle.testing.TestLifecycleOwner(androidx.lifecycle.Lifecycle.State.RESUMED)
        restorationTester.setContent {
            androidx.compose.runtime.CompositionLocalProvider(androidx.lifecycle.compose.LocalLifecycleOwner provides lifecycleOwner) {
                // 메모리 정리 뒤에는 ViewModel도 새로 만들어지므로, 복원할 때마다 새 대역을 만든다.
                val viewModel =
                    if (restore == RestoreKind.PROCESS_DEATH) {
                        androidx.compose.runtime.remember { environment.newViewModel() }
                    } else {
                        environment.ddayViewModel
                    }
                DDayDetailTestScreen(environment = environment, viewModel = viewModel)
            }
        }
        composeRule.waitForIdle()
        composeRule.ddayTitleInput().performTextReplacement(DDAY_DETAIL_ANNIVERSARY)
        composeRule.ddayDescriptionInput().performTextReplacement(TWO)
        composeRule.ddayDetailChangeColor(fromHex = environment.detail.color.colorHex(), toHex = colorB.colorHex())
        composeRule.ddayPickDate(from = LocalDate(2025, 1, 15), to = LocalDate(2025, 2, 1))
        composeRule.ddayToggleCountsBaseDay()
        composeRule.ddayRemoveRepeat(position = 1)
        composeRule.ddayAddRepeatOf(number = "1", unitLabel = DDAY_FORM_DEFAULT_UNIT_YEAR)
        composeRule.ddayAddRepeatOf(number = "0100", unitLabel = DDAY_FORM_DEFAULT_UNIT_DAY)
        composeRule.ddayAddRepeatOf(number = "", unitLabel = DDAY_FORM_DEFAULT_UNIT_MONTH)
        composeRule.ddayOpenEndDatePickerNotSet()
        composeRule.ddayTypePickerDate(LocalDate(2030, 1, 15))
        composeRule.ddayConfirmDatePicker()
        composeRule.ddaySelectOverflowPolicy(currentLabel = DDAY_FORM_DEFAULT_OVERFLOW_CLAMP, label = DDAY_FORM_DEFAULT_OVERFLOW_NEXT_MONTH)

        val viewModelCount = environment.viewModelList.size
        when (restore) {
            RestoreKind.BACKGROUND -> {
                composeRule.runOnIdle { lifecycleOwner.currentState = androidx.lifecycle.Lifecycle.State.CREATED }
                composeRule.runOnIdle { lifecycleOwner.currentState = androidx.lifecycle.Lifecycle.State.RESUMED }
            }

            RestoreKind.RECREATE, RestoreKind.PROCESS_DEATH -> restorationTester.emulateSavedInstanceStateRestore()
        }
        composeRule.waitForIdle()

        if (restore == RestoreKind.PROCESS_DEATH) environment.viewModelList.size shouldBe viewModelCount + 1
        composeRule.ddayTitleInput().ddayEditableText() shouldBe DDAY_DETAIL_ANNIVERSARY
        composeRule.ddayDescriptionInput().ddayEditableText() shouldBe TWO
        composeRule.ddayDisplayedColorHex() shouldBe colorB.colorHex()
        composeRule.ddayDateButton(LocalDate(2025, 2, 1)).assertExists()
        composeRule.ddayIsCountsBaseDayOn() shouldBe false
        composeRule.ddayRepeatList() shouldBe
            listOf("1" to DDAY_FORM_DEFAULT_UNIT_YEAR, "0100" to DDAY_FORM_DEFAULT_UNIT_DAY, "" to DDAY_FORM_DEFAULT_UNIT_MONTH)
        composeRule.ddayDateButton(LocalDate(2030, 1, 15)).assertExists()
        composeRule.ddayOverflowPolicyButton(DDAY_FORM_DEFAULT_OVERFLOW_NEXT_MONTH).assertExists()
        composeRule.onNode(ddayDetailUpdateButton()).assertExists()
    }

    @Test
    fun `TC-d8055b68-9471-48d5-8498-6895553ba607 기준일 고르기를 연 채 화면이 재생성되어도 열린 채 남는다`() {
        assertPickerRestored(isBaseDatePicker = true, isRecreate = true)
    }

    @Test
    fun `TC-d8055b68-9471-48d5-8498-6895553ba607 종료일 고르기를 연 채 화면이 재생성되어도 열린 채 남는다`() {
        assertPickerRestored(isBaseDatePicker = false, isRecreate = true)
    }

    @Test
    fun `TC-d8055b68-9471-48d5-8498-6895553ba607 기준일 고르기를 연 채 백그라운드에 다녀와도 열린 채 남는다`() {
        assertPickerRestored(isBaseDatePicker = true, isRecreate = false)
    }

    @Test
    fun `TC-d8055b68-9471-48d5-8498-6895553ba607 종료일 고르기를 연 채 백그라운드에 다녀와도 열린 채 남는다`() {
        assertPickerRestored(isBaseDatePicker = false, isRecreate = false)
    }

    private fun assertPickerRestored(
        isBaseDatePicker: Boolean,
        isRecreate: Boolean,
    ) {
        val environment = DDayDetailTestEnvironment()
        val restorationTester = StateRestorationTester(composeRule)
        val lifecycleOwner = androidx.lifecycle.testing.TestLifecycleOwner(androidx.lifecycle.Lifecycle.State.RESUMED)
        restorationTester.setContent {
            androidx.compose.runtime.CompositionLocalProvider(androidx.lifecycle.compose.LocalLifecycleOwner provides lifecycleOwner) {
                DDayDetailTestScreen(environment = environment)
            }
        }
        composeRule.waitForIdle()
        val date = if (isBaseDatePicker) LocalDate(2025, 1, 15) else LocalDate(2030, 1, 15)
        composeRule.ddayOpenDatePicker(date)
        composeRule.ddayPickerSelection(date).assertExists()

        if (isRecreate) {
            restorationTester.emulateSavedInstanceStateRestore()
        } else {
            composeRule.runOnIdle { lifecycleOwner.currentState = androidx.lifecycle.Lifecycle.State.CREATED }
            composeRule.runOnIdle { lifecycleOwner.currentState = androidx.lifecycle.Lifecycle.State.RESUMED }
        }
        composeRule.waitForIdle()

        composeRule.onNodeWithText(DEFAULT_DATE_PICKER_CONFIRM).assertExists()
        composeRule.ddayPickerSelection(date).assertExists()
    }

    @Test
    fun `TC-51916629-6b38-404d-9272-dbb3bd571bd0 화면이 재생성되면 수정 성공 피드백을 다시 표시하지 않는다`() {
        assertFeedbackNotRestored(effect = DDayDetailEffect.UpdateSucceeded, message = DDAY_DETAIL_DEFAULT_UPDATE_SUCCEEDED)
    }

    @Test
    fun `TC-51916629-6b38-404d-9272-dbb3bd571bd0 화면이 재생성되면 반복 간격 미입력 안내를 다시 표시하지 않는다`() {
        assertFeedbackNotRestored(effect = DDayDetailEffect.RepeatBlank, message = DDAY_FORM_DEFAULT_REPEAT_BLANK)
    }

    private fun assertFeedbackNotRestored(
        effect: DDayDetailEffect,
        message: String,
    ) {
        val environment = DDayDetailTestEnvironment()
        val restorationTester = StateRestorationTester(composeRule)
        restorationTester.setContent { DDayDetailTestScreen(environment = environment) }
        composeRule.waitForIdle()
        environment.effect.trySend(effect)
        composeRule.waitForIdle()
        composeRule.onNodeWithText(message).assertExists()

        restorationTester.emulateSavedInstanceStateRestore()
        composeRule.waitForIdle()

        composeRule.onNodeWithText(message).assertDoesNotExist()
    }

    @Test
    fun `TC-758e9792-5a85-4288-ba68-3c2afdc171fd 앱을 다시 실행해 진입하면 저장된 내용으로 다시 시작한다`() {
        val environment = DDayDetailTestEnvironment()
        var launchCount by androidx.compose.runtime.mutableIntStateOf(0)
        // 앱을 다시 실행하면 저장해 둔 화면 상태가 없고 ViewModel도 새로 만들어지므로, 새 키로 저장 상태 없이 다시 구성한다.
        composeRule.setContent {
            androidx.compose.runtime.key(launchCount) {
                val viewModel = androidx.compose.runtime.remember { environment.newViewModel() }
                DDayDetailTestScreen(environment = environment, viewModel = viewModel)
            }
        }
        composeRule.waitForIdle()
        composeRule.ddayTitleInput().performTextReplacement(DDAY_DETAIL_ANNIVERSARY)
        composeRule.ddayPickDate(from = LocalDate(2025, 1, 15), to = LocalDate(2025, 2, 1))
        composeRule.ddayInputRepeatNumber(position = 1, number = "200")

        composeRule.runOnIdle { launchCount += 1 }
        composeRule.waitForIdle()

        composeRule.ddayTitleInput().ddayEditableText() shouldBe DDAY_DETAIL_MARRIAGE
        composeRule.ddayDateButton(LocalDate(2025, 1, 15)).assertExists()
        composeRule.ddayRepeatList() shouldBe listOf("100" to DDAY_FORM_DEFAULT_UNIT_DAY)
        composeRule.onNode(ddayDetailUpdateButton()).assertDoesNotExist()
    }

    @Test
    fun `TC-caab3ed3-8d62-4023-8bb0-8b699d60fe40 수정을 반영하거나 저장된 제목이 바뀌거나 삭제에 실패해도 보던 위치를 유지한다`() {
        val environment = DDayDetailTestEnvironment(detail = ddayDetailOf(repeats = (1..SCROLL_REPEAT_COUNT).map(::day).toSet()))
        setScreen(environment)
        val target = composeRule.ddayRepeatNumberInput(SCROLL_REPEAT_COUNT)
        target.performScrollTo()
        composeRule.waitForIdle()
        composeRule.ddayInputRepeatNumber(position = SCROLL_REPEAT_COUNT, number = "${SCROLL_REPEAT_COUNT}0")
        val top = target.ddayDetailTop()
        composeRule.ddayTitleInput().assertIsNotDisplayed()

        composeRule.ddayDetailClickUpdate()
        environment.uiState.value = environment.content(isUpdateInProgress = true)
        composeRule.waitForIdle()
        environment.effect.trySend(DDayDetailEffect.UpdateSucceeded)
        environment.uiState.value = environment.content()
        composeRule.waitForIdle()
        target.ddayDetailTop() shouldBe top
        composeRule.ddayTitleInput().assertIsNotDisplayed()

        environment.uiState.value = environment.content(detail = environment.detail.copy(title = OTHER_TITLE))
        composeRule.waitForIdle()
        target.ddayDetailTop() shouldBe top
        composeRule.ddayTitleInput().assertIsNotDisplayed()

        composeRule.onNode(ddayDetailDeleteButton()).performClick()
        environment.uiState.value = environment.content(detail = environment.detail.copy(title = OTHER_TITLE), isDeleteInProgress = true)
        composeRule.waitForIdle()
        environment.uiState.value = environment.content(detail = environment.detail.copy(title = OTHER_TITLE))
        composeRule.waitForIdle()
        target.ddayDetailTop() shouldBe top
        composeRule.ddayTitleInput().assertIsNotDisplayed()
    }

    @Test
    fun `TC-6685f6a8-7faf-4e91-bb4a-ff401443c7b8 뒤로가기를 선택하면 돌아가는 동작이 한 번 실행되고 수정은 요청하지 않는다`() {
        val environment = DDayDetailTestEnvironment()
        setScreen(environment)
        composeRule.ddayTitleInput().performTextReplacement(DDAY_DETAIL_ANNIVERSARY)
        composeRule.ddayInputRepeatNumber(position = 1, number = "200")

        composeRule.onNodeWithContentDescription(DDAY_FORM_DEFAULT_NAVIGATE_UP).performClick()
        composeRule.waitForIdle()

        environment.navigateUpCount shouldBe 1
        verify(exactly = 0) { environment.ddayViewModel.update(input = any()) }
    }

    private fun setScreen(environment: DDayDetailTestEnvironment) {
        composeRule.setContent { DDayDetailTestScreen(environment = environment) }
        composeRule.waitForIdle()
    }

    private fun expectedInput(
        environment: DDayDetailTestEnvironment,
        title: String = environment.detail.title,
        description: String = environment.detail.description,
        baseDate: LocalDate = environment.detail.baseDate,
    ): DDayInput =
        DDayInput(
            title = title,
            description = description,
            color = environment.detail.color,
            baseDate = baseDate,
            countsBaseDay = environment.detail.countsBaseDay,
            repeatList = listOf(DDayRepeatInput(number = 100, unit = DDayRepeatUnit.DAY)),
            endDate = environment.detail.endDate,
            overflowPolicy = environment.detail.overflowPolicy,
        )

    private enum class RestoreKind { RECREATE, BACKGROUND, PROCESS_DEATH }

    private companion object {
        const val COLOR_INPUT = "ColorInput"
        const val DEFAULT_DESCRIPTION_INPUT_TAB = "Input"
        const val DEFAULT_DESCRIPTION_PREVIEW_TAB = "Preview"
        const val DEFAULT_CLEAR_TEXT = "Clear text"
        const val DEFAULT_PREVIOUS_MONTH = "Change to previous month"
        const val DEFAULT_DATE_PICKER_CONFIRM = "OK"
        const val MONTHS_FROM_JANUARY_TO_JUNE = 5
        const val SCROLL_REPEAT_COUNT = 20
        const val MY_TITLE = "내 제목"
        const val OTHER_TITLE = "다른 제목"
        const val US_TWO = "우리 둘"
        const val TWO = "둘"
        const val SPACED_TITLE = " 결혼 "
        const val MULTILINE_DESCRIPTION = "우리\n둘"
        val RULE_BASE_DATE = LocalDate(2026, 1, 15)
        val RULE_END_DATE = LocalDate(2026, 6, 30)

        fun day(days: Int): DateTimeUnit.DateBased = DateTimeUnit.DayBased(days = days)

        fun month(months: Int): DateTimeUnit.DateBased = DateTimeUnit.MonthBased(months = months)
    }
}
