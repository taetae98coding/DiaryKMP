package io.github.taetae98coding.diary.feature.tag.ui.link

import androidx.activity.ComponentDialog
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isDialog
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.height
import androidx.paging.LoadState
import androidx.paging.LoadStates
import androidx.paging.PagingData
import androidx.paging.compose.collectAsLazyPagingItems
import io.github.taetae98coding.diary.compose.core.dialog.DialogState
import io.github.taetae98coding.diary.compose.core.dialog.rememberDiaryPickerSearchFieldState
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.core.model.tag.Tag
import io.github.taetae98coding.diary.core.model.tag.TagDetail
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import org.robolectric.shadows.ShadowDialog
import kotlin.time.Instant
import kotlin.uuid.Uuid

internal const val TAG_LINK_INPUT_TAG: String = "TagLinkInput"
internal const val WORK_TAG_TITLE: String = "TagLinkWork"
internal const val EXERCISE_TAG_TITLE: String = "TagLinkExercise"
internal const val RENAMED_TAG_TITLE: String = "TagLinkRenamed"
internal const val DEFAULT_TAG_LINK_LABEL: String = "Link tag"
internal const val KOREAN_TAG_LINK_LABEL: String = "태그 연결"
internal const val DEFAULT_TAG_DETAIL_ACTION: String = "Open tag detail"
internal const val KOREAN_TAG_DETAIL_ACTION: String = "태그 상세 보기"
internal const val DEFAULT_PICKER_TITLE: String = "Link Tag"
internal const val KOREAN_PICKER_TITLE: String = KOREAN_TAG_LINK_LABEL
internal const val DEFAULT_PICKER_TAG_ADD: String = "Add tag"
internal const val WORK_TAG_QUERY: String = "Work"
internal const val DEFAULT_PICKER_SEARCH_PLACEHOLDER: String = "Search tags"
internal const val KOREAN_PICKER_SEARCH_PLACEHOLDER: String = "태그 검색"
internal const val DEFAULT_PICKER_SEARCH_EMPTY_TITLE: String = "No search results"
internal const val DEFAULT_PICKER_SEARCH_EMPTY_DESCRIPTION: String = "Try a different search query."
internal const val KOREAN_PICKER_SEARCH_EMPTY_TITLE: String = "검색 결과가 없습니다"
internal const val KOREAN_PICKER_SEARCH_EMPTY_DESCRIPTION: String = "다른 검색어로 찾아보세요"

internal fun testTag(
    title: String,
    emoji: String = "",
    isFinished: Boolean = false,
): Tag =
    Tag(
        id = Uuid.random(),
        detail = TagDetail(emoji = emoji, title = title, description = "", color = 0xFF3A7BD5),
        isFinished = isFinished,
        isDeleted = false,
        updatedAt = Instant.DISTANT_PAST,
        createdAt = Instant.DISTANT_PAST,
    )

internal fun tagPagingDataOf(tagList: List<Tag>): PagingData<Tag> =
    PagingData.from(
        data = tagList,
        sourceLoadStates =
            LoadStates(
                refresh = LoadState.NotLoading(endOfPaginationReached = true),
                prepend = LoadState.NotLoading(endOfPaginationReached = true),
                append = LoadState.NotLoading(endOfPaginationReached = true),
            ),
    )

internal fun refreshingTagPagingData(): PagingData<Tag> =
    PagingData.from(
        data = emptyList(),
        sourceLoadStates =
            LoadStates(
                refresh = LoadState.Loading,
                prepend = LoadState.NotLoading(endOfPaginationReached = false),
                append = LoadState.NotLoading(endOfPaginationReached = false),
            ),
    )

internal fun appendingTagPagingDataOf(tagList: List<Tag>): PagingData<Tag> =
    PagingData.from(
        data = tagList,
        sourceLoadStates =
            LoadStates(
                refresh = LoadState.NotLoading(endOfPaginationReached = false),
                prepend = LoadState.NotLoading(endOfPaginationReached = true),
                append = LoadState.Loading,
            ),
    )

