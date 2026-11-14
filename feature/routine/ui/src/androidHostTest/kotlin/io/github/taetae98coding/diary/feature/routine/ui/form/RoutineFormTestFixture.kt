package io.github.taetae98coding.diary.feature.routine.ui.form

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.SemanticsNodeInteractionsProvider
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isDialog
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.test.performTextReplacement
import androidx.paging.PagingData
import io.github.taetae98coding.diary.compose.tag.entity.EntityTagInputUiState
import io.github.taetae98coding.diary.core.model.tag.Tag
import io.github.taetae98coding.diary.feature.core.tag.EntityAddTagSelectionUiState
import io.github.taetae98coding.diary.feature.core.tag.EntityAddTagViewModel
import io.kotest.property.Arb
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.next
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.datetime.LocalDate
import kotlinx.datetime.isoDayNumber
import kotlinx.datetime.number

internal const val ROUTINE_FORM_DEFAULT_NAVIGATE_UP = "Navigate up"
internal const val ROUTINE_FORM_DEFAULT_TITLE_LABEL = "Title"
internal const val ROUTINE_FORM_DEFAULT_DESCRIPTION_LABEL = "Description"
internal const val ROUTINE_FORM_DEFAULT_DESCRIPTION_INPUT_TAB = "Input"
internal const val ROUTINE_FORM_DEFAULT_DESCRIPTION_PREVIEW_TAB = "Preview"
internal const val ROUTINE_FORM_DEFAULT_CYCLE_LABEL = "Repeat"
internal const val ROUTINE_FORM_DEFAULT_CYCLE_DAILY = "Daily"
internal const val ROUTINE_FORM_DEFAULT_CYCLE_WEEKDAYS = "Weekdays"
internal const val ROUTINE_FORM_DEFAULT_CYCLE_WEEKENDS = "Weekends"
internal const val ROUTINE_FORM_DEFAULT_CYCLE_WEEKLY = "Weekly"
internal const val ROUTINE_FORM_DEFAULT_CYCLE_MONTHLY = "Monthly"
internal const val ROUTINE_FORM_DEFAULT_UNIT_LABEL = "Unit"
internal const val ROUTINE_FORM_UNIT_COUNT = "회"
internal const val ROUTINE_FORM_UNIT_MILLILITER = "ml"
internal const val ROUTINE_FORM_UNIT_METER = "m"
internal const val ROUTINE_FORM_DEFAULT_GOAL_AMOUNT_LABEL = "Goal"
internal const val ROUTINE_FORM_DEFAULT_AMOUNT_PER_RECORD_LABEL = "Amount per record"
internal const val ROUTINE_FORM_DEFAULT_GOAL_AMOUNT_INVALID = "Please enter a goal of 1 or more, or leave it empty."
internal const val ROUTINE_FORM_DEFAULT_AMOUNT_PER_RECORD_INVALID = "Please enter an amount of 1 or more per record."
internal const val ROUTINE_FORM_DEFAULT_TAG_LABEL = "Select tag"
internal const val ROUTINE_FORM_DEFAULT_DATE_PICKER_CONFIRM = "OK"
internal const val ROUTINE_FORM_DEFAULT_DATE_PICKER_CANCEL = "Cancel"

private const val DATE_PICKER_SWITCH_TO_INPUT = "Switch to text input mode"
private const val DATE_PICKER_INPUT_LABEL = "Date"
private const val DATE_PICKER_CURRENT_SELECTION_PREFIX = "Current selection: "
private const val DATE_PICKER_YEAR_DIGIT_COUNT = 4
private const val DATE_PICKER_DAY_DIGIT_COUNT = 2
private const val COLOR_PICKER_CONFIRM = "Confirm"
private const val RGB_MASK = 0xFFFFFF
private const val ALPHA_MASK = 0xFF000000.toInt()

