package io.github.taetae98coding.diary.feature.dday.ui.add

import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTextReplacement
import io.github.taetae98coding.diary.compose.tag.entity.EntityTagInputUiState
import io.github.taetae98coding.diary.core.testing.tag.tag
import io.github.taetae98coding.diary.feature.dday.ui.form.DDAY_FORM_DEFAULT_END_DATE_CLEAR
import io.github.taetae98coding.diary.feature.dday.ui.form.DDAY_FORM_DEFAULT_OVERFLOW_CLAMP
import io.github.taetae98coding.diary.feature.dday.ui.form.DDAY_FORM_DEFAULT_OVERFLOW_NEXT_MONTH
import io.github.taetae98coding.diary.feature.dday.ui.form.DDAY_FORM_DEFAULT_OVERFLOW_SKIP
import io.github.taetae98coding.diary.feature.dday.ui.form.DDAY_FORM_DEFAULT_PRESET_BABY
import io.github.taetae98coding.diary.feature.dday.ui.form.DDAY_FORM_DEFAULT_PRESET_COUPLE
import io.github.taetae98coding.diary.feature.dday.ui.form.DDAY_FORM_DEFAULT_PRESET_CUSTOM
import io.github.taetae98coding.diary.feature.dday.ui.form.DDAY_FORM_DEFAULT_PRESET_EXAM
import io.github.taetae98coding.diary.feature.dday.ui.form.DDAY_FORM_DEFAULT_UNIT_DAY
import io.github.taetae98coding.diary.feature.dday.ui.form.DDAY_FORM_DEFAULT_UNIT_MONTH
import io.github.taetae98coding.diary.feature.dday.ui.form.DDAY_FORM_DEFAULT_UNIT_YEAR
import io.github.taetae98coding.diary.feature.dday.ui.form.DDayFormState
import io.github.taetae98coding.diary.feature.dday.ui.form.DDayFormTestTheme
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
import io.github.taetae98coding.diary.feature.dday.ui.form.rememberDDayAddFormState
import io.github.taetae98coding.diary.feature.dday.ui.resetAndroidUiDispatcher
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next
import kotlinx.datetime.LocalDate
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * DDayAdd 화면의 입력 조작을 화면의 상태 홀더와 함께 배치해 확인한다. 기준일을 정해 두어야 하는 케이스가 많아 기준일을 지정해 상태를 만든다.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w480dp-h1200dp")
class DDayAddFormTest {
    @get:Rule
    val composeRule = createComposeRule()

    private lateinit var state: DDayFormState
    private val addEventList = mutableListOf<DDayAddScaffoldEvent>()

    @Before
    fun setUp() {
        resetAndroidUiDispatcher()
    }

    @Test
    fun `TC-9e114b93-a844-4db8-8737-161a29947f0e 연인 프리셋을 고르면 네 입력이 그 값으로 바뀐다`() {
        assertPreset(label = DDAY_FORM_DEFAULT_PRESET_COUPLE, repeatList = listOf("100" to DDAY_FORM_DEFAULT_UNIT_DAY, "1" to DDAY_FORM_DEFAULT_UNIT_YEAR))
    }

    @Test
    fun `TC-9e114b93-a844-4db8-8737-161a29947f0e 시험 프리셋을 고르면 네 입력이 그 값으로 바뀐다`() {
        assertPreset(label = DDAY_FORM_DEFAULT_PRESET_EXAM, repeatList = emptyList())
    }

    @Test
    fun `TC-9e114b93-a844-4db8-8737-161a29947f0e 아기 프리셋을 고르면 네 입력이 그 값으로 바뀐다`() {
        assertPreset(
            label = DDAY_FORM_DEFAULT_PRESET_BABY,
            repeatList = listOf("100" to DDAY_FORM_DEFAULT_UNIT_DAY, "1" to DDAY_FORM_DEFAULT_UNIT_MONTH, "1" to DDAY_FORM_DEFAULT_UNIT_YEAR),
        )
    }

    @Test
    fun `TC-9e114b93-a844-4db8-8737-161a29947f0e 직접 입력 프리셋을 고르면 네 입력이 그 값으로 바뀐다`() {
        assertPreset(label = DDAY_FORM_DEFAULT_PRESET_CUSTOM, repeatList = emptyList())
    }

