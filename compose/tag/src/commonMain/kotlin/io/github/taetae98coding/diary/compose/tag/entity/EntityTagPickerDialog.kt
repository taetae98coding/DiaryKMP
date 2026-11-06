package io.github.taetae98coding.diary.compose.tag.entity

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
import io.github.taetae98coding.diary.compose.core.preview.ScreenPreview
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.compose.tag.Res
import io.github.taetae98coding.diary.compose.tag.entity_tag_add_action
import io.github.taetae98coding.diary.compose.tag.entity_tag_picker_add_label
import io.github.taetae98coding.diary.compose.tag.entity_tag_picker_search_empty_description
import io.github.taetae98coding.diary.compose.tag.entity_tag_picker_search_empty_title
import io.github.taetae98coding.diary.compose.tag.entity_tag_picker_search_placeholder
import io.github.taetae98coding.diary.compose.tag.entity_tag_picker_title
import io.github.taetae98coding.diary.compose.tag.previewTag
import io.github.taetae98coding.diary.core.model.tag.Tag
import kotlinx.coroutines.flow.flowOf
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun EntityTagPickerDialog(
    onDismissRequest: () -> Unit,
    onEvent: (EntityTagPickerEvent) -> Unit,
    modifier: Modifier = Modifier,
    searchFieldState: DiaryPickerSearchFieldState = rememberDiaryPickerSearchFieldState(),
    tagPagingItems: LazyPagingItems<Tag> = remember { flowOf(PagingData.empty<Tag>()) }.collectAsLazyPagingItems(),
    uiStateProvider: () -> EntityTagInputUiState = { EntityTagInputUiState() },
) {
    DiaryPagingPickerDialog(
        text =
            DiaryPagingPickerText(
                title = stringResource(Res.string.entity_tag_picker_title),
                searchPlaceholder = stringResource(Res.string.entity_tag_picker_search_placeholder),
                searchEmptyTitle = stringResource(Res.string.entity_tag_picker_search_empty_title),
                searchEmptyDescription = stringResource(Res.string.entity_tag_picker_search_empty_description),
                addLabel = stringResource(Res.string.entity_tag_picker_add_label),
                addActionLabel = stringResource(Res.string.entity_tag_add_action),
            ),
        onAddClick = { onEvent(EntityTagPickerEvent.ClickAdd) },
        onDismissRequest = onDismissRequest,
        itemKey = { tag -> tag.id },
        modifier = modifier,
        searchFieldState = searchFieldState,
        pagingItems = tagPagingItems,
    ) { tag, itemModifier ->
        EntityTagPickerRow(
            onEvent = onEvent,
            tag = tag,
            isSelected = tag != null && uiStateProvider().tagList.any { selectedTag -> selectedTag.id == tag.id },
            modifier = itemModifier,
        )
    }
}

@ScreenPreview
@Composable
private fun EntityTagPickerDialogPreview() {
    val tagList = remember { listOf(previewTag(emoji = "💼", title = "업무", color = 0xFF3A7BD5)) }
    val tagPagingData = remember(tagList) { flowOf(PagingData.from(tagList)) }

    DiaryTheme {
        EntityTagPickerDialog(
            onDismissRequest = {},
            onEvent = {},
            tagPagingItems = tagPagingData.collectAsLazyPagingItems(),
            uiStateProvider = { EntityTagInputUiState(tagList = tagList) },
        )
    }
}
