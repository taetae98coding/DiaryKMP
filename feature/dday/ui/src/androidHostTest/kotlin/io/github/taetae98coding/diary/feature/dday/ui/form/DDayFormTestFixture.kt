package io.github.taetae98coding.diary.feature.dday.ui.form

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.SemanticsNodeInteractionsProvider
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isToggleable
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.test.performTextReplacement
import androidx.navigation3.runtime.result.LocalResultEventBus
import androidx.navigation3.runtime.result.ResultEventBus
import androidx.paging.PagingData
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.compose.tag.entity.EntityTagInputUiState
import io.github.taetae98coding.diary.core.model.tag.Tag
import io.github.taetae98coding.diary.feature.core.tag.EntityAddTagSelectionUiState
import io.github.taetae98coding.diary.feature.core.tag.EntityAddTagViewModel
import io.github.taetae98coding.diary.feature.core.tag.EntityDetailTagViewModel
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.datetime.LocalDate
import kotlinx.datetime.isoDayNumber
import kotlinx.datetime.number
import kotlin.uuid.Uuid

internal const val DDAY_FORM_DEFAULT_NAVIGATE_UP = "Navigate up"
internal const val DDAY_FORM_DEFAULT_TITLE_LABEL = "Title"
internal const val DDAY_FORM_DEFAULT_DESCRIPTION_LABEL = "Description"
internal const val DDAY_FORM_DEFAULT_PRESET = "Presets"
internal const val DDAY_FORM_DEFAULT_PRESET_COUPLE = "Couple"
internal const val DDAY_FORM_DEFAULT_PRESET_EXAM = "Exam"
internal const val DDAY_FORM_DEFAULT_PRESET_BABY = "Baby"
internal const val DDAY_FORM_DEFAULT_PRESET_CUSTOM = "Custom"
internal const val DDAY_FORM_DEFAULT_BASE_DATE_LABEL = "Start date"
internal const val DDAY_FORM_DEFAULT_COUNTS_BASE_DAY_LABEL = "Count start date as day 1"
internal const val DDAY_FORM_DEFAULT_END_DATE_LABEL = "End date"
internal const val DDAY_FORM_DEFAULT_END_DATE_NOT_SET = "Not set"
internal const val DDAY_FORM_DEFAULT_END_DATE_CLEAR = "Clear end date"
internal const val DDAY_FORM_DEFAULT_OVERFLOW_LABEL = "Missing dates"
internal const val DDAY_FORM_DEFAULT_OVERFLOW_CLAMP = "Use last day of month"
internal const val DDAY_FORM_DEFAULT_OVERFLOW_SKIP = "Skip"
internal const val DDAY_FORM_DEFAULT_OVERFLOW_NEXT_MONTH = "Use 1st of next month"
internal const val DDAY_FORM_DEFAULT_REPEAT_LABEL = "Repeat intervals"
internal const val DDAY_FORM_DEFAULT_REPEAT_ADD = "Add interval"
internal const val DDAY_FORM_DEFAULT_UNIT_DAY = "Days"
internal const val DDAY_FORM_DEFAULT_UNIT_MONTH = "Months"
internal const val DDAY_FORM_DEFAULT_UNIT_YEAR = "Years"
internal const val DDAY_FORM_DEFAULT_REPEAT_BLANK = "Please enter a number of 1 or more for each interval."
internal const val DDAY_FORM_DEFAULT_TAG_LABEL = "Select tag"
internal const val DDAY_FORM_DEFAULT_DATE_PICKER_CONFIRM = "OK"
internal const val DDAY_FORM_DEFAULT_DATE_PICKER_CANCEL = "Cancel"

private const val DATE_PICKER_SWITCH_TO_INPUT = "Switch to text input mode"
private const val DATE_PICKER_INPUT_LABEL = "Date"
private const val DATE_PICKER_CURRENT_SELECTION_PREFIX = "Current selection: "
private const val DATE_PICKER_YEAR_DIGIT_COUNT = 4
private const val DATE_PICKER_DAY_DIGIT_COUNT = 2

