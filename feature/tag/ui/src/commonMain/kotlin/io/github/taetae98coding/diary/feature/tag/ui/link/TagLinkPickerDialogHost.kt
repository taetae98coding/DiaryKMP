package io.github.taetae98coding.diary.feature.tag.ui.link

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.paging.PagingData
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import io.github.taetae98coding.diary.compose.core.dialog.DialogState
import io.github.taetae98coding.diary.compose.core.dialog.rememberDialogState
import io.github.taetae98coding.diary.compose.core.dialog.rememberDiaryPickerSearchFieldState
import io.github.taetae98coding.diary.compose.core.effect.DiarySearchQueryEffect
import io.github.taetae98coding.diary.compose.core.preview.ScreenPreview
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.core.model.tag.Tag
import io.github.taetae98coding.diary.feature.tag.ui.previewTag
import kotlinx.coroutines.flow.flowOf

@Composable
internal fun TagLinkPickerDialogHost(
    dialogState: DialogState,
    onEvent: (TagLinkPickerEvent) -> Unit,
    tagPagingItems: LazyPagingItems<Tag> = remember { flowOf(PagingData.empty<Tag>()) }.collectAsLazyPagingItems(),
    uiStateProvider: () -> TagLinkInputUiState = { TagLinkInputUiState() },
) {
    if (!dialogState.isVisible) return

    val searchFieldState = rememberDiaryPickerSearchFieldState()

    val hide = {
        onEvent(TagLinkPickerEvent.ChangeQuery(query = ""))
        dialogState.hide()
    }

    DiarySearchQueryEffect(
        queryState = searchFieldState.textFieldState,
        onQueryChange = { query -> onEvent(TagLinkPickerEvent.ChangeQuery(query = query)) },
    )

    TagLinkPickerDialog(
        onDismissRequest = hide,
        onEvent = { event ->
            if (event is TagLinkPickerEvent.ClickAdd) hide()
            onEvent(event)
        },
        searchFieldState = searchFieldState,
        tagPagingItems = tagPagingItems,
        uiStateProvider = uiStateProvider,
    )
}

@ScreenPreview
@Composable
private fun TagLinkPickerDialogHostPreview() {
    val tagList = remember { listOf(previewTag(emoji = "💼", title = "업무", color = 0xFF3A7BD5)) }
    val tagPagingData = remember(tagList) { flowOf(PagingData.from(tagList)) }

    DiaryTheme {
        TagLinkPickerDialogHost(
            dialogState = rememberDialogState().apply { show() },
            onEvent = {},
            tagPagingItems = tagPagingData.collectAsLazyPagingItems(),
            uiStateProvider = { TagLinkInputUiState(linkedTagList = tagList) },
        )
    }
}
