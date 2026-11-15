package io.github.taetae98coding.diary.feature.routine.ui.detail

import androidx.compose.runtime.Composable
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsNode
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.SemanticsNodeInteractionsProvider
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasAnyDescendant
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isDialog
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onLast
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.test.performTextReplacement
import androidx.paging.LoadState
import androidx.paging.LoadStates
import androidx.paging.PagingData
import io.github.taetae98coding.diary.compose.tag.entity.EntityTagInputUiState
import io.github.taetae98coding.diary.core.model.routine.RoutineCycle
import io.github.taetae98coding.diary.core.model.routine.RoutineDetail
import io.github.taetae98coding.diary.core.model.routine.RoutineRecord
import io.github.taetae98coding.diary.core.model.tag.Tag
import io.github.taetae98coding.diary.core.testing.routine.routineRecord
import io.github.taetae98coding.diary.domain.routine.RoutineInput
import io.github.taetae98coding.diary.feature.core.tag.EntityDetailTagViewModel
import io.github.taetae98coding.diary.feature.routine.ui.RoutineTestTheme
import io.github.taetae98coding.diary.feature.routine.ui.detail.record.ROUTINE_RECORD_ROW_TEST_TAG
import io.github.taetae98coding.diary.feature.routine.ui.detail.record.RoutineDetailRecordEffect
import io.github.taetae98coding.diary.feature.routine.ui.detail.record.RoutineDetailRecordUiState
import io.github.taetae98coding.diary.feature.routine.ui.detail.record.RoutineDetailRecordViewModel
import io.github.taetae98coding.diary.feature.routine.ui.form.routineInputtedUnit
import io.github.taetae98coding.diary.feature.routine.ui.form.routineSelectedCycle
import io.github.taetae98coding.diary.library.compose.ui.color.toColor
import io.github.taetae98coding.diary.library.compose.ui.color.toHexString
import io.github.taetae98coding.diary.library.fixture.kotlinUuid
import io.kotest.property.Arb
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.next
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.datetime.LocalDate
import kotlinx.datetime.isoDayNumber
import kotlinx.datetime.number
import kotlin.uuid.Uuid

internal const val ROUTINE_DETAIL_WATER = "물 마시기"
internal const val ROUTINE_DETAIL_WATER_TWO_LITER = "물 2L 마시기"
internal const val ROUTINE_DETAIL_DESCRIPTION = "하루 2L"
internal const val ROUTINE_DETAIL_DEFAULT_NAVIGATE_UP = "Navigate up"
internal const val ROUTINE_DETAIL_DEFAULT_UPDATE = "Update routine"
internal const val ROUTINE_DETAIL_DEFAULT_DELETE = "Delete routine"
internal const val ROUTINE_DETAIL_DEFAULT_FINISH = "Finish routine"
internal const val ROUTINE_DETAIL_DEFAULT_RESTART = "Restart routine"
internal const val ROUTINE_DETAIL_DEFAULT_LOADING = "Loading routine"
internal const val ROUTINE_DETAIL_DEFAULT_UPDATE_SUCCEEDED = "Routine updated."
internal const val ROUTINE_DETAIL_DEFAULT_GOAL_INVALID = "Please enter a goal of 1 or more, or leave it empty."
internal const val ROUTINE_DETAIL_DEFAULT_AMOUNT_PER_RECORD_INVALID = "Please enter an amount of 1 or more per record."
internal const val ROUTINE_DETAIL_DEFAULT_TITLE_LABEL = "Title"
internal const val ROUTINE_DETAIL_DEFAULT_DESCRIPTION_LABEL = "Description"
internal const val ROUTINE_DETAIL_DEFAULT_GOAL_LABEL = "Goal"
internal const val ROUTINE_DETAIL_DEFAULT_AMOUNT_PER_RECORD_LABEL = "Amount per record"
internal const val ROUTINE_DETAIL_DEFAULT_RECORD_AMOUNT_LABEL = "Amount"
internal const val ROUTINE_DETAIL_DEFAULT_RECORD_ADD = "Add record"
internal const val ROUTINE_DETAIL_DEFAULT_RECORD_DATE_PREFIX = "Record date, "
internal const val ROUTINE_DETAIL_DEFAULT_RECORD_REMOVE_PREFIX = "Remove record, "
internal const val ROUTINE_DETAIL_DEFAULT_CYCLE_LABEL = "Repeat"
internal const val ROUTINE_DETAIL_DEFAULT_CYCLE_DAILY = "Daily"
internal const val ROUTINE_DETAIL_DEFAULT_CYCLE_WEEKLY = "Weekly"
internal const val ROUTINE_DETAIL_DEFAULT_CYCLE_MONTHLY = "Monthly"
internal const val ROUTINE_DETAIL_DEFAULT_UNIT_LABEL = "Unit"
internal const val ROUTINE_DETAIL_UNIT_COUNT = "회"
internal const val ROUTINE_DETAIL_UNIT_MILLILITER = "ml"
internal const val ROUTINE_DETAIL_UNIT_METER = "m"
internal const val ROUTINE_DETAIL_DEFAULT_TAG_LABEL = "Select tag"
internal const val ROUTINE_DETAIL_DEFAULT_CLEAR_TEXT = "Clear text"
internal const val ROUTINE_DETAIL_DEFAULT_DESCRIPTION_INPUT_TAB = "Input"
internal const val ROUTINE_DETAIL_DEFAULT_DESCRIPTION_PREVIEW_TAB = "Preview"
internal const val ROUTINE_DETAIL_KOREAN_UPDATE = "루틴 수정"
internal const val ROUTINE_DETAIL_KOREAN_UPDATE_SUCCEEDED = "루틴이 수정되었습니다."
internal const val ROUTINE_DETAIL_KOREAN_GOAL_INVALID = "목표량에 1 이상의 숫자를 입력하거나 비워 주세요."
internal const val ROUTINE_DETAIL_KOREAN_AMOUNT_PER_RECORD_INVALID = "한 번에 기록할 양에 1 이상의 숫자를 입력해 주세요."