    // 처음 상태에서 프리셋을 고르므로, 고르기 전에 네 입력을 직접 입력 값과 다르게 바꿔 두어 실제로 바뀌는지 드러낸다.
    private fun assertPreset(
        label: String,
        repeatList: List<Pair<String, String>>,
    ) {
        setForm(baseDate = BASE_DATE)
        editRuleInput()

        composeRule.onNodeWithText(label).performClick()
        composeRule.waitForIdle()

        // 확인을 되묻는 다이얼로그 없이 바로 바뀐다.
        composeRule.onNodeWithText(DDAY_FORM_DEFAULT_DATE_PICKER_CONFIRM_TEXT).assertDoesNotExist()
        composeRule.ddayIsCountsBaseDayOn() shouldBe true
        composeRule.ddayRepeatList() shouldBe repeatList
        composeRule.ddayEndDateNotSetButton().assertExists()
        composeRule.ddayOverflowPolicyButton(DDAY_FORM_DEFAULT_OVERFLOW_CLAMP).assertExists()
    }

    @Test
    fun `TC-e5628fbe-673f-4e85-872e-97fc8393757c 프리셋은 네 입력만 덮어쓰고 제목, 설명, 컬러, 기준일과 태그는 그대로다`() {
        val tag = Arb.tag(title = TAG_TITLE).next()
        setForm(baseDate = LocalDate(2025, 1, 15), tagUiState = EntityTagInputUiState(tagList = listOf(tag)))
        composeRule.ddayTitleInput().performTextInput(MARRIAGE)
        composeRule.ddayDescriptionInput().performTextInput(US)
        val color = composeRule.ddayDisplayedColorHex()
        editRuleInput()

        composeRule.onNodeWithText(DDAY_FORM_DEFAULT_PRESET_COUPLE).performClick()
        composeRule.waitForIdle()

        composeRule.ddayIsCountsBaseDayOn() shouldBe true
        composeRule.ddayRepeatList() shouldBe listOf("100" to DDAY_FORM_DEFAULT_UNIT_DAY, "1" to DDAY_FORM_DEFAULT_UNIT_YEAR)
        composeRule.ddayEndDateNotSetButton().assertExists()
        composeRule.ddayOverflowPolicyButton(DDAY_FORM_DEFAULT_OVERFLOW_CLAMP).assertExists()
        composeRule.ddayTitleInput().ddayEditableText() shouldBe MARRIAGE
        composeRule.ddayDescriptionInput().ddayEditableText() shouldBe US
        composeRule.ddayDisplayedColorHex() shouldBe color
        composeRule.ddayDateButton(LocalDate(2025, 1, 15)).assertExists()
        composeRule.onNodeWithText(TAG_TITLE).assertExists()
    }

    @Test
    fun `TC-599e5f12-eb4e-4d0f-b1ed-5dc33fa4de6e 프리셋을 골라도 디데이가 추가되지 않고 고른 프리셋을 계속 표시하지 않는다`() {
        setForm(baseDate = BASE_DATE)
        composeRule.ddayTitleInput().performTextInput(MARRIAGE)

        composeRule.onNodeWithText(DDAY_FORM_DEFAULT_PRESET_BABY).performClick()
        composeRule.waitForIdle()

        addEventList shouldBe emptyList()
        listOf(DDAY_FORM_DEFAULT_PRESET_COUPLE, DDAY_FORM_DEFAULT_PRESET_EXAM, DDAY_FORM_DEFAULT_PRESET_BABY, DDAY_FORM_DEFAULT_PRESET_CUSTOM).forEach { label ->
            // 칩은 선택 상태를 갖지 않으므로 고른 뒤에도 선택됨이나 켜짐을 알리지 않는다.
            composeRule
                .onNode(hasText(label) and hasClickAction())
                .assert(SemanticsMatcher.keyNotDefined(SemanticsProperties.Selected) and SemanticsMatcher.keyNotDefined(SemanticsProperties.ToggleableState))
        }
    }

