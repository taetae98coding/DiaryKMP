package io.github.taetae98coding.diary.feature.routine.ui.detail

import androidx.compose.runtime.Composable
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.SemanticsNodeInteractionsProvider
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasAnyDescendant
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasScrollToIndexAction
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isDialog
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToIndex
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
import io.github.taetae98coding.diary.library.compose.ui.color.toColor
import io.github.taetae98coding.diary.library.compose.ui.color.toHexString
import io.github.taetae98coding.diary.library.fixture.kotlinUuid
import io.kotest.property.Arb
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.next
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkObject
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.isoDayNumber
import kotlinx.datetime.number
import kotlinx.datetime.toInstant
import kotlin.time.Clock
import kotlin.time.Instant
import kotlin.uuid.Uuid

internal const val ROUTINE_RECORD_TITLE = "물 마시기"
internal const val ROUTINE_RECORD_TITLE_EDITED = "물 2L 마시기"
internal const val ROUTINE_RECORD_DESCRIPTION = "하루 2L"
internal const val ROUTINE_RECORD_DEFAULT_NAVIGATE_UP = "Navigate up"
internal const val ROUTINE_RECORD_DEFAULT_UPDATE = "Update routine"
internal const val ROUTINE_RECORD_DEFAULT_DELETE = "Delete routine"
internal const val ROUTINE_RECORD_DEFAULT_UPDATE_SUCCEEDED = "Routine updated."
internal const val ROUTINE_RECORD_DEFAULT_GOAL_INVALID = "Please enter a goal of 1 or more, or leave it empty."
internal const val ROUTINE_RECORD_DEFAULT_AMOUNT_PER_RECORD_INVALID = "Please enter an amount of 1 or more per record."
internal const val ROUTINE_RECORD_DEFAULT_EMPTY_TITLE = "No records yet"
internal const val ROUTINE_RECORD_KOREAN_EMPTY_TITLE = "아직 기록이 없습니다"
internal const val ROUTINE_RECORD_DEFAULT_TAG_LABEL = "Select tag"
internal const val ROUTINE_RECORD_DEFAULT_DATE_PICKER_CONFIRM = "OK"
internal const val ROUTINE_RECORD_DEFAULT_DATE_PICKER_CANCEL = "Cancel"
internal const val ROUTINE_RECORD_DEFAULT_CYCLE_WEEKLY = "Weekly"
internal const val ROUTINE_RECORD_UNIT_METER = "m"

private const val DEFAULT_GOAL_LABEL = "Goal"
private const val DEFAULT_AMOUNT_PER_RECORD_LABEL = "Amount per record"
private const val DEFAULT_RECORD_AMOUNT_LABEL = "Amount"
private const val DEFAULT_RECORD_ADD = "Add record"
private const val DEFAULT_RECORD_DATE_PREFIX = "Record date, "
private const val DEFAULT_RECORD_REMOVE_PREFIX = "Remove record, "
private const val DEFAULT_COLOR_PICKER_CONFIRM = "Confirm"
private const val DATE_PICKER_SWITCH_TO_INPUT = "Switch to text input mode"
private const val DATE_PICKER_INPUT_LABEL = "Date"
private const val DATE_PICKER_CURRENT_SELECTION_PREFIX = "Current selection: "
private const val DATE_PICKER_YEAR_DIGIT_COUNT = 4
private const val DATE_PICKER_DAY_DIGIT_COUNT = 2
private const val RGB_MASK = 0xFFFFFF
private const val ALPHA_MASK = 0xFF000000.toInt()
private const val TITLE_INPUT_INDEX = 0
private const val DESCRIPTION_INPUT_INDEX = 1
private const val NOON_HOUR = 12
private const val PICKER_WAIT_ATTEMPT_COUNT = 500
private const val PICKER_WAIT_INTERVAL_MILLIS = 10L
private const val ADDED_EPOCH_SECONDS = 4_200_000_000L

// 루틴 컬러는 불투명한 ARGB를 Long으로 기록한다.
internal fun routineRecordColor(): Long = (ALPHA_MASK or Arb.int(0..RGB_MASK).next()).toLong()

internal fun routineRecordOtherColor(color: Long): Long = generateSequence { routineRecordColor() }.first { value -> value.routineRecordColorHex() != color.routineRecordColorHex() }