/**
 * 태그 입력은 TagAdd 결과를 [LocalResultEventBus]로 받으므로, 디데이 입력 화면을 직접 배치하는 테스트는 이 테마로 감싼다.
 */
@Composable
internal fun DDayFormTestTheme(
    resultEventBus: ResultEventBus = ResultEventBus(),
    content: @Composable () -> Unit,
) {
    CompositionLocalProvider(LocalResultEventBus provides resultEventBus) {
        DiaryTheme(content = content)
    }
}

internal fun ddayFormAddTagViewModel(tagList: List<Tag> = emptyList()): EntityAddTagViewModel {
    val viewModel = mockk<EntityAddTagViewModel>(relaxed = true)
    every { viewModel.uiState } returns MutableStateFlow(EntityTagInputUiState(tagList = tagList))
    every { viewModel.selectionUiState } returns MutableStateFlow(EntityAddTagSelectionUiState(tagIdSet = tagList.map { tag -> tag.id }.toSet()))
    every { viewModel.tagPagingData } returns MutableStateFlow(PagingData.from(tagList))
    every { viewModel.selectableTagPagingData } returns MutableStateFlow(PagingData.from(tagList))
    return viewModel
}

internal fun ddayFormDetailTagViewModel(
    tagList: List<Tag> = emptyList(),
    selectableTagList: List<Tag> = emptyList(),
): EntityDetailTagViewModel {
    val viewModel = mockk<EntityDetailTagViewModel>(relaxed = true)
    every { viewModel.uiState } returns MutableStateFlow(EntityTagInputUiState(tagList = tagList))
    every { viewModel.tagPagingData } returns MutableStateFlow(PagingData.from(selectableTagList))
    every { viewModel.selectableTagPagingData } returns MutableStateFlow(PagingData.from(selectableTagList))
    return viewModel
}

internal fun SemanticsNodeInteractionsProvider.ddayTitleInput(): SemanticsNodeInteraction = onAllNodes(hasSetTextAction())[0]

internal fun SemanticsNodeInteractionsProvider.ddayDescriptionInput(): SemanticsNodeInteraction = onAllNodes(hasSetTextAction())[1]

internal fun SemanticsNodeInteraction.ddayEditableText(): String = fetchSemanticsNode().config[SemanticsProperties.EditableText].text

internal fun SemanticsNodeInteractionsProvider.ddayRepeatNumberInput(position: Int): SemanticsNodeInteraction = onNodeWithContentDescription("Interval $position number")

internal fun SemanticsNodeInteractionsProvider.ddayRepeatUnitInput(position: Int): SemanticsNodeInteraction = onNodeWithContentDescription("Interval $position unit")

internal fun ddayRepeatRemoveButton(position: Int): SemanticsMatcher = hasContentDescription("Remove interval $position")

/**
 * 반복 간격 입력을 위에서부터 숫자와 단위 이름의 쌍으로 읽는다.
 */
internal fun SemanticsNodeInteractionsProvider.ddayRepeatList(): List<Pair<String, String>> {
    val count = onAllNodes(hasContentDescription("Remove interval ", substring = true)).fetchSemanticsNodes().size

    return (1..count).map { position ->
        ddayRepeatNumberInput(position).ddayEditableText() to ddayRepeatUnitInput(position).ddayEditableText()
    }
}

// 반복 간격 카드는 본문 아래쪽에 놓여 화면 밖에 있을 수 있으므로, 누르는 위치와 무관한 클릭 동작으로 누른다.
internal fun ComposeContentTestRule.ddayAddRepeat() {
    onNodeWithContentDescription(DDAY_FORM_DEFAULT_REPEAT_ADD).performSemanticsAction(SemanticsActions.OnClick)
    waitForIdle()
}