    @Test
    fun `TC-a14c44e4-089c-47f6-9251-4cadeb183a9a 프리셋으로 채운 값을 모두 고칠 수 있다`() {
        setForm(baseDate = BASE_DATE)
        composeRule.onNodeWithText(DDAY_FORM_DEFAULT_PRESET_BABY).performClick()
        composeRule.waitForIdle()

        composeRule.ddayToggleCountsBaseDay()
        composeRule.ddayRemoveRepeat(position = 2)
        composeRule.ddayInputRepeatNumber(position = 1, number = "200")
        composeRule.ddayOpenEndDatePickerNotSet()
        composeRule.ddayConfirmDatePicker()
        composeRule.ddaySelectOverflowPolicy(currentLabel = DDAY_FORM_DEFAULT_OVERFLOW_CLAMP, label = DDAY_FORM_DEFAULT_OVERFLOW_NEXT_MONTH)

        composeRule.ddayIsCountsBaseDayOn() shouldBe false
        composeRule.ddayRepeatList() shouldBe listOf("200" to DDAY_FORM_DEFAULT_UNIT_DAY, "1" to DDAY_FORM_DEFAULT_UNIT_YEAR)
        composeRule.onAllNodesWithText(ddayDisplayText(BASE_DATE)).fetchSemanticsNodes().size shouldBe 2
        composeRule.ddayOverflowPolicyButton(DDAY_FORM_DEFAULT_OVERFLOW_NEXT_MONTH).assertExists()
    }

    @Test
    fun `TC-1fff6bf3-9517-4ccf-8fe0-9375c3b60aa6 기준일 고르기는 지금 기준일에서 시작하고 확인하면 오늘보다 앞의 날짜가 기준일이 된다`() {
        assertBaseDatePicked(LocalDate(2020, 5, 1))
    }

    @Test
    fun `TC-1fff6bf3-9517-4ccf-8fe0-9375c3b60aa6 기준일 고르기는 지금 기준일에서 시작하고 확인하면 오늘보다 뒤의 날짜가 기준일이 된다`() {
        assertBaseDatePicked(LocalDate(2030, 12, 31))
    }

    private fun assertBaseDatePicked(date: LocalDate) {
        setForm(baseDate = BASE_DATE)

        composeRule.ddayOpenDatePicker(BASE_DATE)
        composeRule.ddayPickerSelection(BASE_DATE).assertExists()
        composeRule.ddayTypePickerDate(date)
        composeRule.ddayConfirmDatePicker()

        composeRule.ddayDateButton(date).assertExists()
        composeRule.ddayDateButton(BASE_DATE).assertDoesNotExist()
    }

    @Test
    fun `TC-e574c021-247b-48ae-ad2f-8c37ca8db7fb 기준일 고르기를 취소하면 고르기 전 기준일을 유지한다`() {
        setForm(baseDate = BASE_DATE)

        composeRule.ddayOpenDatePicker(BASE_DATE)
        composeRule.ddayPickerDayCell(LocalDate(2026, 4, 1)).assertDoesNotExist()
        composeRule.ddayTypePickerDate(LocalDate(2026, 4, 1))
        composeRule.ddayCancelDatePicker()

        composeRule.ddayDateButton(BASE_DATE).assertExists()
        composeRule.ddayDateButton(LocalDate(2026, 4, 1)).assertDoesNotExist()
    }

    @Test
    fun `TC-61a2f412-4358-4fb4-a678-2c6528332dfb 기준일은 지울 수 없다`() {
        setForm(baseDate = BASE_DATE, endDate = LocalDate(2026, 12, 31))

        // 지우기 버튼은 종료일에만 있고 기준일에는 없다.
        composeRule.onNodeWithContentDescription(DDAY_FORM_DEFAULT_END_DATE_CLEAR).assertExists()
        composeRule.onAllNodes(clearAction()).fetchSemanticsNodes().size shouldBe 1
        composeRule.ddayOpenDatePicker(BASE_DATE)
        // 날짜 고르기에는 확인과 취소만 있고 날짜를 비우는 동작은 없다.
        composeRule.onAllNodes(clearAction()).fetchSemanticsNodes().size shouldBe 1
        composeRule.ddayCancelDatePicker()
        composeRule.ddayDateButton(BASE_DATE).assertExists()
    }

    @Test
    fun `TC-1ab9dbae-8463-4725-9531-12f55af8432d 기준일을 종료일보다 뒤로 고르면 종료일이 기준일과 같은 날짜로 맞춰진다`() {
        assertEndDateAfterBaseDate(endDate = LocalDate(2026, 6, 30), selected = LocalDate(2026, 7, 10), expected = LocalDate(2026, 7, 10))
    }

