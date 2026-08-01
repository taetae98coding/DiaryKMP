package io.github.taetae98coding.diary.feature.memo.ui.tag

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
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isDialog
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.height
import androidx.paging.PagingData
import androidx.paging.compose.collectAsLazyPagingItems
import io.github.taetae98coding.diary.compose.core.dialog.DialogState
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.core.model.tag.Tag
import io.github.taetae98coding.diary.core.model.tag.TagDetail
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlin.time.Instant
import kotlin.uuid.Uuid

internal const val MEMO_TAG_INPUT_TAG: String = "MemoTagInput"
internal const val WORK_TAG_TITLE: String = "MemoTagWork"
internal const val EXERCISE_TAG_TITLE: String = "MemoTagExercise"
internal const val WORK_TAG_QUERY: String = "Work"
internal const val KOREAN_TAG_SELECT_LABEL: String = "태그 선택"
internal const val DEFAULT_TAG_SELECT_LABEL: String = "Select tag"
internal const val KOREAN_SELECT_ACTION: String = KOREAN_TAG_SELECT_LABEL
internal const val DEFAULT_SELECT_ACTION: String = DEFAULT_TAG_SELECT_LABEL
internal const val KOREAN_TAG_DETAIL_ACTION: String = "태그 상세 보기"
internal const val DEFAULT_TAG_DETAIL_ACTION: String = "Open tag detail"
internal const val DEFAULT_PICKER_TITLE: String = "Select Tag"
internal const val KOREAN_PICKER_TITLE: String = KOREAN_TAG_SELECT_LABEL
internal const val KOREAN_PICKER_TAG_ADD: String = "태그 추가"
internal const val DEFAULT_PICKER_TAG_ADD: String = "Add tag"
internal const val DEFAULT_CONFIRM: String = "Confirm"
internal const val DEFAULT_PRIMARY_TAG_DESCRIPTION: String = "Primary tag"
internal const val DEFAULT_PRIMARY_SET_DESCRIPTION: String = "Set as primary tag"
internal const val DEFAULT_PRIMARY_UNSET_DESCRIPTION: String = "Unset primary tag"
internal const val KOREAN_PRIMARY_SET_DESCRIPTION: String = "대표 태그 지정"
internal const val KOREAN_PRIMARY_UNSET_DESCRIPTION: String = "대표 태그 지정 해제"
internal const val DEFAULT_PICKER_SEARCH_PLACEHOLDER: String = "Search tags"
internal const val KOREAN_PICKER_SEARCH_PLACEHOLDER: String = "태그 검색"
internal const val DEFAULT_PICKER_SEARCH_EMPTY_TITLE: String = "No search results"
internal const val DEFAULT_PICKER_SEARCH_EMPTY_DESCRIPTION: String = "Try a different search query."
internal const val KOREAN_PICKER_SEARCH_EMPTY_TITLE: String = "검색 결과가 없습니다"
internal const val KOREAN_PICKER_SEARCH_EMPTY_DESCRIPTION: String = "다른 검색어로 찾아보세요"

internal fun testTag(
    title: String,
    emoji: String = "",
    updatedAt: Instant = Instant.DISTANT_PAST,
    createdAt: Instant = Instant.DISTANT_PAST,
): Tag =
    Tag(
        id = Uuid.random(),
        detail = TagDetail(emoji = emoji, title = title, description = "", color = 0xFF3A7BD5),
        isFinished = false,
        isDeleted = false,
        updatedAt = updatedAt,
        createdAt = createdAt,
    )

internal fun ComposeContentTestRule.setMemoTagInput(
    uiState: MemoTagInputUiState = MemoTagInputUiState(),
    onTagClick: (Uuid) -> Unit = {},
    onAddClick: () -> Unit = {},
) {
    setContent {
        DiaryTheme {
            MemoTagInput(
                uiStateProvider = { uiState },
                onTagClick = onTagClick,
                onAddClick = onAddClick,
                modifier = Modifier.testTag(MEMO_TAG_INPUT_TAG),
            )
        }
    }
}

/**
 * 선택한 태그를 테스트에서 바꿀 수 있도록 상태를 끌어올려 [MemoTagInput]을 배치하고, 선택을 바꾸는 함수를 돌려준다.
 */
