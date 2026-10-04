package io.github.taetae98coding.diary.feature.memo.ui.tag

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.paging.PagingData
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import io.github.taetae98coding.diary.compose.core.dialog.DialogState
import io.github.taetae98coding.diary.compose.core.dialog.DiaryPickerDialogHost
import io.github.taetae98coding.diary.compose.core.dialog.rememberDialogState
import io.github.taetae98coding.diary.compose.core.preview.ScreenPreview
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.compose.tag.previewTag
import io.github.taetae98coding.diary.core.model.tag.Tag
import kotlinx.coroutines.flow.flowOf

@Composable
internal fun MemoTagPickerDialogHost(
    dialogState: DialogState,
    onEvent: (MemoTagPickerEvent) -> Unit,
    tagPagingItems: LazyPagingItems<Tag> = remember { flowOf(PagingData.empty<Tag>()) }.collectAsLazyPagingItems(),
    uiStateProvider: () -> MemoTagInputUiState = { MemoTagInputUiState() },
) {
    DiaryPickerDialogHost(
        dialogState = dialogState,
        onQueryChange = { query -> onEvent(MemoTagPickerEvent.ChangeQuery(query = query)) },
    ) { searchFieldState, hide ->
        MemoTagPickerDialog(
            onDismissRequest = hide,
            onEvent = { event ->
                if (event is MemoTagPickerEvent.ClickAdd) hide()
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
private fun MemoTagPickerDialogHostPreview() {
    val tagList = remember { listOf(previewTag(emoji = "💼", title = "업무", color = 0xFF3A7BD5)) }
    val tagPagingData = remember(tagList) { flowOf(PagingData.from(tagList)) }

    DiaryTheme {
        MemoTagPickerDialogHost(
            dialogState = rememberDialogState().apply { show() },
            onEvent = {},
            tagPagingItems = tagPagingData.collectAsLazyPagingItems(),
            uiStateProvider = { MemoTagInputUiState(selectedTagList = tagList, primaryTagId = tagList.first().id) },
        )
    }
}
