package io.github.taetae98coding.diary.feature.dday.ui.detail

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasAnyDescendant
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isDialog
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onLast
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.test.performTextReplacement
import androidx.paging.PagingData
import io.github.taetae98coding.diary.compose.tag.entity.EntityTagInputUiState
import io.github.taetae98coding.diary.core.model.dday.DDayDetail
import io.github.taetae98coding.diary.core.model.dday.DDayOverflowPolicy
import io.github.taetae98coding.diary.core.model.tag.Tag
import io.github.taetae98coding.diary.feature.core.tag.EntityDetailTagViewModel
import io.github.taetae98coding.diary.feature.dday.ui.form.DDAY_FORM_DEFAULT_TAG_LABEL
import io.github.taetae98coding.diary.feature.dday.ui.form.DDayFormTestTheme
import io.github.taetae98coding.diary.feature.dday.ui.form.ddayProgressIndicator
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
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlin.uuid.Uuid

internal const val DDAY_DETAIL_MARRIAGE = "결혼"
internal const val DDAY_DETAIL_ANNIVERSARY = "결혼기념일"
internal const val DDAY_DETAIL_US = "우리"
internal const val DDAY_DETAIL_DEFAULT_UPDATE = "Update D-Day"
internal const val DDAY_DETAIL_DEFAULT_DELETE = "Delete D-Day"
internal const val DDAY_DETAIL_DEFAULT_LOADING = "Loading D-Day"
internal const val DDAY_DETAIL_DEFAULT_UPDATE_SUCCEEDED = "D-Day updated."
internal const val DDAY_DETAIL_KOREAN_UPDATE = "디데이 수정"
internal const val DDAY_DETAIL_KOREAN_UPDATE_SUCCEEDED = "디데이가 수정되었습니다."

private const val RGB_MASK = 0xFFFFFF
private const val ALPHA_MASK = 0xFF000000.toInt()
private const val PICKER_WAIT_ATTEMPT_COUNT = 500
private const val PICKER_WAIT_INTERVAL_MILLIS = 10L
private const val DEFAULT_COLOR_PICKER_CONFIRM = "Confirm"

// 디데이 컬러는 불투명한 ARGB를 Long으로 기록한다.
internal fun ddayDetailColor(): Long = (ALPHA_MASK or Arb.int(0..RGB_MASK).next()).toLong()

internal fun ddayDetailOtherColor(color: Long): Long = generateSequence { ddayDetailColor() }.first { value -> value.colorHex() != color.colorHex() }

internal fun Long.colorHex(): String = toColor().toHexString()

internal fun Color.colorHex(): String = toHexString()

internal fun ddayDetailOf(
    title: String = DDAY_DETAIL_MARRIAGE,
    description: String = DDAY_DETAIL_US,
    color: Long = ddayDetailColor(),
    baseDate: LocalDate = LocalDate(2025, 1, 15),
    countsBaseDay: Boolean = false,
    repeats: Set<DateTimeUnit.DateBased> = setOf(DateTimeUnit.DayBased(days = 100)),
    endDate: LocalDate? = LocalDate(2030, 1, 15),
    overflowPolicy: DDayOverflowPolicy = DDayOverflowPolicy.SKIP,
): DDayDetail =
    DDayDetail(
        title = title,
        description = description,
        color = color,
        baseDate = baseDate,
        countsBaseDay = countsBaseDay,
        repeats = repeats,
        endDate = endDate,
        overflowPolicy = overflowPolicy,
    )

/**
 * DDayDetail 화면을 ViewModel 대역과 함께 배치한다. 저장된 디데이와 태그 연결은 상태 스트림으로 바꾸고, 결과는 Effect 채널로 보낸다.
 */
