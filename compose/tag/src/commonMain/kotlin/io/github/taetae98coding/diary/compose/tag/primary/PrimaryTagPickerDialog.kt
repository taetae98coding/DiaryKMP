package io.github.taetae98coding.diary.compose.tag.primary

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.paging.PagingData
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import io.github.taetae98coding.diary.compose.core.dialog.DiaryPagingPickerDialog
import io.github.taetae98coding.diary.compose.core.dialog.DiaryPagingPickerText
import io.github.taetae98coding.diary.compose.core.dialog.DiaryPickerSearchFieldState
import io.github.taetae98coding.diary.compose.core.dialog.rememberDiaryPickerSearchFieldState
import io.github.taetae98coding.diary.compose.tag.Res
import io.github.taetae98coding.diary.compose.tag.previewTag
import io.github.taetae98coding.diary.compose.tag.primary_tag_add_action
import io.github.taetae98coding.diary.compose.tag.primary_tag_picker_add_label
import io.github.taetae98coding.diary.compose.tag.primary_tag_picker_search_empty_description
import io.github.taetae98coding.diary.compose.tag.primary_tag_picker_search_empty_title
import io.github.taetae98coding.diary.compose.tag.primary_tag_picker_search_placeholder
import io.github.taetae98coding.diary.compose.tag.primary_tag_picker_title
import io.github.taetae98coding.diary.compose.theme.DiaryTheme
import io.github.taetae98coding.diary.compose.theme.preview.ScreenPreview
import io.github.taetae98coding.diary.core.model.tag.Tag
import kotlinx.coroutines.flow.flowOf
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun PrimaryTagPickerDialog(
    onDismissRequest: () -> Unit,
    onEvent: (PrimaryTagPickerEvent) -> Unit,
    modifier: Modifier = Modifier,
    searchFieldState: DiaryPickerSearchFieldState = rememberDiaryPickerSearchFieldState(),
    tagPagingItems: LazyPagingItems<Tag> = remember { flowOf(PagingData.empty<Tag>()) }.collectAsLazyPagingItems(),
    uiStateProvider: () -> PrimaryTagInputUiState = { PrimaryTagInputUiState() },
) {
    DiaryPagingPickerDialog(
        text =
            DiaryPagingPickerText(
                title = stringResource(Res.string.primary_tag_picker_title),
                searchPlaceholder = stringResource(Res.string.primary_tag_picker_search_placeholder),
                searchEmptyTitle = stringResource(Res.string.primary_tag_picker_search_empty_title),
                searchEmptyDescription = stringResource(Res.string.primary_tag_picker_search_empty_description),
                addLabel = stringResource(Res.string.primary_tag_picker_add_label),
                addActionLabel = stringResource(Res.string.primary_tag_add_action),
            ),
        onAddClick = { onEvent(PrimaryTagPickerEvent.ClickAdd) },
        onDismissRequest = onDismissRequest,
        itemKey = { tag -> tag.id },
        modifier = modifier,
        searchFieldState = searchFieldState,
        pagingItems = tagPagingItems,
        isSearchFocusRequested = true,
    ) { tag, itemModifier ->
        val uiState = uiStateProvider()

        PrimaryTagPickerRow(
            onEvent = onEvent,
            tag = tag,
            isSelected = tag != null && uiState.selectedTagList.any { selectedTag -> selectedTag.id == tag.id },
            isPrimary = tag != null && tag.id == uiState.primaryTagId,
            modifier = itemModifier,
        )
    }
}

@ScreenPreview
@Composable
private fun PrimaryTagPickerDialogPreview() {
    val tagList = remember { listOf(previewTag(emoji = "💼", title = "업무", color = 0xFF3A7BD5)) }
    val tagPagingData = remember(tagList) { flowOf(PagingData.from(tagList)) }

    DiaryTheme {
        PrimaryTagPickerDialog(
            onDismissRequest = {},
            onEvent = {},
            tagPagingItems = tagPagingData.collectAsLazyPagingItems(),
            uiStateProvider = { PrimaryTagInputUiState(selectedTagList = tagList, primaryTagId = tagList.first().id) },
        )
    }
}