private const val RGB_MASK = 0xFFFFFF
private const val ALPHA_MASK = 0xFF000000.toInt()
private const val PICKER_WAIT_ATTEMPT_COUNT = 500
private const val PICKER_WAIT_INTERVAL_MILLIS = 10L
private const val DEFAULT_COLOR_PICKER_CONFIRM = "Confirm"
private const val DATE_PICKER_CONFIRM = "OK"
private const val DATE_PICKER_CANCEL = "Cancel"
private const val DATE_PICKER_SWITCH_TO_INPUT = "Switch to text input mode"
private const val DATE_PICKER_INPUT_LABEL = "Date"
private const val DATE_PICKER_CURRENT_SELECTION_PREFIX = "Current selection: "
private const val DATE_PICKER_YEAR_DIGIT_COUNT = 4
private const val DATE_PICKER_DAY_DIGIT_COUNT = 2
private const val TITLE_INPUT_INDEX = 0
private const val DESCRIPTION_INPUT_INDEX = 1

// 루틴 컬러는 불투명한 ARGB를 Long으로 기록한다.
internal fun routineDetailColor(): Long = (ALPHA_MASK or Arb.int(0..RGB_MASK).next()).toLong()

internal fun routineDetailOtherColor(color: Long): Long = generateSequence { routineDetailColor() }.first { value -> value.routineDetailColorHex() != color.routineDetailColorHex() }

internal fun Long.routineDetailColorHex(): String = toColor().toHexString()

internal fun routineDetailOf(
    title: String = ROUTINE_DETAIL_WATER,
    description: String = ROUTINE_DETAIL_DESCRIPTION,
    color: Long = routineDetailColor(),
    startDate: LocalDate = LocalDate(2026, 3, 1),
    cycle: RoutineCycle = RoutineCycle.WEEKLY,
    unit: String = "ml",
    goalAmount: Int? = 2000,
    amountPerRecord: Int = 250,
): RoutineDetail =
    RoutineDetail(
        title = title,
        description = description,
        color = color,
        startDate = startDate,
        cycle = cycle,
        unit = unit,
        goalAmount = goalAmount,
        amountPerRecord = amountPerRecord,
    )

internal fun RoutineDetail.routineDetailInput(): RoutineInput =
    RoutineInput(
        title = title,
        description = description,
        color = color,
        startDate = startDate,
        cycle = cycle,
        unit = unit,
        goalAmount = goalAmount,
        amountPerRecord = amountPerRecord,
    )