internal fun ComposeContentTestRule.ddayRemoveRepeat(position: Int) {
    onNode(ddayRepeatRemoveButton(position)).performSemanticsAction(SemanticsActions.OnClick)
    waitForIdle()
}

internal fun ComposeContentTestRule.ddayInputRepeatNumber(
    position: Int,
    number: String,
) {
    ddayRepeatNumberInput(position).performTextReplacement(number)
    waitForIdle()
}

internal fun ComposeContentTestRule.ddaySelectRepeatUnit(
    position: Int,
    unitLabel: String,
) {
    ddayRepeatUnitInput(position).performSemanticsAction(SemanticsActions.OnClick)
    waitForIdle()
    onNode(hasText(unitLabel) and hasClickAction() and SemanticsMatcher.keyIsDefined(SemanticsProperties.Selected)).performClick()
    waitForIdle()
}

internal fun ComposeContentTestRule.ddayAddRepeatOf(
    number: String,
    unitLabel: String,
) {
    ddayAddRepeat()
    val position = ddayRepeatList().size
    if (number.isNotEmpty()) ddayInputRepeatNumber(position = position, number = number)
    if (unitLabel != DDAY_FORM_DEFAULT_UNIT_DAY) ddaySelectRepeatUnit(position = position, unitLabel = unitLabel)
}

internal fun ddayCountsBaseDayRow(): SemanticsMatcher = isToggleable() and hasText(DDAY_FORM_DEFAULT_COUNTS_BASE_DAY_LABEL)

internal fun ComposeContentTestRule.ddayToggleCountsBaseDay() {
    onNode(ddayCountsBaseDayRow()).performSemanticsAction(SemanticsActions.OnClick)
    waitForIdle()
}

internal fun ComposeContentTestRule.ddayIsCountsBaseDayOn(): Boolean =
    onNode(ddayCountsBaseDayRow())
        .fetchSemanticsNode()
        .config
        .getOrNull(SemanticsProperties.ToggleableState)
        ?.name == "On"

/**
 * 달력에 없는 날짜 버튼은 지금 고른 값을 이름으로 보여 주므로, 그 버튼을 눌러 메뉴를 펼친 뒤 항목을 고른다.
 */
internal fun ComposeContentTestRule.ddaySelectOverflowPolicy(
    currentLabel: String,
    label: String,
) {
    onNode(hasText(currentLabel) and hasClickAction() and SemanticsMatcher.keyNotDefined(SemanticsProperties.Selected)).performSemanticsAction(SemanticsActions.OnClick)
    waitForIdle()
    onNode(hasText(label) and SemanticsMatcher.keyIsDefined(SemanticsProperties.Selected)).performClick()
    waitForIdle()
}

internal fun SemanticsNodeInteractionsProvider.ddayOverflowPolicyButton(label: String): SemanticsNodeInteraction = onNode(hasText(label) and hasClickAction() and SemanticsMatcher.keyNotDefined(SemanticsProperties.Selected))

internal fun SemanticsNodeInteractionsProvider.ddayDateButton(date: LocalDate): SemanticsNodeInteraction = onNode(hasText(ddayDisplayText(date)) and hasClickAction())

internal fun SemanticsNodeInteractionsProvider.ddayEndDateNotSetButton(): SemanticsNodeInteraction = onNode(hasText(DDAY_FORM_DEFAULT_END_DATE_NOT_SET) and hasClickAction())

internal fun ComposeContentTestRule.ddayOpenDatePicker(date: LocalDate) {
    ddayDateButton(date).performSemanticsAction(SemanticsActions.OnClick)
    waitForIdle()
}

internal fun ComposeContentTestRule.ddayOpenEndDatePickerNotSet() {
    ddayEndDateNotSetButton().performSemanticsAction(SemanticsActions.OnClick)
    waitForIdle()
}

/**
 * 열린 날짜 고르기를 글자 입력 방식으로 바꿔 다른 달의 날짜도 고른다. 기본 환경의 입력 형식은 `MM/DD/YYYY`다.
 */