internal fun routineFormAddTagViewModel(tagList: List<Tag> = emptyList()): EntityAddTagViewModel {
    val viewModel = mockk<EntityAddTagViewModel>(relaxed = true)
    every { viewModel.uiState } returns MutableStateFlow(EntityTagInputUiState(tagList = tagList))
    every { viewModel.selectionUiState } returns MutableStateFlow(EntityAddTagSelectionUiState(tagIdSet = tagList.map { tag -> tag.id }.toSet()))
    every { viewModel.tagPagingData } returns MutableStateFlow(PagingData.from(tagList))
    every { viewModel.selectableTagPagingData } returns MutableStateFlow(PagingData.from(tagList))
    return viewModel
}

// 루틴 컬러는 불투명한 컬러로 제시한다.
internal fun routineFormColor(): Color = Color(color = ALPHA_MASK or Arb.int(0..RGB_MASK).next())

internal fun SemanticsNodeInteractionsProvider.routineFormTitleInput(): SemanticsNodeInteraction = onAllNodes(hasSetTextAction())[0]

internal fun SemanticsNodeInteractionsProvider.routineFormDescriptionInput(): SemanticsNodeInteraction = onAllNodes(hasSetTextAction())[1]

internal fun SemanticsNodeInteractionsProvider.routineFormGoalAmountInput(): SemanticsNodeInteraction = onNode(hasSetTextAction() and hasText(ROUTINE_FORM_DEFAULT_GOAL_AMOUNT_LABEL))

internal fun SemanticsNodeInteractionsProvider.routineFormAmountPerRecordInput(): SemanticsNodeInteraction = onNode(hasSetTextAction() and hasText(ROUTINE_FORM_DEFAULT_AMOUNT_PER_RECORD_LABEL))

internal fun SemanticsNodeInteraction.routineFormEditableText(): String = fetchSemanticsNode().config[SemanticsProperties.EditableText].text

internal fun ComposeContentTestRule.routineFormInputGoalAmount(amount: String) {
    routineFormGoalAmountInput().performTextReplacement(amount)
    waitForIdle()
}

internal fun ComposeContentTestRule.routineFormInputAmountPerRecord(amount: String) {
    routineFormAmountPerRecordInput().performTextReplacement(amount)
    waitForIdle()
}

internal fun ComposeContentTestRule.routineFormSelectCycle(option: String) {
    routineSelectCycle(option = option)
}

internal fun ComposeContentTestRule.routineFormInputUnit(unit: String) {
    routineInputUnit(unit = unit)
}

internal fun SemanticsNodeInteractionsProvider.routineFormSelectedCycle(): String = routineSelectedCycle()

internal fun SemanticsNodeInteractionsProvider.routineFormInputtedUnit(): String = routineInputtedUnit()

internal fun routineFormDateButtonMatcher(date: LocalDate): SemanticsMatcher = hasText(routineFormDisplayText(date)) and hasClickAction()

internal fun SemanticsNodeInteractionsProvider.routineFormDateButton(date: LocalDate): SemanticsNodeInteraction = onNode(routineFormDateButtonMatcher(date))

internal fun ComposeContentTestRule.routineFormOpenDatePicker(date: LocalDate) {
    routineFormDateButton(date).performSemanticsAction(SemanticsActions.OnClick)
    waitForIdle()
}

/**
 * 열린 날짜 고르기를 글자 입력 방식으로 바꿔 다른 달의 날짜도 고른다. 기본 환경의 입력 형식은 `MM/DD/YYYY`다.
 */
internal fun ComposeContentTestRule.routineFormTypePickerDate(date: LocalDate) {
    onNodeWithContentDescription(DATE_PICKER_SWITCH_TO_INPUT).performClick()
    waitForIdle()
    onNode(hasSetTextAction() and hasText(DATE_PICKER_INPUT_LABEL)).performTextReplacement(
        date.month.number
            .toString()
            .padStart(DATE_PICKER_DAY_DIGIT_COUNT, '0') +
            date.day.toString().padStart(DATE_PICKER_DAY_DIGIT_COUNT, '0') +
            date.year.toString().padStart(DATE_PICKER_YEAR_DIGIT_COUNT, '0'),
    )
    waitForIdle()
}