internal fun routineDetailRecordOf(
    routineId: Uuid,
    date: LocalDate,
    amount: Int,
): RoutineRecord = Arb.routineRecord(routineId = routineId).next().copy(date = date, amount = amount)

internal fun routineDetailRecordPagingDataOf(recordList: List<RoutineRecord>): PagingData<RoutineRecord> =
    PagingData.from(
        data = recordList,
        sourceLoadStates =
            LoadStates(
                refresh = LoadState.NotLoading(endOfPaginationReached = true),
                prepend = LoadState.NotLoading(endOfPaginationReached = true),
                append = LoadState.NotLoading(endOfPaginationReached = true),
            ),
    )

/**
 * RoutineDetail 화면을 ViewModel 대역과 함께 배치한다. 저장된 루틴, 기록과 태그 연결은 상태 스트림으로 바꾸고, 결과는 Effect 채널로 보낸다.
 */
internal class RoutineDetailTestEnvironment(
    val detail: RoutineDetail = routineDetailOf(),
    val id: Uuid = Arb.kotlinUuid().next(),
    val tagAddRequestKey: Uuid = Arb.kotlinUuid().next(),
    recordList: List<RoutineRecord> = emptyList(),
    tagList: List<Tag> = emptyList(),
    selectableTagList: List<Tag> = emptyList(),
) {
    val uiState: MutableStateFlow<RoutineDetailUiState> = MutableStateFlow(RoutineDetailUiState.Content(id = id, detail = detail, isFinished = false))
    val recordUiState: MutableStateFlow<RoutineDetailRecordUiState> = MutableStateFlow(RoutineDetailRecordUiState())
    val recordPagingData: MutableStateFlow<PagingData<RoutineRecord>> = MutableStateFlow(routineDetailRecordPagingDataOf(recordList))
    val tagUiState: MutableStateFlow<EntityTagInputUiState> = MutableStateFlow(EntityTagInputUiState(tagList = tagList))
    val effect: Channel<RoutineDetailEffect> = Channel(Channel.BUFFERED)
    val recordEffect: Channel<RoutineDetailRecordEffect> = Channel(Channel.BUFFERED)
    val navigatedTagIdList: MutableList<Uuid> = mutableListOf()
    var navigateUpCount: Int = 0

    val tagViewModel: EntityDetailTagViewModel =
        mockk<EntityDetailTagViewModel>(relaxed = true).apply {
            every { uiState } returns this@RoutineDetailTestEnvironment.tagUiState
            every { tagPagingData } returns MutableStateFlow(PagingData.from(selectableTagList))
            every { selectableTagPagingData } returns MutableStateFlow(PagingData.from(selectableTagList))
        }

    val recordViewModel: RoutineDetailRecordViewModel =
        mockk<RoutineDetailRecordViewModel>(relaxed = true).apply {
            every { uiState } returns this@RoutineDetailTestEnvironment.recordUiState
            every { recordPagingData } returns this@RoutineDetailTestEnvironment.recordPagingData
            every { effect } returns this@RoutineDetailTestEnvironment.recordEffect.receiveAsFlow()
        }

    val routineViewModel: RoutineDetailViewModel =
        mockk<RoutineDetailViewModel>(relaxed = true).apply {
            every { uiState } returns this@RoutineDetailTestEnvironment.uiState
            every { effect } returns this@RoutineDetailTestEnvironment.effect.receiveAsFlow()
        }

    fun content(
        detail: RoutineDetail = this.detail,
        isUpdateInProgress: Boolean = false,
        isFinishInProgress: Boolean = false,
        isDeleteInProgress: Boolean = false,
    ): RoutineDetailUiState.Content =
        RoutineDetailUiState.Content(
            id = id,
            detail = detail,
            isFinished = false,
            isUpdateInProgress = isUpdateInProgress,
            isFinishInProgress = isFinishInProgress,
            isDeleteInProgress = isDeleteInProgress,
        )
}