    @Test
    fun `TC-1ab9dbae-8463-4725-9531-12f55af8432d 기준일을 종료일과 같은 날로 고르면 종료일은 그대로다`() {
        assertEndDateAfterBaseDate(endDate = LocalDate(2026, 6, 30), selected = LocalDate(2026, 6, 30), expected = LocalDate(2026, 6, 30))
    }

    @Test
    fun `TC-1ab9dbae-8463-4725-9531-12f55af8432d 기준일을 종료일보다 앞으로 고르면 종료일은 그대로다`() {
        assertEndDateAfterBaseDate(endDate = LocalDate(2026, 6, 30), selected = LocalDate(2026, 3, 1), expected = LocalDate(2026, 6, 30))
    }

    @Test
    fun `TC-1ab9dbae-8463-4725-9531-12f55af8432d 종료일이 없으면 기준일을 골라도 종료일은 없다`() {
        assertEndDateAfterBaseDate(endDate = null, selected = LocalDate(2026, 7, 10), expected = null)
    }

    private fun assertEndDateAfterBaseDate(
        endDate: LocalDate?,
        selected: LocalDate,
        expected: LocalDate?,
    ) {
        val baseDate = LocalDate(2026, 1, 15)
        setForm(baseDate = baseDate, endDate = endDate)

        composeRule.ddayPickDate(from = baseDate, to = selected)

        if (expected == null) {
            composeRule.ddayEndDateNotSetButton().assertExists()
            composeRule.ddayDateButton(selected).assertExists()
        } else {
            // 기준일과 종료일이 같으면 같은 날짜 버튼이 두 개 놓인다.
            val expectedCount = if (expected == selected) 2 else 1
            composeRule.onAllNodes(hasText(ddayDisplayText(expected)) and hasClickAction()).fetchSemanticsNodes().size shouldBe expectedCount
            if (expected != selected) composeRule.ddayDateButton(selected).assertExists()
        }
    }

    @Test
    fun `TC-414d134e-d848-44cc-88c0-adcf632c57d4 기준일을 1일로 세기를 켜거나 끌 수 있다`() {
        setForm(baseDate = BASE_DATE)
        composeRule.ddayIsCountsBaseDayOn() shouldBe true

        composeRule.ddayToggleCountsBaseDay()
        composeRule.ddayIsCountsBaseDayOn() shouldBe false

        composeRule.ddayToggleCountsBaseDay()
        composeRule.ddayIsCountsBaseDayOn() shouldBe true
    }

    @Test
    fun `TC-db083202-8779-4ebe-b816-c4e7c7d42742 간격을 추가하면 마지막 자리에 빈 일 간격이 놓이고 제목과 초점은 그대로다`() {
        setForm(baseDate = BASE_DATE)
        composeRule.onNodeWithText(DDAY_FORM_DEFAULT_PRESET_COUPLE).performClick()
        composeRule.ddayTitleInput().performTextInput(MARRIAGE)
        composeRule.waitForIdle()

        composeRule.ddayAddRepeat()

        composeRule.ddayRepeatList() shouldBe listOf("100" to DDAY_FORM_DEFAULT_UNIT_DAY, "1" to DDAY_FORM_DEFAULT_UNIT_YEAR, "" to DDAY_FORM_DEFAULT_UNIT_DAY)
        composeRule.ddayTitleInput().ddayEditableText() shouldBe MARRIAGE
        composeRule.ddayTitleInput().assertIsFocused()
    }

    @Test
    fun `TC-1a8fe5c1-5688-42c5-93a1-684cda3ec7f1 간격을 삭제하면 그 간격만 사라지고 제목과 초점은 그대로다`() {
        setForm(baseDate = BASE_DATE)
        composeRule.onNodeWithText(DDAY_FORM_DEFAULT_PRESET_BABY).performClick()
        composeRule.ddayTitleInput().performTextInput(MARRIAGE)
        composeRule.waitForIdle()

        composeRule.ddayRemoveRepeat(position = 2)

        composeRule.onNodeWithText(DDAY_FORM_DEFAULT_DATE_PICKER_CONFIRM_TEXT).assertDoesNotExist()
        composeRule.ddayRepeatList() shouldBe listOf("100" to DDAY_FORM_DEFAULT_UNIT_DAY, "1" to DDAY_FORM_DEFAULT_UNIT_YEAR)
        composeRule.ddayTitleInput().ddayEditableText() shouldBe MARRIAGE
        composeRule.ddayTitleInput().assertIsFocused()
    }