internal fun ComposeContentTestRule.routineFormConfirmDatePicker() {
    onNodeWithText(ROUTINE_FORM_DEFAULT_DATE_PICKER_CONFIRM).performClick()
    waitForIdle()
}

internal fun ComposeContentTestRule.routineFormCancelDatePicker() {
    onNodeWithText(ROUTINE_FORM_DEFAULT_DATE_PICKER_CANCEL).performClick()
    waitForIdle()
}

internal fun ComposeContentTestRule.routineFormPickDate(
    from: LocalDate,
    to: LocalDate,
) {
    routineFormOpenDatePicker(from)
    routineFormTypePickerDate(to)
    routineFormConfirmDatePicker()
}

// 날짜 고르기는 머리글에서 지금 가리키는 날짜를 기본 환경의 문구로 알린다.
internal fun SemanticsNodeInteractionsProvider.routineFormPickerSelection(date: LocalDate): SemanticsNodeInteraction = onNodeWithContentDescription("$DATE_PICKER_CURRENT_SELECTION_PREFIX${routineFormDayCellText(date)}")

internal fun SemanticsNodeInteractionsProvider.routineFormPickerDayCell(date: LocalDate): SemanticsNodeInteraction = onNodeWithText(routineFormDayCellText(date))

// 날짜 선택 다이얼로그의 날짜 칸은 기본 환경에서 요일과 월 이름을 모두 쓴 문구로 읽힌다.
internal fun routineFormDayCellText(date: LocalDate): String = "${DAY_OF_WEEK_NAMES[date.dayOfWeek.isoDayNumber - 1]}, ${FULL_MONTH_NAMES[date.month.number - 1]} ${date.day}, ${date.year}"

// 기본 환경의 날짜 표시 형식과 월 이름을 고정 값으로 두어 날짜 버튼의 문구를 검증한다.
internal fun routineFormDisplayText(date: LocalDate): String = "${SHORT_MONTH_NAMES[date.month.number - 1]} ${date.day}, ${date.year}"

private val DAY_OF_WEEK_NAMES = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")

private val FULL_MONTH_NAMES =
    listOf("January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December")

private val SHORT_MONTH_NAMES = listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")

internal fun SemanticsNodeInteractionsProvider.routineFormDisplayedColorHex(): String =
    onAllNodes(SemanticsMatcher.keyIsDefined(SemanticsProperties.Text), useUnmergedTree = true)
        .fetchSemanticsNodes()
        .flatMap { node -> node.config[SemanticsProperties.Text] }
        .map { text -> text.text }
        .single { text -> COLOR_HEX_REGEX.matches(text) }

private val COLOR_HEX_REGEX = Regex("^#[0-9A-F]{6}$")

internal fun ComposeContentTestRule.routineFormChangeColor(
    fromHex: String,
    toHex: String,
) {
    onNode(hasText(fromHex, substring = true) and hasClickAction()).performSemanticsAction(SemanticsActions.OnClick)
    waitForIdle()
    onNode(hasSetTextAction() and hasAnyAncestor(isDialog())).performTextReplacement(toHex)
    waitForIdle()
    onNodeWithText(COLOR_PICKER_CONFIRM).performClick()
    waitForIdle()
}

/**
 * 화면에서 누르거나 입력할 수 있는 요소의 이름을 모은다. 접근성 이름이 있으면 그 이름을, 없으면 보이는 글자 가운데 첫 번째를 쓴다.
 */
internal fun SemanticsNodeInteractionsProvider.routineFormActionNameList(): List<String> =
    onAllNodes(hasClickAction() or hasSetTextAction())
        .fetchSemanticsNodes()
        .map { node ->
            node.config.getOrNull(SemanticsProperties.ContentDescription)?.joinToString()
                ?: node.config
                    .getOrNull(SemanticsProperties.Text)
                    .orEmpty()
                    .firstOrNull()
                    ?.text
                    .orEmpty()
        }

internal fun routineFormProgressIndicator(): SemanticsMatcher = SemanticsMatcher.keyIsDefined(SemanticsProperties.ProgressBarRangeInfo)