@Composable
internal fun RoutineDetailTestScreen(
    environment: RoutineDetailTestEnvironment,
    routineViewModel: RoutineDetailViewModel = environment.routineViewModel,
) {
    RoutineTestTheme {
        RoutineDetailScreen(
            navigateUp = { environment.navigateUpCount += 1 },
            navigateToTagAdd = {},
            navigateToTagDetail = environment.navigatedTagIdList::add,
            tagAddRequestKey = environment.tagAddRequestKey,
            componentVisibleProvider = { RoutineDetailScaffoldComponentVisible() },
            routineViewModel = routineViewModel,
            recordViewModel = environment.recordViewModel,
            tagViewModel = environment.tagViewModel,
        )
    }
}

internal fun routineDetailTopBarTitle(text: String): SemanticsMatcher = hasText(text) and !hasSetTextAction() and !hasAnyAncestor(hasSetTextAction())

internal fun routineDetailUpdateButton(): SemanticsMatcher = hasContentDescription(ROUTINE_DETAIL_DEFAULT_UPDATE)

internal fun routineDetailDeleteButton(): SemanticsMatcher = hasContentDescription(ROUTINE_DETAIL_DEFAULT_DELETE)

internal fun routineDetailFinishButton(): SemanticsMatcher = hasContentDescription(ROUTINE_DETAIL_DEFAULT_FINISH)

internal fun routineDetailRestartButton(): SemanticsMatcher = hasContentDescription(ROUTINE_DETAIL_DEFAULT_RESTART)

internal fun routineDetailRecordAddButton(): SemanticsMatcher = hasContentDescription(ROUTINE_DETAIL_DEFAULT_RECORD_ADD)

internal fun hasRoutineDetailProgress(): SemanticsMatcher = hasAnyDescendant(SemanticsMatcher.keyIsDefined(SemanticsProperties.ProgressBarRangeInfo))

internal fun SemanticsNodeInteractionsProvider.routineDetailTitleInput(): SemanticsNodeInteraction = onAllNodes(hasSetTextAction())[TITLE_INPUT_INDEX]

internal fun SemanticsNodeInteractionsProvider.routineDetailDescriptionInput(): SemanticsNodeInteraction = onAllNodes(hasSetTextAction())[DESCRIPTION_INPUT_INDEX]

internal fun SemanticsNodeInteractionsProvider.routineDetailGoalInput(): SemanticsNodeInteraction = onNode(hasSetTextAction() and hasText(ROUTINE_DETAIL_DEFAULT_GOAL_LABEL))

internal fun SemanticsNodeInteractionsProvider.routineDetailAmountPerRecordInput(): SemanticsNodeInteraction = onNode(hasSetTextAction() and hasText(ROUTINE_DETAIL_DEFAULT_AMOUNT_PER_RECORD_LABEL))

internal fun SemanticsNodeInteractionsProvider.routineDetailRecordAmountInput(): SemanticsNodeInteraction = onNode(hasSetTextAction() and hasText(ROUTINE_DETAIL_DEFAULT_RECORD_AMOUNT_LABEL))

internal fun SemanticsNodeInteraction.routineDetailEditableText(): String = fetchSemanticsNode().config[SemanticsProperties.EditableText].text

internal fun SemanticsNodeInteractionsProvider.routineDetailSelectedCycle(): String = routineSelectedCycle()

internal fun SemanticsNodeInteractionsProvider.routineDetailInputtedUnit(): String = routineInputtedUnit()

internal fun ComposeContentTestRule.routineDetailReplaceText(
    input: SemanticsNodeInteraction,
    text: String,
) {
    input.performTextReplacement(text)
    waitForIdle()
}

internal fun ComposeContentTestRule.routineDetailClickUpdate() {
    onNode(routineDetailUpdateButton()).performSemanticsAction(SemanticsActions.OnClick)
    waitForIdle()
}

internal fun ComposeContentTestRule.routineDetailClickDelete() {
    onNode(routineDetailDeleteButton()).performClick()
    waitForIdle()
}

internal fun ComposeContentTestRule.routineDetailClickRecordAdd() {
    onNode(routineDetailRecordAddButton()).performSemanticsAction(SemanticsActions.OnClick)
    waitForIdle()
}

internal fun SemanticsNodeInteractionsProvider.routineDetailDateButton(date: LocalDate): SemanticsNodeInteraction = onNode(hasText(routineDetailDisplayText(date)) and hasClickAction())