internal fun appendFailedTagPagingDataOf(tagList: List<Tag>): PagingData<Tag> =
    PagingData.from(
        data = tagList,
        sourceLoadStates =
            LoadStates(
                refresh = LoadState.NotLoading(endOfPaginationReached = false),
                prepend = LoadState.NotLoading(endOfPaginationReached = true),
                append = LoadState.Error(IllegalStateException("append failed")),
            ),
    )

internal fun ComposeContentTestRule.setTagLinkInput(
    uiState: TagLinkInputUiState = TagLinkInputUiState(),
    onTagClick: (Uuid) -> Unit = {},
    onLinkClick: () -> Unit = {},
) {
    setContent {
        DiaryTheme {
            TagLinkInput(
                uiStateProvider = { uiState },
                onTagClick = onTagClick,
                onLinkClick = onLinkClick,
                modifier = Modifier.testTag(TAG_LINK_INPUT_TAG),
            )
        }
    }
}

/**
 * 연결한 태그를 테스트에서 바꿀 수 있도록 상태를 끌어올려 [TagLinkInput]을 배치하고, 연결을 바꾸는 함수를 돌려준다.
 */
internal fun ComposeContentTestRule.setTagLinkInputWithLink(): (List<Tag>) -> Unit {
    var uiState by mutableStateOf(TagLinkInputUiState())

    setContent {
        DiaryTheme {
            TagLinkInput(
                uiStateProvider = { uiState },
                onTagClick = {},
                onLinkClick = {},
                modifier = Modifier.testTag(TAG_LINK_INPUT_TAG),
            )
        }
    }

    return { linkedTagList ->
        uiState = uiState.copy(linkedTagList = linkedTagList)
        waitForIdle()
    }
}

internal fun ComposeContentTestRule.setTagLinkPickerDialog(
    tagList: List<Tag> = emptyList(),
    uiState: TagLinkInputUiState = TagLinkInputUiState(),
    tagPagingData: PagingData<Tag> = tagPagingDataOf(tagList),
    tagPagingDataFlow: Flow<PagingData<Tag>> = MutableStateFlow(tagPagingData),
    query: String = "",
    onDismissRequest: () -> Unit = {},
    onClickAdd: () -> Unit = {},
    onLink: (Uuid) -> Unit = {},
    onUnlink: (Uuid) -> Unit = {},
) {
    setContent {
        DiaryTheme {
            TagLinkPickerDialog(
                searchFieldState = rememberDiaryPickerSearchFieldState(initialText = query),
                tagPagingItems = remember(tagPagingDataFlow) { tagPagingDataFlow }.collectAsLazyPagingItems(),
                uiStateProvider = { uiState },
                onDismissRequest = onDismissRequest,
                onEvent = { event ->
                    when (event) {
                        is TagLinkPickerEvent.ClickAdd -> onClickAdd()
                        is TagLinkPickerEvent.Link -> onLink(event.id)
                        is TagLinkPickerEvent.Unlink -> onUnlink(event.id)
                        is TagLinkPickerEvent.ChangeQuery -> Unit
                    }
                },
            )
        }
    }
}

internal fun ComposeContentTestRule.setTagLinkPickerDialogHost(
    dialogState: DialogState = DialogState(isVisible = true),
    tagList: List<Tag> = emptyList(),
    tagPagingDataFlow: Flow<PagingData<Tag>> = MutableStateFlow(tagPagingDataOf(tagList)),
    uiState: TagLinkInputUiState = TagLinkInputUiState(),
    onQueryChange: (String) -> Unit = {},
    onClickAdd: () -> Unit = {},
    onLink: (Uuid) -> Unit = {},
    onUnlink: (Uuid) -> Unit = {},
) {
    setContent {
        DiaryTheme {
            TagLinkPickerDialogHost(
                dialogState = dialogState,
                onEvent = { event ->
                    when (event) {
                        is TagLinkPickerEvent.ClickAdd -> onClickAdd()
                        is TagLinkPickerEvent.Link -> onLink(event.id)
                        is TagLinkPickerEvent.Unlink -> onUnlink(event.id)
                        is TagLinkPickerEvent.ChangeQuery -> onQueryChange(event.query)
                    }
                },
                tagPagingItems = remember(tagPagingDataFlow) { tagPagingDataFlow }.collectAsLazyPagingItems(),
                uiStateProvider = { uiState },
            )
        }
    }
}