internal fun Long.routineRecordColorHex(): String = toColor().toHexString()

internal fun routineRecordDetailOf(
    title: String = ROUTINE_RECORD_TITLE,
    description: String = ROUTINE_RECORD_DESCRIPTION,
    color: Long = routineRecordColor(),
    startDate: LocalDate = LocalDate(2026, 3, 1),
    cycle: RoutineCycle = RoutineCycle.DAILY,
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

internal fun routineRecordOf(
    date: LocalDate,
    amount: Int = 250,
    routineId: Uuid = Arb.kotlinUuid().next(),
): RoutineRecord =
    Arb.routineRecord().next().copy(
        routineId = routineId,
        date = date,
        amount = amount,
        isDeleted = false,
    )

internal fun routineRecordLoadedPagingDataOf(recordList: List<RoutineRecord>): PagingData<RoutineRecord> =
    PagingData.from(
        data = recordList,
        sourceLoadStates =
            LoadStates(
                refresh = LoadState.NotLoading(endOfPaginationReached = true),
                prepend = LoadState.NotLoading(endOfPaginationReached = true),
                append = LoadState.NotLoading(endOfPaginationReached = true),
            ),
    )

internal fun routineRecordLoadingPagingData(): PagingData<RoutineRecord> =
    PagingData.empty(
        sourceLoadStates =
            LoadStates(
                refresh = LoadState.Loading,
                prepend = LoadState.NotLoading(endOfPaginationReached = false),
                append = LoadState.NotLoading(endOfPaginationReached = false),
            ),
    )

// 기록 내역의 순서는 날짜가 늦은 순이고, 같은 날짜는 나중에 넣은 기록이 먼저다.
private fun List<RoutineRecord>.inRecordOrder(): List<RoutineRecord> = sortedWith(compareByDescending<RoutineRecord> { record -> record.date }.thenByDescending { record -> record.createdAt })

/**
 * RoutineDetail 화면을 ViewModel 대역과 함께 배치한다. 저장된 루틴과 기록, 태그 연결은 상태 스트림으로 바꾸고 결과는 Effect 채널로 보낸다.
 */
internal class RoutineRecordTestEnvironment(
    val detail: RoutineDetail = routineRecordDetailOf(),
    val id: Uuid = Arb.kotlinUuid().next(),
    recordList: List<RoutineRecord> = emptyList(),
    tagList: List<Tag> = emptyList(),
    selectableTagList: List<Tag> = emptyList(),
    isFinished: Boolean = false,
) {
    val uiState: MutableStateFlow<RoutineDetailUiState> = MutableStateFlow(RoutineDetailUiState.Content(id = id, detail = detail, isFinished = isFinished))
    val recordUiState: MutableStateFlow<RoutineDetailRecordUiState> = MutableStateFlow(RoutineDetailRecordUiState())
    val recordList: MutableStateFlow<List<RoutineRecord>> = MutableStateFlow(recordList.inRecordOrder())
    val recordPagingData: MutableStateFlow<PagingData<RoutineRecord>> = MutableStateFlow(routineRecordLoadedPagingDataOf(this.recordList.value))
    val tagUiState: MutableStateFlow<EntityTagInputUiState> = MutableStateFlow(EntityTagInputUiState(tagList = tagList))
    val effect: Channel<RoutineDetailEffect> = Channel(Channel.BUFFERED)
    val recordEffect: Channel<RoutineDetailRecordEffect> = Channel(Channel.BUFFERED)
    val navigatedTagIdList: MutableList<Uuid> = mutableListOf()
    val routineViewModelList: MutableList<RoutineDetailViewModel> = mutableListOf()
    val recordViewModelList: MutableList<RoutineDetailRecordViewModel> = mutableListOf()
    var navigateUpCount: Int = 0
    private var addedCount: Long = 0
    private var isFinishStored: Boolean = false
    val tagAddRequestKey: Uuid = Arb.kotlinUuid().next()

    val tagViewModel: EntityDetailTagViewModel =
        mockk<EntityDetailTagViewModel>(relaxed = true).apply {
            every { uiState } returns this@RoutineRecordTestEnvironment.tagUiState
            every { tagPagingData } returns MutableStateFlow(PagingData.from(selectableTagList))
            every { selectableTagPagingData } returns MutableStateFlow(PagingData.from(selectableTagList))
        }

    val routineViewModel: RoutineDetailViewModel = newRoutineViewModel()
    val recordViewModel: RoutineDetailRecordViewModel = newRecordViewModel()

    val currentDetail: RoutineDetail
        get() = (uiState.value as RoutineDetailUiState.Content).detail

    // 메모리 정리 뒤처럼 ViewModel을 새로 만들 때도 같은 저장 상태를 바라보게 한다.
    fun newRoutineViewModel(): RoutineDetailViewModel =
        mockk<RoutineDetailViewModel>(relaxed = true)
            .apply {
                every { uiState } returns this@RoutineRecordTestEnvironment.uiState
                every { effect } returns this@RoutineRecordTestEnvironment.effect.receiveAsFlow()
                every { finish() } answers { if (isFinishStored) setFinished(isFinished = true) }
                every { restart() } answers { if (isFinishStored) setFinished(isFinished = false) }
            }.also(routineViewModelList::add)

    fun newRecordViewModel(): RoutineDetailRecordViewModel =
        mockk<RoutineDetailRecordViewModel>(relaxed = true)
            .apply {
                every { uiState } returns this@RoutineRecordTestEnvironment.recordUiState
                every { recordPagingData } returns this@RoutineRecordTestEnvironment.recordPagingData
                every { effect } returns this@RoutineRecordTestEnvironment.recordEffect.receiveAsFlow()
            }.also(recordViewModelList::add)

    fun updateDetail(transform: (RoutineDetail) -> RoutineDetail) {
        uiState.update { state -> (state as RoutineDetailUiState.Content).copy(detail = transform(state.detail)) }
    }

    fun setFinished(isFinished: Boolean) {
        uiState.update { state -> (state as RoutineDetailUiState.Content).copy(isFinished = isFinished) }
    }

    fun setFinishInProgress(isInProgress: Boolean) {
        uiState.update { state -> (state as RoutineDetailUiState.Content).copy(isFinishInProgress = isInProgress) }
    }

    // 완료와 다시 시작이 저장에 성공한 결과: 저장된 완료 여부가 바뀐다.
    fun storeOnFinish() {
        isFinishStored = true
    }

    fun setRecordList(recordList: List<RoutineRecord>) {
        this.recordList.value = recordList.inRecordOrder()
        recordPagingData.value = routineRecordLoadedPagingDataOf(this.recordList.value)
    }

    // 기록 넣기가 저장에 성공한 결과: 기록이 기기에 저장되어 기록 내역에 나타나고 성공을 알린다.
    fun completeAdd(
        date: LocalDate,
        amount: Int,
    ): RoutineRecord {
        // 임의로 만든 기록의 시각은 2100년 이전이므로, 새로 넣은 기록은 그보다 늦은 시각으로 두어 같은 날짜에서 먼저 놓이게 한다.
        val record = routineRecordOf(date = date, amount = amount, routineId = id).copy(createdAt = Instant.fromEpochSeconds(ADDED_EPOCH_SECONDS + addedCount))

        addedCount += 1

        setRecordList(recordList.value + record)
        recordEffect.trySend(RoutineDetailRecordEffect.AddSucceeded)
        return record
    }

    fun storeOnAdd() {
        every { recordViewModel.add(date = any(), amount = any()) } answers { completeAdd(date = firstArg(), amount = secondArg()) }
    }

    fun storeOnDelete() {
        every { recordViewModel.delete(recordId = any()) } answers {
            val recordId = firstArg<Uuid>()

            setRecordList(recordList.value.filterNot { record -> record.id == recordId })
        }
    }

    // 수정이 저장에 성공한 결과: 저장된 루틴이 입력한 내용으로 바뀌고 성공을 알린다.
    fun completeUpdate(input: RoutineInput) {
        updateDetail { detail ->
            detail.copy(
                title = input.title.ifBlank { detail.title },
                description = input.description,
                color = input.color,
                startDate = input.startDate,
                cycle = input.cycle,
                unit = input.unit,
                goalAmount = input.goalAmount,
                amountPerRecord = input.amountPerRecord,
            )
        }
        effect.trySend(RoutineDetailEffect.UpdateSucceeded)
    }

    fun storeOnUpdate() {
        every { routineViewModel.update(input = any()) } answers { completeUpdate(input = firstArg()) }
    }
}

@Composable
internal fun RoutineRecordTestScreen(
    environment: RoutineRecordTestEnvironment,
    routineViewModel: RoutineDetailViewModel = environment.routineViewModel,
    recordViewModel: RoutineDetailRecordViewModel = environment.recordViewModel,
) {
    RoutineTestTheme {
        RoutineDetailScreen(
            navigateUp = { environment.navigateUpCount += 1 },
            navigateToTagAdd = {},
            navigateToTagDetail = environment.navigatedTagIdList::add,
            tagAddRequestKey = environment.tagAddRequestKey,
            componentVisibleProvider = { RoutineDetailScaffoldComponentVisible() },
            routineViewModel = routineViewModel,
            recordViewModel = recordViewModel,
            tagViewModel = environment.tagViewModel,
        )
    }
}

// 기록 넣기의 처음 날짜와 진행 상태는 기기의 오늘로 정해지므로, 기기 시계를 그 날의 정오로 맞춘다.
internal fun routineRecordSetToday(date: LocalDate) {
    mockkObject(Clock.System)
    every { Clock.System.now() } returns LocalDateTime(date, LocalTime(hour = NOON_HOUR, minute = 0)).toInstant(TimeZone.currentSystemDefault())
}

internal fun routineRecordReleaseToday() {
    unmockkObject(Clock.System)
}

internal fun routineRecordUpdateButton(): SemanticsMatcher = hasContentDescription(ROUTINE_RECORD_DEFAULT_UPDATE)

internal fun routineRecordDeleteButton(): SemanticsMatcher = hasContentDescription(ROUTINE_RECORD_DEFAULT_DELETE)

internal fun routineRecordAddButton(): SemanticsMatcher = hasContentDescription(DEFAULT_RECORD_ADD)

internal fun routineRecordProgress(): SemanticsMatcher = hasAnyDescendant(SemanticsMatcher.keyIsDefined(SemanticsProperties.ProgressBarRangeInfo))

// 스낵바는 내용을 알림 영역으로 읽어 준다. 기록 내역의 빈 상태 안내도 알림 영역이므로 그 안내는 뺀다.
internal fun routineRecordSnackbar(): SemanticsMatcher = SemanticsMatcher.keyIsDefined(SemanticsProperties.LiveRegion) and !hasText(ROUTINE_RECORD_DEFAULT_EMPTY_TITLE)

internal fun routineRecordDateButton(date: LocalDate): SemanticsMatcher = hasContentDescription("$DEFAULT_RECORD_DATE_PREFIX${routineRecordDisplayText(date)}")

internal fun routineRecordAnyDateButton(): SemanticsMatcher = hasContentDescription(DEFAULT_RECORD_DATE_PREFIX, substring = true)

internal fun routineRecordRemoveButton(
    date: LocalDate,
    amountText: String,
): SemanticsMatcher = hasContentDescription("$DEFAULT_RECORD_REMOVE_PREFIX${routineRecordDisplayText(date)} $amountText")

internal fun routineRecordRow(
    date: LocalDate,
    amountText: String,
): SemanticsMatcher = hasText(routineRecordDisplayText(date)) and hasText(amountText) and hasAnyAncestor(hasTestTag(ROUTINE_RECORD_ROW_TEST_TAG))

internal fun SemanticsNodeInteractionsProvider.routineRecordTitleInput(): SemanticsNodeInteraction = onAllNodes(hasSetTextAction())[TITLE_INPUT_INDEX]

internal fun SemanticsNodeInteractionsProvider.routineRecordDescriptionInput(): SemanticsNodeInteraction = onAllNodes(hasSetTextAction())[DESCRIPTION_INPUT_INDEX]

internal fun SemanticsNodeInteractionsProvider.routineRecordGoalInput(): SemanticsNodeInteraction = onNode(hasSetTextAction() and hasText(DEFAULT_GOAL_LABEL))

internal fun SemanticsNodeInteractionsProvider.routineRecordAmountPerRecordInput(): SemanticsNodeInteraction = onNode(hasSetTextAction() and hasText(DEFAULT_AMOUNT_PER_RECORD_LABEL))

internal fun SemanticsNodeInteractionsProvider.routineRecordAmountInput(): SemanticsNodeInteraction = onNode(hasSetTextAction() and hasText(DEFAULT_RECORD_AMOUNT_LABEL))

internal fun SemanticsNodeInteraction.routineRecordEditableText(): String = fetchSemanticsNode().config[SemanticsProperties.EditableText].text

/**
 * 기록 내역의 기록을 위에서부터 빼기 버튼의 이름(`날짜 양의 문구`)으로 읽는다.
 */
internal fun SemanticsNodeInteractionsProvider.routineRecordRowNameList(): List<String> =
    onAllNodes(hasContentDescription(DEFAULT_RECORD_REMOVE_PREFIX, substring = true))
        .fetchSemanticsNodes()
        .sortedBy { node -> node.boundsInRoot.top }
        .map { node ->
            node.config[SemanticsProperties.ContentDescription]
                .first()
                .removePrefix(DEFAULT_RECORD_REMOVE_PREFIX)
        }

internal fun routineRecordRowName(
    date: LocalDate,
    amountText: String,
): String = "${routineRecordDisplayText(date)} $amountText"

internal fun ComposeContentTestRule.routineRecordInputAmount(amount: String) {
    routineRecordAmountInput().performTextReplacement(amount)
    waitForIdle()
}

internal fun ComposeContentTestRule.routineRecordClickAdd() {
    onNode(routineRecordAddButton()).performSemanticsAction(SemanticsActions.OnClick)
    waitForIdle()
}

internal fun ComposeContentTestRule.routineRecordClickRemove(
    date: LocalDate,
    amountText: String,
) {
    onNode(routineRecordRemoveButton(date = date, amountText = amountText)).performSemanticsAction(SemanticsActions.OnClick)
    waitForIdle()
}

internal fun ComposeContentTestRule.routineRecordClickUpdate() {
    onNode(routineRecordUpdateButton()).performSemanticsAction(SemanticsActions.OnClick)
    waitForIdle()
}

internal fun ComposeContentTestRule.routineRecordClickDelete() {
    onNode(routineRecordDeleteButton()).performSemanticsAction(SemanticsActions.OnClick)
    waitForIdle()
}

// 기간 카드의 날짜 버튼은 날짜를 글자로 보여 준다. 기록 넣기의 날짜 버튼은 이름으로만 읽히므로 이 탐색에 걸리지 않는다.
internal fun SemanticsNodeInteractionsProvider.routineRecordFormDateButton(date: LocalDate): SemanticsNodeInteraction = onNode(hasText(routineRecordDisplayText(date)) and hasClickAction())

internal fun ComposeContentTestRule.routineRecordOpenFormDatePicker(date: LocalDate) {
    routineRecordFormDateButton(date).performSemanticsAction(SemanticsActions.OnClick)
    waitForIdle()
}

internal fun ComposeContentTestRule.routineRecordOpenDatePicker() {
    onNode(routineRecordAnyDateButton()).performSemanticsAction(SemanticsActions.OnClick)
    waitForIdle()
}

/**
 * 열린 날짜 고르기를 글자 입력 방식으로 바꿔 다른 달의 날짜도 고른다. 기본 환경의 입력 형식은 `MM/DD/YYYY`다.
 */
internal fun ComposeContentTestRule.routineRecordTypePickerDate(date: LocalDate) {
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

internal fun ComposeContentTestRule.routineRecordConfirmDatePicker() {
    onNodeWithText(ROUTINE_RECORD_DEFAULT_DATE_PICKER_CONFIRM).performClick()
    waitForIdle()
}

internal fun ComposeContentTestRule.routineRecordCancelDatePicker() {
    onNodeWithText(ROUTINE_RECORD_DEFAULT_DATE_PICKER_CANCEL).performClick()
    waitForIdle()
}

internal fun ComposeContentTestRule.routineRecordPickDate(date: LocalDate) {
    routineRecordOpenDatePicker()
    routineRecordTypePickerDate(date)
    routineRecordConfirmDatePicker()
}

internal fun ComposeContentTestRule.routineRecordPickFormDate(
    from: LocalDate,
    to: LocalDate,
) {
    routineRecordOpenFormDatePicker(from)
    routineRecordTypePickerDate(to)
    routineRecordConfirmDatePicker()
}

// 날짜 고르기는 머리글에서 지금 가리키는 날짜를 기본 환경의 문구로 알린다.
internal fun SemanticsNodeInteractionsProvider.routineRecordPickerSelection(date: LocalDate): SemanticsNodeInteraction = onNodeWithContentDescription("$DATE_PICKER_CURRENT_SELECTION_PREFIX${routineRecordDayCellText(date)}")

internal fun SemanticsNodeInteractionsProvider.routineRecordPickerDayCell(date: LocalDate): SemanticsNodeInteraction = onNodeWithText(routineRecordDayCellText(date))

// 날짜 선택 다이얼로그의 날짜 칸은 기본 환경에서 요일과 월 이름을 모두 쓴 문구로 읽힌다.
internal fun routineRecordDayCellText(date: LocalDate): String = "${DAY_OF_WEEK_NAMES[date.dayOfWeek.isoDayNumber - 1]}, ${FULL_MONTH_NAMES[date.month.number - 1]} ${date.day}, ${date.year}"

// 기본 환경의 날짜 표시 형식과 월 이름을 고정 값으로 두어 날짜 버튼과 기록의 문구를 검증한다.
internal fun routineRecordDisplayText(date: LocalDate): String = "${SHORT_MONTH_NAMES[date.month.number - 1]} ${date.day}, ${date.year}"

private val DAY_OF_WEEK_NAMES = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")

private val FULL_MONTH_NAMES =
    listOf("January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December")

private val SHORT_MONTH_NAMES = listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")

internal fun SemanticsNodeInteractionsProvider.routineRecordDisplayedColorHex(): String =
    onAllNodes(SemanticsMatcher.keyIsDefined(SemanticsProperties.Text), useUnmergedTree = true)
        .fetchSemanticsNodes()
        .flatMap { node -> node.config[SemanticsProperties.Text] }
        .map { text -> text.text }
        .single { text -> COLOR_HEX_REGEX.matches(text) }

private val COLOR_HEX_REGEX = Regex("^#[0-9A-F]{6}$")

internal fun ComposeContentTestRule.routineRecordChangeColor(
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

// 화면 본문은 입력과 기록 내역을 한 목록에 담는다. 태그 입력 같은 가로 목록과 구분하도록 세로로 스크롤하는 목록만 찾는다.
internal fun SemanticsNodeInteractionsProvider.routineRecordContentList(): SemanticsNodeInteraction = onNode(hasScrollToIndexAction() and SemanticsMatcher.keyIsDefined(SemanticsProperties.VerticalScrollAxisRange) and !hasAnyAncestor(isDialog()))

// 본문 목록에서 기록 행은 입력, 기록 제목, 기록 넣기 행 다음에 놓인다.
// 창이 낮아 기록 넣기 행이 화면 밖에 있으면 그 행이 보이도록 본문을 내린다.
internal fun ComposeContentTestRule.routineRecordScrollToInput() {
    routineRecordContentList().performScrollToIndex(RECORD_INPUT_INDEX)
    waitForIdle()
}

internal fun ComposeContentTestRule.routineRecordScrollToRecord(index: Int) {
    routineRecordContentList().performScrollToIndex(index + RECORD_INDEX_OFFSET)
    waitForIdle()
}

private const val RECORD_INDEX_OFFSET = 3
private const val RECORD_INPUT_INDEX = 2

internal fun SemanticsNodeInteraction.routineRecordTop(): Float = fetchSemanticsNode().boundsInRoot.top

internal fun ComposeContentTestRule.routineRecordOpenTagPicker() {
    onNodeWithText(ROUTINE_RECORD_DEFAULT_TAG_LABEL).performSemanticsAction(SemanticsActions.OnClick)
    waitForIdle()
}

// 선택 목록은 페이지 단위로 준비되므로 항목이 나타날 때까지 프레임과 실제 시간을 함께 진행시킨다.
internal fun ComposeContentTestRule.routineRecordAwaitText(
    text: String,
    count: Int = 1,
) {
    repeat(PICKER_WAIT_ATTEMPT_COUNT) {
        waitForIdle()
        if (onAllNodes(hasText(text)).fetchSemanticsNodes().size >= count) return
        mainClock.advanceTimeByFrame()
        @Suppress("ForbiddenMethodCall")
        Thread.sleep(PICKER_WAIT_INTERVAL_MILLIS)
    }

    error("$text 이 나타나지 않았다")
}
