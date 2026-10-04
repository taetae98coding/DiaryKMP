package io.github.taetae98coding.diary.feature.tag.ui.link

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
import io.github.taetae98coding.diary.compose.tag.previewTag
import io.github.taetae98coding.diary.core.model.tag.Tag
import io.github.taetae98coding.diary.feature.tag.ui.Res
import io.github.taetae98coding.diary.feature.tag.ui.tag_link_picker_add_action
import io.github.taetae98coding.diary.feature.tag.ui.tag_link_picker_add_label
import io.github.taetae98coding.diary.feature.tag.ui.tag_link_picker_search_empty_description
import io.github.taetae98coding.diary.feature.tag.ui.tag_link_picker_search_empty_title
import io.github.taetae98coding.diary.feature.tag.ui.tag_link_picker_search_placeholder
import io.github.taetae98coding.diary.feature.tag.ui.tag_link_picker_title
import kotlinx.coroutines.flow.flowOf
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun TagLinkPickerDialog(
    onDismissRequest: () -> Unit,
    onEvent: (TagLinkPickerEvent) -> Unit,
    modifier: Modifier = Modifier,
    searchFieldState: DiaryPickerSearchFieldState = rememberDiaryPickerSearchFieldState(),
    tagPagingItems: LazyPagingItems<Tag> = remember { flowOf(PagingData.empty<Tag>()) }.collectAsLazyPagingItems(),
    uiStateProvider: () -> TagLinkInputUiState = { TagLinkInputUiState() },
) {
    DiaryPagingPickerDialog(
        text =
            DiaryPagingPickerText(
                title = stringResource(Res.string.tag_link_picker_title),
                searchPlaceholder = stringResource(Res.string.tag_link_picker_search_placeholder),
                searchEmptyTitle = stringResource(Res.string.tag_link_picker_search_empty_title),
                searchEmptyDescription = stringResource(Res.string.tag_link_picker_search_empty_description),
                addLabel = stringResource(Res.string.tag_link_picker_add_label),
                addActionLabel = stringResource(Res.string.tag_link_picker_add_action),
            ),
        onAddClick = { onEvent(TagLinkPickerEvent.ClickAdd) },
        onDismissRequest = onDismissRequest,
        itemKey = { tag -> tag.id },
        modifier = modifier,
        searchFieldState = searchFieldState,
        pagingItems = tagPagingItems,
    ) { tag, itemModifier ->
        TagLinkPickerRow(
            onEvent = onEvent,
            tag = tag,
            isLinked = tag != null && uiStateProvider().linkedTagList.any { linkedTag -> linkedTag.id == tag.id },
            modifier = itemModifier,
        )
    }
}

@ScreenPreview
@Composable
private fun TagLinkPickerDialogPreview() {
    val tagList = remember { listOf(previewTag(emoji = "💼", title = "업무", color = 0xFF3A7BD5)) }
    val tagPagingData = remember(tagList) { flowOf(PagingData.from(tagList)) }

    DiaryTheme {
        TagLinkPickerDialog(
            onDismissRequest = {},
            onEvent = {},
            tagPagingItems = tagPagingData.collectAsLazyPagingItems(),
            uiStateProvider = { TagLinkInputUiState(linkedTagList = tagList) },
        )
    }
}
