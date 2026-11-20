package io.github.taetae98coding.diary.compose.tag.primary

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.paging.PagingData
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import io.github.taetae98coding.diary.compose.core.dialog.DialogState
import io.github.taetae98coding.diary.compose.core.dialog.DiaryPickerDialogHost
import io.github.taetae98coding.diary.compose.core.dialog.rememberDialogState
import io.github.taetae98coding.diary.compose.tag.previewTag
import io.github.taetae98coding.diary.compose.theme.DiaryTheme
import io.github.taetae98coding.diary.compose.theme.preview.ScreenPreview
import io.github.taetae98coding.diary.core.model.tag.Tag
import kotlinx.coroutines.flow.flowOf

@Composable
public fun PrimaryTagPickerDialogHost(
    dialogState: DialogState,
    onEvent: (PrimaryTagPickerEvent) -> Unit,
    tagPagingItems: LazyPagingItems<Tag> = remember { flowOf(PagingData.empty<Tag>()) }.collectAsLazyPagingItems(),
    uiStateProvider: () -> PrimaryTagInputUiState = { PrimaryTagInputUiState() },
) {
    DiaryPickerDialogHost(
        dialogState = dialogState,
        onQueryChange = { query -> onEvent(PrimaryTagPickerEvent.ChangeQuery(query = query)) },
    ) { searchFieldState, hide ->
        PrimaryTagPickerDialog(
            onDismissRequest = hide,
            onEvent = { event ->
                if (event is PrimaryTagPickerEvent.ClickAdd) hide()
                onEvent(event)
            },
            searchFieldState = searchFieldState,
            tagPagingItems = tagPagingItems,
            uiStateProvider = uiStateProvider,
        )
    }
}

@ScreenPreview
@Composable
private fun PrimaryTagPickerDialogHostPreview() {
    val tagList = remember { listOf(previewTag(emoji = "💼", title = "업무", color = 0xFF3A7BD5)) }
    val tagPagingData = remember(tagList) { flowOf(PagingData.from(tagList)) }

    DiaryTheme {
        PrimaryTagPickerDialogHost(
            dialogState = rememberDialogState().apply { show() },
            onEvent = {},
            tagPagingItems = tagPagingData.collectAsLazyPagingItems(),
            uiStateProvider = { PrimaryTagInputUiState(selectedTagList = tagList, primaryTagId = tagList.first().id) },
        )
    }
}