internal fun routineDetailRecordDateButton(): SemanticsMatcher = hasContentDescription(ROUTINE_DETAIL_DEFAULT_RECORD_DATE_PREFIX, substring = true)

internal fun SemanticsNodeInteractionsProvider.routineDetailRecordDateText(): String =
    onNode(routineDetailRecordDateButton())
        .fetchSemanticsNode()
        .config[SemanticsProperties.ContentDescription]
        .single()
        .removePrefix(ROUTINE_DETAIL_DEFAULT_RECORD_DATE_PREFIX)

internal fun ComposeContentTestRule.routineDetailOpenDatePicker(date: LocalDate) {
    routineDetailDateButton(date).performSemanticsAction(SemanticsActions.OnClick)
    waitForIdle()
}

internal fun ComposeContentTestRule.routineDetailOpenRecordDatePicker() {
    onNode(routineDetailRecordDateButton()).performSemanticsAction(SemanticsActions.OnClick)
    waitForIdle()
}

/**
 * 열린 날짜 고르기를 글자 입력 방식으로 바꿔 다른 달의 날짜도 고른다. 기본 환경의 입력 형식은 `MM/DD/YYYY`다.
 */
internal fun ComposeContentTestRule.routineDetailTypePickerDate(date: LocalDate) {
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

internal fun ComposeContentTestRule.routineDetailConfirmDatePicker() {
    onNodeWithText(DATE_PICKER_CONFIRM).performClick()
    waitForIdle()
}

internal fun ComposeContentTestRule.routineDetailCancelDatePicker() {
    onNodeWithText(DATE_PICKER_CANCEL).performClick()
    waitForIdle()
}

internal fun ComposeContentTestRule.routineDetailPickDate(
    from: LocalDate,
    to: LocalDate,
) {
    routineDetailOpenDatePicker(from)
    routineDetailTypePickerDate(to)
    routineDetailConfirmDatePicker()
}

// 날짜 고르기는 머리글에서 지금 가리키는 날짜를 기본 환경의 문구로 알린다.
internal fun SemanticsNodeInteractionsProvider.routineDetailPickerSelection(date: LocalDate): SemanticsNodeInteraction = onNodeWithContentDescription("$DATE_PICKER_CURRENT_SELECTION_PREFIX${routineDetailDayCellText(date)}")

internal fun SemanticsNodeInteractionsProvider.routineDetailPickerDayCell(date: LocalDate): SemanticsNodeInteraction = onNodeWithText(routineDetailDayCellText(date))

// 날짜 선택 다이얼로그의 날짜 칸은 기본 환경에서 요일과 월 이름을 모두 쓴 문구로 읽힌다.
internal fun routineDetailDayCellText(date: LocalDate): String = "${DAY_OF_WEEK_NAMES[date.dayOfWeek.isoDayNumber - 1]}, ${FULL_MONTH_NAMES[date.month.number - 1]} ${date.day}, ${date.year}"

// 기본 환경의 날짜 표시 형식과 월 이름을 고정 값으로 두어 날짜 버튼의 문구를 검증한다.
internal fun routineDetailDisplayText(date: LocalDate): String = "${SHORT_MONTH_NAMES[date.month.number - 1]} ${date.day}, ${date.year}"

private val DAY_OF_WEEK_NAMES = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")

private val FULL_MONTH_NAMES =
    listOf("January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December")

private val SHORT_MONTH_NAMES = listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")

internal fun SemanticsNodeInteractionsProvider.routineDetailDisplayedColorHex(): String =
    onAllNodes(SemanticsMatcher.keyIsDefined(SemanticsProperties.Text), useUnmergedTree = true)
        .fetchSemanticsNodes()
        .flatMap { node -> node.config[SemanticsProperties.Text] }
        .map { text -> text.text }
        .single { text -> COLOR_HEX_REGEX.matches(text) }

private val COLOR_HEX_REGEX = Regex("^#[0-9A-F]{6}$")

internal fun ComposeContentTestRule.routineDetailChangeColor(
    fromHex: String,
    toHex: String,
) {
    onNode(hasText(fromHex, substring = true) and hasClickAction()).performSemanticsAction(SemanticsActions.OnClick)
    waitForIdle()
    onNode(hasSetTextAction() and hasAnyAncestor(isDialog())).performTextReplacement(toHex)
    waitForIdle()
    onNodeWithText(DEFAULT_COLOR_PICKER_CONFIRM).performClick()
    waitForIdle()
}

internal fun SemanticsNodeInteractionsProvider.routineDetailActionNameList(): List<String> =
    onAllNodes(hasClickAction() or hasSetTextAction())
        .fetchSemanticsNodes()
        .map { node ->
            node.config.getOrNull(SemanticsProperties.ContentDescription)?.joinToString()
                ?: node.config
                    .getOrNull(SemanticsProperties.Text)
                    .orEmpty()
                    .joinToString { text -> text.text }
        }

internal fun SemanticsNodeInteractionsProvider.routineDetailVisibleTextList(): List<String> =
    onAllNodes(SemanticsMatcher.keyIsDefined(SemanticsProperties.Text), useUnmergedTree = true)
        .fetchSemanticsNodes()
        .flatMap { node -> node.config[SemanticsProperties.Text] }
        .map { text -> text.text }

/**
 * 기록 내역을 위에서부터 날짜와 양의 문구의 쌍으로 읽는다.
 */
internal fun SemanticsNodeInteractionsProvider.routineDetailRecordRowList(): List<Pair<String, String>> =
    onAllNodes(hasTestTag(ROUTINE_RECORD_ROW_TEST_TAG), useUnmergedTree = true)
        .fetchSemanticsNodes()
        .map { row ->
            val textList = row.routineDetailTextList()

            textList.first() to textList.last()
        }

private fun SemanticsNode.routineDetailTextList(): List<String> =
    config
        .getOrNull(SemanticsProperties.Text)
        .orEmpty()
        .map { text -> text.text } + children.flatMap { child -> child.routineDetailTextList() }

internal fun routineDetailRecordRemoveButton(
    date: LocalDate,
    amountText: String,
): SemanticsMatcher = hasContentDescription("$ROUTINE_DETAIL_DEFAULT_RECORD_REMOVE_PREFIX${routineDetailDisplayText(date)} $amountText")

// 태그 입력은 본문의 마지막 입력이라 화면 아래에 놓일 수 있으므로, 누르는 위치와 무관한 클릭 동작으로 누른다.
internal fun ComposeContentTestRule.routineDetailOpenTagPicker() {
    onNodeWithText(ROUTINE_DETAIL_DEFAULT_TAG_LABEL).performSemanticsAction(SemanticsActions.OnClick)
    waitForIdle()
}

internal fun ComposeContentTestRule.routineDetailLinkTag(title: String) {
    routineDetailOpenTagPicker()
    routineDetailAwaitTagPickerRow(title = title)
    onNodeWithText(title).performClick()
    waitForIdle()
}

// 연결된 태그는 태그 입력과 선택 목록에 모두 나타나므로, 선택 목록의 마지막 노드를 눌러 해제한다.
internal fun ComposeContentTestRule.routineDetailUnlinkTag(title: String) {
    routineDetailOpenTagPicker()
    routineDetailAwaitTagPickerRow(title = title, count = 2)
    onAllNodesWithText(title).onLast().performClick()
    waitForIdle()
}

// 선택 목록은 페이지 단위로 준비되므로 항목이 나타날 때까지 프레임과 실제 시간을 함께 진행시킨다.
private fun ComposeContentTestRule.routineDetailAwaitTagPickerRow(
    title: String,
    count: Int = 1,
) {
    repeat(PICKER_WAIT_ATTEMPT_COUNT) {
        waitForIdle()
        if (onAllNodesWithText(title).fetchSemanticsNodes().size >= count) return
        mainClock.advanceTimeByFrame()
        @Suppress("ForbiddenMethodCall")
        Thread.sleep(PICKER_WAIT_INTERVAL_MILLIS)
    }

    error("태그 선택 목록에 $title 이 나타나지 않았다")
}

// 태그 입력에 놓인 연결 대상 태그다. 태그 선택 목록의 같은 이름 행과 구분한다.
internal fun routineDetailTagChip(title: String): SemanticsMatcher = hasText(title) and !hasAnyAncestor(isDialog())
