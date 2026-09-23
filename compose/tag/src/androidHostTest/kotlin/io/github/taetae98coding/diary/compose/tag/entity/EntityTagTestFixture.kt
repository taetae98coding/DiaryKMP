package io.github.taetae98coding.diary.compose.tag.entity

import androidx.activity.ComponentDialog
import androidx.compose.foundation.text.input.TextFieldState
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
import androidx.compose.ui.test.SemanticsNodeInteractionCollection
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isDialog
import androidx.compose.ui.test.isToggleable
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.height
import androidx.paging.LoadState
import androidx.paging.LoadStates
import androidx.paging.PagingData
import androidx.paging.compose.collectAsLazyPagingItems
import io.github.taetae98coding.diary.compose.core.dialog.DialogState
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.core.model.tag.Tag
import io.github.taetae98coding.diary.core.model.tag.TagDetail
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import org.robolectric.shadows.ShadowDialog
import kotlin.time.Instant
import kotlin.uuid.Uuid

internal const val ENTITY_TAG_INPUT_TAG: String = "EntityTagInput"
internal const val WORK_TAG_TITLE: String = "EntityTagWork"
internal const val EXERCISE_TAG_TITLE: String = "EntityTagExercise"
internal const val RENAMED_TAG_TITLE: String = "EntityTagRenamed"
internal const val DEFAULT_ENTITY_TAG_LABEL: String = "Select tag"
internal const val KOREAN_ENTITY_TAG_LABEL: String = "태그 선택"
internal const val DEFAULT_TAG_DETAIL_ACTION: String = "Open tag detail"
internal const val KOREAN_TAG_DETAIL_ACTION: String = "태그 상세 보기"
internal const val DEFAULT_PICKER_TITLE: String = "Select Tag"
internal const val DEFAULT_PICKER_TAG_ADD: String = "Add tag"
internal const val WORK_TAG_QUERY: String = "Work"
internal const val DEFAULT_PICKER_SEARCH_PLACEHOLDER: String = "Search tags"
internal const val KOREAN_PICKER_SEARCH_PLACEHOLDER: String = "태그 검색"
internal const val DEFAULT_PICKER_SEARCH_EMPTY_TITLE: String = "No search results"
internal const val DEFAULT_PICKER_SEARCH_EMPTY_DESCRIPTION: String = "Try a different search query."
internal const val KOREAN_ENTITY_PICKER_SEARCH_EMPTY_TITLE: String = "검색 결과가 없습니다"
internal const val KOREAN_ENTITY_PICKER_SEARCH_EMPTY_DESCRIPTION: String = "다른 검색어로 찾아보세요"