internal class DDayDetailTestEnvironment(
    val detail: DDayDetail = ddayDetailOf(),
    val id: Uuid = Arb.kotlinUuid().next(),
    val tagAddRequestKey: Uuid = Arb.kotlinUuid().next(),
    tagList: List<Tag> = emptyList(),
    selectableTagList: List<Tag> = emptyList(),
) {
    val uiState: MutableStateFlow<DDayDetailUiState> = MutableStateFlow(DDayDetailUiState.Content(id = id, detail = detail))
    val tagUiState: MutableStateFlow<EntityTagInputUiState> = MutableStateFlow(EntityTagInputUiState(tagList = tagList))
    val effect: Channel<DDayDetailEffect> = Channel(Channel.BUFFERED)
    val navigatedTagIdList: MutableList<Uuid> = mutableListOf()
    val viewModelList: MutableList<DDayDetailViewModel> = mutableListOf()
    var navigateUpCount: Int = 0

    val tagViewModel: EntityDetailTagViewModel =
        mockk<EntityDetailTagViewModel>(relaxed = true).apply {
            every { uiState } returns this@DDayDetailTestEnvironment.tagUiState
            every { tagPagingData } returns MutableStateFlow(PagingData.from(selectableTagList))
            every { selectableTagPagingData } returns MutableStateFlow(PagingData.from(selectableTagList))
        }

    val ddayViewModel: DDayDetailViewModel = newViewModel()

    // 메모리 정리 뒤처럼 ViewModel을 새로 만들 때도 같은 저장 상태를 바라보게 한다.
    fun newViewModel(): DDayDetailViewModel =
        mockk<DDayDetailViewModel>(relaxed = true)
            .apply {
                every { uiState } returns this@DDayDetailTestEnvironment.uiState
                every { effect } returns this@DDayDetailTestEnvironment.effect.receiveAsFlow()
            }.also(viewModelList::add)

    fun content(
        detail: DDayDetail = this.detail,
        isUpdateInProgress: Boolean = false,
        isDeleteInProgress: Boolean = false,
    ): DDayDetailUiState.Content = DDayDetailUiState.Content(id = id, detail = detail, isUpdateInProgress = isUpdateInProgress, isDeleteInProgress = isDeleteInProgress)
}

@Composable
internal fun DDayDetailTestScreen(
    environment: DDayDetailTestEnvironment,
    viewModel: DDayDetailViewModel = environment.ddayViewModel,
) {
    DDayFormTestTheme {
        DDayDetailScreen(
            navigateUp = { environment.navigateUpCount += 1 },
            navigateToTagAdd = {},
            navigateToTagDetail = environment.navigatedTagIdList::add,
            tagAddRequestKey = environment.tagAddRequestKey,
            componentVisibleProvider = { DDayDetailScaffoldComponentVisible() },
            ddayViewModel = viewModel,
            tagViewModel = environment.tagViewModel,
        )
    }
}

internal fun ddayDetailTopBarTitle(text: String): SemanticsMatcher = hasText(text) and !hasSetTextAction() and !hasAnyAncestor(hasSetTextAction())

internal fun ddayDetailUpdateButton(): SemanticsMatcher = hasContentDescription(DDAY_DETAIL_DEFAULT_UPDATE)

internal fun ddayDetailDeleteButton(): SemanticsMatcher = hasContentDescription(DDAY_DETAIL_DEFAULT_DELETE)

internal fun hasDDayDetailProgress(): SemanticsMatcher = hasAnyDescendant(ddayProgressIndicator())

internal fun ComposeContentTestRule.ddayDetailClickUpdate() {
    onNode(ddayDetailUpdateButton()).performSemanticsAction(SemanticsActions.OnClick)
    waitForIdle()
}

internal fun ComposeContentTestRule.ddayDetailClickDelete() {
    onNode(ddayDetailDeleteButton()).performClick()
    waitForIdle()
}

internal fun ComposeContentTestRule.ddayDetailChangeColor(
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

// 태그 입력은 본문의 마지막 입력이라 화면 아래에 놓일 수 있으므로, 누르는 위치와 무관한 클릭 동작으로 누른다.
internal fun ComposeContentTestRule.ddayDetailOpenTagPicker() {
    onNodeWithText(DDAY_FORM_DEFAULT_TAG_LABEL).performSemanticsAction(SemanticsActions.OnClick)
    waitForIdle()
}

internal fun ComposeContentTestRule.ddayDetailLinkTag(title: String) {
    ddayDetailOpenTagPicker()
    awaitTagPickerRow(title = title)
    onNodeWithText(title).performClick()
    waitForIdle()
}

// 연결된 태그는 태그 입력과 선택 목록에 모두 나타나므로, 선택 목록의 마지막 노드를 눌러 해제한다.
internal fun ComposeContentTestRule.ddayDetailUnlinkTag(title: String) {
    ddayDetailOpenTagPicker()
    awaitTagPickerRow(title = title, count = 2)
    onAllNodesWithText(title).onLast().performClick()
    waitForIdle()
}

// 선택 목록은 페이지 단위로 준비되므로 항목이 나타날 때까지 프레임과 실제 시간을 함께 진행시킨다.
private fun ComposeContentTestRule.awaitTagPickerRow(
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

internal fun SemanticsNodeInteraction.ddayDetailTop(): Float = fetchSemanticsNode().boundsInRoot.top