    @Test
    fun `TC-0dd93d8f-5bba-44a8-b150-5cc18aeb8d89 단위를 바꿔도 입력한 숫자는 그대로다`() {
        setForm(baseDate = BASE_DATE)
        composeRule.ddayAddRepeatOf(number = "3", unitLabel = DDAY_FORM_DEFAULT_UNIT_DAY)

        composeRule.ddaySelectRepeatUnit(position = 1, unitLabel = DDAY_FORM_DEFAULT_UNIT_MONTH)
        composeRule.ddayRepeatList() shouldBe listOf("3" to DDAY_FORM_DEFAULT_UNIT_MONTH)

        composeRule.ddaySelectRepeatUnit(position = 1, unitLabel = DDAY_FORM_DEFAULT_UNIT_YEAR)
        composeRule.ddayRepeatList() shouldBe listOf("3" to DDAY_FORM_DEFAULT_UNIT_YEAR)
    }

    @Test
    fun `TC-83fa5809-f191-43cb-b1ac-f817751108ec 간격의 숫자에는 반각 숫자만 다섯 자리까지 반영한다`() {
        setForm(baseDate = BASE_DATE)
        composeRule.ddayAddRepeat()

        listOf(
            Triple("", "100", "100"),
            Triple("", "0100", "0100"),
            Triple("12", "a", "12"),
            Triple("12", "-", "12"),
            Triple("12", ".", "12"),
            Triple("12", "３", "12"),
            Triple("12345", "6", "12345"),
            Triple("", "99999", "99999"),
        ).forEach { (before, input, expected) ->
            composeRule.ddayRepeatNumberInput(1).performTextReplacement(before)
            composeRule.waitForIdle()

            composeRule.ddayRepeatNumberInput(1).performTextInput(input)
            composeRule.waitForIdle()

            composeRule.ddayRepeatNumberInput(1).ddayEditableText() shouldBe expected
        }
    }

    @Test
    fun `TC-cdd10340-9a47-434a-8831-44dcbbc19ce4 간격의 개수에는 제한이 없다`() {
        setForm(baseDate = BASE_DATE)

        for (number in 1..REPEAT_COUNT) {
            composeRule.ddayAddRepeatOf(number = number.toString(), unitLabel = DDAY_FORM_DEFAULT_UNIT_DAY)
        }

        composeRule.ddayRepeatList() shouldBe (1..REPEAT_COUNT).map { number -> number.toString() to DDAY_FORM_DEFAULT_UNIT_DAY }
        composeRule.onNodeWithContentDescription(REPEAT_ADD).assertIsEnabled()
    }

    @Test
    fun `TC-c4c30a10-b7f6-4a9a-8492-21153722abc1 같은 간격을 여러 번 입력해도 막지 않는다`() {
        setForm(baseDate = BASE_DATE)
        composeRule.ddayAddRepeatOf(number = "100", unitLabel = DDAY_FORM_DEFAULT_UNIT_DAY)

        composeRule.ddayAddRepeatOf(number = "100", unitLabel = DDAY_FORM_DEFAULT_UNIT_DAY)
        composeRule.ddayAddRepeatOf(number = "12", unitLabel = DDAY_FORM_DEFAULT_UNIT_MONTH)
        composeRule.ddayAddRepeatOf(number = "1", unitLabel = DDAY_FORM_DEFAULT_UNIT_YEAR)

        composeRule.ddayRepeatList() shouldBe
            listOf(
                "100" to DDAY_FORM_DEFAULT_UNIT_DAY,
                "100" to DDAY_FORM_DEFAULT_UNIT_DAY,
                "12" to DDAY_FORM_DEFAULT_UNIT_MONTH,
                "1" to DDAY_FORM_DEFAULT_UNIT_YEAR,
            )
        composeRule.onNodeWithText(REPEAT_BLANK).assertDoesNotExist()
        state.snackbarHostState.currentSnackbarData shouldBe null
    }