internal fun ComposeContentTestRule.setMemoTagInputWithSelection(): (List<Tag>, Uuid?) -> Unit {
    var uiState by mutableStateOf(MemoTagInputUiState())

    setContent {
        DiaryTheme {
            MemoTagInput(
                uiStateProvider = { uiState },
                onTagClick = {},
                onAddClick = {},
                modifier = Modifier.testTag(MEMO_TAG_INPUT_TAG),
            )
        }
    }

    return { selectedTagList, primaryTagId ->
        uiState = uiState.copy(selectedTagList = selectedTagList, primaryTagId = primaryTagId)
        waitForIdle()
    }
}

internal fun ComposeContentTestRule.setMemoTagPickerDialog(
    tagList: List<Tag> = emptyList(),
    uiState: MemoTagInputUiState = MemoTagInputUiState(),
    tagPagingData: PagingData<Tag> = tagPagingDataOf(tagList),
    tagPagingDataFlow: Flow<PagingData<Tag>> = MutableStateFlow(tagPagingData),
    queryState: TextFieldState = TextFieldState(),
    onDismissRequest: () -> Unit = {},
    onTagSelect: (Uuid) -> Unit = {},
    onTagUnselect: (Uuid) -> Unit = {},
    onPrimaryTagSelect: (Uuid) -> Unit = {},
    onPrimaryTagUnselect: () -> Unit = {},
    onTagAdd: () -> Unit = {},
) {
    setContent {
        DiaryTheme {
            MemoTagPickerDialog(
                queryState = queryState,
                tagPagingItems = remember(tagPagingDataFlow) { tagPagingDataFlow }.collectAsLazyPagingItems(),
                uiStateProvider = { uiState },
                onDismissRequest = onDismissRequest,
                onEvent = { event ->
                    when (event) {
                        is MemoTagPickerEvent.Select -> onTagSelect(event.id)
                        is MemoTagPickerEvent.Unselect -> onTagUnselect(event.id)
                        is MemoTagPickerEvent.SelectPrimary -> onPrimaryTagSelect(event.id)
                        is MemoTagPickerEvent.UnselectPrimary -> onPrimaryTagUnselect()
                        is MemoTagPickerEvent.ClickAdd -> onTagAdd()
                        is MemoTagPickerEvent.ChangeQuery -> Unit
                    }
                },
            )
        }
    }
}

internal fun ComposeContentTestRule.setMemoTagPickerDialogHost(
    dialogState: DialogState = DialogState(isVisible = true),
    tagList: List<Tag> = emptyList(),
    tagPagingDataFlow: Flow<PagingData<Tag>> = MutableStateFlow(tagPagingDataOf(tagList)),
    uiState: MemoTagInputUiState = MemoTagInputUiState(),
    onQueryChange: (String) -> Unit = {},
    onTagSelect: (Uuid) -> Unit = {},
    onPrimaryTagSelect: (Uuid) -> Unit = {},
    onTagAdd: () -> Unit = {},
) {
    setContent {
        DiaryTheme {
            MemoTagPickerDialogHost(
                dialogState = dialogState,
                onEvent = { event ->
                    when (event) {
                        is MemoTagPickerEvent.Select -> onTagSelect(event.id)

                        is MemoTagPickerEvent.SelectPrimary -> onPrimaryTagSelect(event.id)

                        is MemoTagPickerEvent.ClickAdd -> onTagAdd()

                        is MemoTagPickerEvent.ChangeQuery -> onQueryChange(event.query)

                        is MemoTagPickerEvent.Unselect,
                        is MemoTagPickerEvent.UnselectPrimary,
                        -> Unit
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

internal fun ComposeContentTestRule.dialogNodeWithText(text: String): SemanticsNodeInteraction = onNode(hasText(text) and hasAnyAncestor(isDialog()))

internal fun ComposeContentTestRule.dialogNodesWithContentDescription(contentDescription: String): SemanticsNodeInteractionCollection = onAllNodes(hasContentDescription(contentDescription) and hasAnyAncestor(isDialog()))

internal fun ComposeContentTestRule.memoTagInputHeight(): Dp = onNodeWithTag(MEMO_TAG_INPUT_TAG).getUnclippedBoundsInRoot().height