internal fun entityTestTag(
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

internal fun entityTagPagingDataOf(tagList: List<Tag>): PagingData<Tag> =
    PagingData.from(
        data = tagList,
        sourceLoadStates =
            LoadStates(
                refresh = LoadState.NotLoading(endOfPaginationReached = true),
                prepend = LoadState.NotLoading(endOfPaginationReached = true),
                append = LoadState.NotLoading(endOfPaginationReached = true),
            ),
    )

internal fun appendingEntityTagPagingDataOf(tagList: List<Tag>): PagingData<Tag> =
    PagingData.from(
        data = tagList,
        sourceLoadStates =
            LoadStates(
                refresh = LoadState.NotLoading(endOfPaginationReached = false),
                prepend = LoadState.NotLoading(endOfPaginationReached = true),
                append = LoadState.Loading,
            ),
    )

internal fun appendFailedEntityTagPagingDataOf(tagList: List<Tag>): PagingData<Tag> =
    PagingData.from(
        data = tagList,
        sourceLoadStates =
            LoadStates(
                refresh = LoadState.NotLoading(endOfPaginationReached = false),
                prepend = LoadState.NotLoading(endOfPaginationReached = true),
                append = LoadState.Error(IllegalStateException("append failed")),
            ),
    )

internal fun ComposeContentTestRule.setEntityTagInput(
    uiState: EntityTagInputUiState = EntityTagInputUiState(),
    onTagClick: (Uuid) -> Unit = {},
    onAddClick: () -> Unit = {},
) {
    setContent {
        DiaryTheme {
            EntityTagInput(
                uiStateProvider = { uiState },
                onTagClick = onTagClick,
                onAddClick = onAddClick,
                modifier = Modifier.testTag(ENTITY_TAG_INPUT_TAG),
            )
        }
    }
}

/**
 * 연결한 태그를 테스트에서 바꿀 수 있도록 상태를 끌어올려 [EntityTagInput]을 배치하고, 연결을 바꾸는 함수를 돌려준다.
 */
internal fun ComposeContentTestRule.setEntityTagInputWithTagList(): (List<Tag>) -> Unit {
    var uiState by mutableStateOf(EntityTagInputUiState())

    setContent {
        DiaryTheme {
            EntityTagInput(
                uiStateProvider = { uiState },
                onTagClick = {},
                onAddClick = {},
                modifier = Modifier.testTag(ENTITY_TAG_INPUT_TAG),
            )
        }
    }

    return { tagList ->
        uiState = uiState.copy(tagList = tagList)
        waitForIdle()
    }
}

internal fun ComposeContentTestRule.setEntityTagPickerDialog(
    tagList: List<Tag> = emptyList(),
    uiState: EntityTagInputUiState = EntityTagInputUiState(),
    tagPagingData: PagingData<Tag> = entityTagPagingDataOf(tagList),
    tagPagingDataFlow: Flow<PagingData<Tag>> = MutableStateFlow(tagPagingData),
    queryState: TextFieldState = TextFieldState(),
    onDismissRequest: () -> Unit = {},
    onClickAdd: () -> Unit = {},
    onAdd: (Uuid) -> Unit = {},
    onRemove: (Uuid) -> Unit = {},
) {
    setContent {
        DiaryTheme {
            EntityTagPickerDialog(
                queryState = queryState,
                tagPagingItems = remember(tagPagingDataFlow) { tagPagingDataFlow }.collectAsLazyPagingItems(),
                uiStateProvider = { uiState },
                onDismissRequest = onDismissRequest,
                onEvent = { event ->
                    when (event) {
                        is EntityTagPickerEvent.ClickAdd -> onClickAdd()
                        is EntityTagPickerEvent.Add -> onAdd(event.id)
                        is EntityTagPickerEvent.Remove -> onRemove(event.id)
                        is EntityTagPickerEvent.ChangeQuery -> Unit
                    }
                },
            )
        }
    }
}

internal fun ComposeContentTestRule.setEntityTagPickerDialogHost(
    dialogState: DialogState = DialogState(isVisible = true),
    tagList: List<Tag> = emptyList(),
    tagPagingDataFlow: Flow<PagingData<Tag>> = MutableStateFlow(entityTagPagingDataOf(tagList)),
    uiState: EntityTagInputUiState = EntityTagInputUiState(),
    onQueryChange: (String) -> Unit = {},
    onClickAdd: () -> Unit = {},
    onAdd: (Uuid) -> Unit = {},
    onRemove: (Uuid) -> Unit = {},
) {
    setContent {
        DiaryTheme {
            EntityTagPickerDialogHost(
                dialogState = dialogState,
                onEvent = { event ->
                    when (event) {
                        is EntityTagPickerEvent.ClickAdd -> onClickAdd()
                        is EntityTagPickerEvent.Add -> onAdd(event.id)
                        is EntityTagPickerEvent.Remove -> onRemove(event.id)
                        is EntityTagPickerEvent.ChangeQuery -> onQueryChange(event.query)
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

internal fun ComposeContentTestRule.entityTagPickerList(): SemanticsNodeInteraction = onNode(hasTestTag(ENTITY_TAG_PICKER_LIST_TEST_TAG))

internal fun ComposeContentTestRule.dialogNodeWithText(text: String): SemanticsNodeInteraction = onNode(hasText(text) and hasAnyAncestor(isDialog()))

/**
 * 선택 목록은 페이지 단위로 준비되므로 첫 페이지가 목록에 나타날 때까지 프레임을 진행시킨다.
 */
internal fun ComposeContentTestRule.awaitEntityTagPickerRows() {
    awaitEntityTagPicker(description = "첫 페이지가 준비되지 않았다") {
        entityTagPickerList().fetchSemanticsNode().children.isNotEmpty()
    }
}

/**
 * 목록 갱신은 백그라운드 조회가 끝난 뒤에 반영되므로 조건을 만족할 때까지 프레임과 실제 시간을 함께 진행시킨다.
 */
private fun ComposeContentTestRule.awaitEntityTagPicker(
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

internal fun ComposeContentTestRule.pickerRows(): SemanticsNodeInteractionCollection = onAllNodes(isToggleable() and hasAnyAncestor(isDialog()))

internal fun ComposeContentTestRule.pickerList(): SemanticsNodeInteraction = entityTagPickerList()

internal fun ComposeContentTestRule.entityTagInputHeight(): Dp = onNodeWithTag(ENTITY_TAG_INPUT_TAG).getUnclippedBoundsInRoot().height