    @Test
    fun `TC-efa940b0-c550-4dea-b122-7a50f08a9d72 종료일이 없으면 종료일 고르기는 기준일에서 시작한다`() {
        val baseDate = LocalDate(2026, 1, 15)
        setForm(baseDate = baseDate)

        composeRule.ddayOpenEndDatePickerNotSet()

        composeRule.ddayPickerSelection(baseDate).assertExists()
    }

    @Test
    fun `TC-efa940b0-c550-4dea-b122-7a50f08a9d72 종료일이 있으면 종료일 고르기는 종료일에서 시작한다`() {
        val endDate = LocalDate(2026, 12, 31)
        setForm(baseDate = LocalDate(2026, 1, 15), endDate = endDate)

        composeRule.ddayOpenDatePicker(endDate)

        composeRule.ddayPickerSelection(endDate).assertExists()
    }

    @Test
    fun `TC-e67c630d-8f29-4178-9368-d12ce2fac3cd 종료일 고르기에서는 기준일보다 앞의 날짜를 고를 수 없다`() {
        setForm(baseDate = LocalDate(2026, 1, 15))

        composeRule.ddayOpenEndDatePickerNotSet()

        composeRule.ddayPickerDayCell(LocalDate(2026, 1, 14)).assertIsNotEnabled()
        composeRule.ddayPickerDayCell(LocalDate(2026, 1, 15)).assertIsEnabled()
        composeRule.ddayPickerDayCell(LocalDate(2026, 1, 16)).assertIsEnabled()
    }

    @Test
    fun `TC-7b2c569d-3cbc-4081-9fd2-883820002800 종료일이 없을 때 고르고 확인하면 고른 날짜가 종료일이 된다`() {
        assertEndDatePicked(endDate = null, isConfirm = true, expected = LocalDate(2026, 6, 30))
    }

    @Test
    fun `TC-7b2c569d-3cbc-4081-9fd2-883820002800 종료일이 없을 때 고르고 취소하면 종료일이 없다`() {
        assertEndDatePicked(endDate = null, isConfirm = false, expected = null)
    }

    @Test
    fun `TC-7b2c569d-3cbc-4081-9fd2-883820002800 종료일이 있을 때 고르고 확인하면 고른 날짜가 종료일이 된다`() {
        assertEndDatePicked(endDate = LocalDate(2026, 12, 31), isConfirm = true, expected = LocalDate(2026, 6, 30))
    }

    @Test
    fun `TC-7b2c569d-3cbc-4081-9fd2-883820002800 종료일이 있을 때 고르고 취소하면 종료일은 그대로다`() {
        assertEndDatePicked(endDate = LocalDate(2026, 12, 31), isConfirm = false, expected = LocalDate(2026, 12, 31))
    }

    private fun assertEndDatePicked(
        endDate: LocalDate?,
        isConfirm: Boolean,
        expected: LocalDate?,
    ) {
        setForm(baseDate = LocalDate(2026, 1, 15), endDate = endDate)

        if (endDate == null) composeRule.ddayOpenEndDatePickerNotSet() else composeRule.ddayOpenDatePicker(endDate)
        composeRule.ddayTypePickerDate(LocalDate(2026, 6, 30))
        if (isConfirm) composeRule.ddayConfirmDatePicker() else composeRule.ddayCancelDatePicker()

        if (expected == null) {
            composeRule.ddayEndDateNotSetButton().assertExists()
            composeRule.ddayDateButton(LocalDate(2026, 6, 30)).assertDoesNotExist()
        } else {
            composeRule.ddayDateButton(expected).assertExists()
            composeRule.ddayEndDateNotSetButton().assertDoesNotExist()
        }
    }

    @Test
    fun `TC-b0e57a45-6ac8-4893-a1b4-5d0e2b4a3a08 고른 종료일을 지우면 종료일이 없는 상태로 돌아간다`() {
        setForm(baseDate = LocalDate(2026, 1, 15), endDate = LocalDate(2026, 12, 31))

        composeRule.onNodeWithContentDescription(DDAY_FORM_DEFAULT_END_DATE_CLEAR).performSemanticsAction(SemanticsActions.OnClick)
        composeRule.waitForIdle()

        composeRule.ddayEndDateNotSetButton().assertExists()
        composeRule.ddayDateButton(LocalDate(2026, 12, 31)).assertDoesNotExist()
        composeRule.onNodeWithContentDescription(DDAY_FORM_DEFAULT_END_DATE_CLEAR).assertDoesNotExist()
    }