internal fun ComposeContentTestRule.ddayTypePickerDate(date: LocalDate) {
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

internal fun ComposeContentTestRule.ddayConfirmDatePicker() {
    onNodeWithText(DDAY_FORM_DEFAULT_DATE_PICKER_CONFIRM).performClick()
    waitForIdle()
}

internal fun ComposeContentTestRule.ddayCancelDatePicker() {
    onNodeWithText(DDAY_FORM_DEFAULT_DATE_PICKER_CANCEL).performClick()
    waitForIdle()
}

internal fun ComposeContentTestRule.ddayPickDate(
    from: LocalDate,
    to: LocalDate,
) {
    ddayOpenDatePicker(from)
    ddayTypePickerDate(to)
    ddayConfirmDatePicker()
}

// 날짜 고르기는 머리글에서 지금 가리키는 날짜를 기본 환경의 문구로 알린다.
internal fun SemanticsNodeInteractionsProvider.ddayPickerSelection(date: LocalDate): SemanticsNodeInteraction = onNodeWithContentDescription("$DATE_PICKER_CURRENT_SELECTION_PREFIX${ddayDayCellText(date)}")

internal fun SemanticsNodeInteractionsProvider.ddayPickerDayCell(date: LocalDate): SemanticsNodeInteraction = onNodeWithText(ddayDayCellText(date))

internal fun SemanticsNodeInteractionsProvider.ddayDatePickerConfirmButton(): SemanticsNodeInteraction = onNodeWithText(DDAY_FORM_DEFAULT_DATE_PICKER_CONFIRM)

// 날짜 선택 다이얼로그의 날짜 칸은 기본 환경에서 요일과 월 이름을 모두 쓴 문구로 읽힌다.
internal fun ddayDayCellText(date: LocalDate): String = "${DAY_OF_WEEK_NAMES[date.dayOfWeek.isoDayNumber - 1]}, ${FULL_MONTH_NAMES[date.month.number - 1]} ${date.day}, ${date.year}"

// 기본 환경의 날짜 표시 형식과 월 이름을 고정 값으로 두어 날짜 버튼의 문구를 검증한다.
internal fun ddayDisplayText(date: LocalDate): String = "${SHORT_MONTH_NAMES[date.month.number - 1]} ${date.day}, ${date.year}"

private val DAY_OF_WEEK_NAMES = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")

private val FULL_MONTH_NAMES =
    listOf("January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December")

private val SHORT_MONTH_NAMES = listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")

internal fun SemanticsNodeInteractionsProvider.ddayDisplayedColorHex(): String =
    onAllNodes(SemanticsMatcher.keyIsDefined(SemanticsProperties.Text), useUnmergedTree = true)
        .fetchSemanticsNodes()
        .flatMap { node -> node.config[SemanticsProperties.Text] }
        .map { text -> text.text }
        .single { text -> COLOR_HEX_REGEX.matches(text) }

private val COLOR_HEX_REGEX = Regex("^#[0-9A-F]{6}$")

internal fun SemanticsNodeInteractionsProvider.ddayActionNameList(): List<String> =
    onAllNodes(hasClickAction() or hasSetTextAction())
        .fetchSemanticsNodes()
        .map { node ->
            node.config.getOrNull(SemanticsProperties.ContentDescription)?.joinToString()
                ?: node.config
                    .getOrNull(SemanticsProperties.Text)
                    .orEmpty()
                    .joinToString { text -> text.text }
        }

internal fun SemanticsNodeInteractionsProvider.ddayVisibleTextList(): List<String> =
    onAllNodes(SemanticsMatcher.keyIsDefined(SemanticsProperties.Text), useUnmergedTree = true)
        .fetchSemanticsNodes()
        .flatMap { node -> node.config[SemanticsProperties.Text] }
        .map { text -> text.text }

internal fun ddayProgressIndicator(): SemanticsMatcher = SemanticsMatcher.keyIsDefined(SemanticsProperties.ProgressBarRangeInfo)