internal fun ComposeContentTestRule.dialogSearchField(): SemanticsNodeInteraction = onNode(hasSetTextAction() and hasAnyAncestor(isDialog()))

internal fun hasClickLabel(label: String): SemanticsMatcher =
    SemanticsMatcher("has click label $label") { node ->
        node.config.getOrNull(SemanticsActions.OnClick)?.label == label
    }

internal fun ComposeContentTestRule.tagLinkPickerList(): SemanticsNodeInteraction = onNode(hasTestTag(TAG_LINK_PICKER_LIST_TEST_TAG))

internal fun ComposeContentTestRule.dialogNodeWithText(text: String): SemanticsNodeInteraction = onNode(hasText(text) and hasAnyAncestor(isDialog()))

/**
 * 선택 목록은 페이지 단위로 준비되므로 첫 페이지가 목록에 나타날 때까지 프레임을 진행시킨다.
 */
internal fun ComposeContentTestRule.awaitTagLinkPickerRows() {
    awaitTagLinkPicker(description = "첫 페이지가 준비되지 않았다") {
        tagLinkPickerList().fetchSemanticsNode().children.isNotEmpty()
    }
}

internal fun ComposeContentTestRule.awaitTagLinkPickerRow(title: String) {
    awaitTagLinkPicker(description = "$title 항목이 목록에 나타나지 않았다") {
        onAllNodes(hasText(title) and hasAnyAncestor(isDialog())).fetchSemanticsNodes().isNotEmpty()
    }
}

internal fun ComposeContentTestRule.awaitTagLinkPickerRowGone(title: String) {
    awaitTagLinkPicker(description = "$title 항목이 목록에서 사라지지 않았다") {
        onAllNodes(hasText(title) and hasAnyAncestor(isDialog())).fetchSemanticsNodes().isEmpty()
    }
}

/**
 * 목록 갱신은 백그라운드 조회가 끝난 뒤에 반영되므로 조건을 만족할 때까지 프레임과 실제 시간을 함께 진행시킨다.
 */
private fun ComposeContentTestRule.awaitTagLinkPicker(
    description: String,
    condition: () -> Boolean,
) {
    repeat(TAG_PICKER_WAIT_ATTEMPT_COUNT) {
        waitForIdle()
        if (condition()) return
        mainClock.advanceTimeByFrame()
        @Suppress("ForbiddenMethodCall")
        Thread.sleep(TAG_PICKER_WAIT_INTERVAL_MILLIS)
    }

    error(description)
}

/**
 * 선택 목록에는 확인 버튼이 없으므로 목록을 닫는 조작인 뒤로가기를 다이얼로그 창에 전달한다.
 */
internal fun ComposeContentTestRule.closeDialogByBack() {
    val dialog = ShadowDialog.getLatestDialog() as ComponentDialog

    runOnUiThread { dialog.onBackPressedDispatcher.onBackPressed() }
    waitForIdle()
}

private const val TAG_PICKER_WAIT_ATTEMPT_COUNT: Int = 500
private const val TAG_PICKER_WAIT_INTERVAL_MILLIS: Long = 10

internal fun ComposeContentTestRule.tagLinkInputHeight(): Dp = onNodeWithTag(TAG_LINK_INPUT_TAG).getUnclippedBoundsInRoot().height
