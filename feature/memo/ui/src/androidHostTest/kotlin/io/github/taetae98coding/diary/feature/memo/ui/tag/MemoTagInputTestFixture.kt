package io.github.taetae98coding.diary.feature.memo.ui.tag

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsProperties
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
import io.github.taetae98coding.diary.compose.core.dialog.rememberDiaryPickerSearchFieldState
import io.github.taetae98coding.diary.compose.tag.primary.PrimaryTagInputUiState
import io.github.taetae98coding.diary.compose.tag.primary.PrimaryTagPickerDialogHost
import io.github.taetae98coding.diary.compose.tag.primary.PrimaryTagPickerEvent
import io.github.taetae98coding.diary.compose.theme.DiaryTheme
import io.github.taetae98coding.diary.core.model.tag.Tag
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
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
internal const val DEFAULT_PRIMARY_TAG_STATE_DESCRIPTION: String = "Primary tag"
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

internal fun ComposeContentTestRule.setMemoTagPickerDialogHost(
    dialogState: DialogState = DialogState(isVisible = true),
    tagList: List<Tag> = emptyList(),
    tagPagingDataFlow: Flow<PagingData<Tag>> = MutableStateFlow(tagPagingDataOf(tagList)),
    uiState: PrimaryTagInputUiState = PrimaryTagInputUiState(),
    onQueryChange: (String) -> Unit = {},
    onTagSelect: (Uuid) -> Unit = {},
    onPrimaryTagSelect: (Uuid) -> Unit = {},
    onTagAdd: () -> Unit = {},
) {
    setContent {
        DiaryTheme {
            PrimaryTagPickerDialogHost(
                dialogState = dialogState,
                onEvent = { event ->
                    when (event) {
                        is PrimaryTagPickerEvent.Select -> onTagSelect(event.id)

                        is PrimaryTagPickerEvent.SelectPrimary -> onPrimaryTagSelect(event.id)

                        is PrimaryTagPickerEvent.ClickAdd -> onTagAdd()

                        is PrimaryTagPickerEvent.ChangeQuery -> onQueryChange(event.query)

                        is PrimaryTagPickerEvent.Unselect,
                        is PrimaryTagPickerEvent.UnselectPrimary,
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

internal fun hasPrimaryTagState(): SemanticsMatcher = SemanticsMatcher.expectValue(SemanticsProperties.StateDescription, DEFAULT_PRIMARY_TAG_STATE_DESCRIPTION)