    @Test
    fun `TC-36f0fa0c-fa01-4990-b74d-cc351fe03ccd 반복 간격이 없어도 달력에 없는 날짜 처리를 세 값 중 하나로 고를 수 있다`() {
        assertOverflowPolicySelectable(repeat = null)
    }

    @Test
    fun `TC-36f0fa0c-fa01-4990-b74d-cc351fe03ccd 일 간격만 있어도 달력에 없는 날짜 처리를 세 값 중 하나로 고를 수 있다`() {
        assertOverflowPolicySelectable(repeat = "100" to DDAY_FORM_DEFAULT_UNIT_DAY)
    }

    @Test
    fun `TC-36f0fa0c-fa01-4990-b74d-cc351fe03ccd 개월 간격이 있을 때 달력에 없는 날짜 처리를 세 값 중 하나로 고를 수 있다`() {
        assertOverflowPolicySelectable(repeat = "1" to DDAY_FORM_DEFAULT_UNIT_MONTH)
    }

    private fun assertOverflowPolicySelectable(repeat: Pair<String, String>?) {
        setForm(baseDate = BASE_DATE)
        repeat?.let { (number, unit) -> composeRule.ddayAddRepeatOf(number = number, unitLabel = unit) }

        var current = DDAY_FORM_DEFAULT_OVERFLOW_CLAMP
        listOf(DDAY_FORM_DEFAULT_OVERFLOW_SKIP, DDAY_FORM_DEFAULT_OVERFLOW_NEXT_MONTH, DDAY_FORM_DEFAULT_OVERFLOW_CLAMP).forEach { label ->
            composeRule.ddaySelectOverflowPolicy(currentLabel = current, label = label)

            composeRule.ddayOverflowPolicyButton(label).assertExists()
            current = label
        }
    }

    private fun editRuleInput() {
        composeRule.ddayToggleCountsBaseDay()
        composeRule.ddayAddRepeatOf(number = "3", unitLabel = DDAY_FORM_DEFAULT_UNIT_MONTH)
        composeRule.ddayOpenEndDatePickerNotSet()
        composeRule.ddayConfirmDatePicker()
        composeRule.ddaySelectOverflowPolicy(currentLabel = DDAY_FORM_DEFAULT_OVERFLOW_CLAMP, label = DDAY_FORM_DEFAULT_OVERFLOW_SKIP)
        composeRule.ddayIsCountsBaseDayOn() shouldBe false
        composeRule.ddayRepeatList() shouldBe listOf("3" to DDAY_FORM_DEFAULT_UNIT_MONTH)
        composeRule.ddayEndDateNotSetButton().assertDoesNotExist()
        composeRule.ddayOverflowPolicyButton(DDAY_FORM_DEFAULT_OVERFLOW_SKIP).assertExists()
    }

    private fun setForm(
        baseDate: LocalDate,
        endDate: LocalDate? = null,
        tagUiState: EntityTagInputUiState = EntityTagInputUiState(),
    ) {
        composeRule.setContent {
            DDayFormTestTheme {
                state = rememberDDayAddFormState(initialBaseDate = baseDate)
                DDayAddScaffold(
                    onEvent = addEventList::add,
                    onFormEvent = {},
                    onTagPickerEvent = {},
                    state = state,
                    tagUiStateProvider = { tagUiState },
                )
            }
        }
        composeRule.waitForIdle()
        if (endDate != null) {
            composeRule.runOnIdle { state.ruleState.selectEndDate(date = endDate) }
            composeRule.waitForIdle()
        }
        composeRule.runOnIdle { state.titleState.requestFocus() }
        composeRule.waitForIdle()
    }

    private companion object {
        const val MARRIAGE = "결혼"
        const val US = "우리"
        const val TAG_TITLE = "DDayAddFormTag"
        const val REPEAT_COUNT = 30
        const val REPEAT_ADD = "Add interval"
        const val REPEAT_BLANK = "Please enter a number of 1 or more for each interval."
        const val DDAY_FORM_DEFAULT_DATE_PICKER_CONFIRM_TEXT = "OK"
        val BASE_DATE = LocalDate(2026, 3, 20)

        fun clearAction() = hasClickAction() and (hasContentDescription("Clear", substring = true) or hasText("Clear", substring = true))
    }
}
