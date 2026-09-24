package io.github.taetae98coding.diary.feature.memo.ui.tag

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
import io.github.taetae98coding.diary.feature.memo.ui.previewTag
import kotlinx.coroutines.flow.flowOf

@Composable
internal fun MemoTagPickerDialogHost(
    dialogState: DialogState,
    onEvent: (MemoTagPickerEvent) -> Unit,
    tagPagingItems: LazyPagingItems<Tag> = remember { flowOf(PagingData.empty<Tag>()) }.collectAsLazyPagingItems(),
    uiStateProvider: () -> MemoTagInputUiState = { MemoTagInputUiState() },
) {
    if (!dialogState.isVisible) return

    val searchFieldState = rememberDiaryPickerSearchFieldState()

    val hide = {
        onEvent(MemoTagPickerEvent.ChangeQuery(query = ""))
        dialogState.hide()
    }

    DiarySearchQueryEffect(
        queryState = searchFieldState.textFieldState,
        onQueryChange = { query -> onEvent(MemoTagPickerEvent.ChangeQuery(query = query)) },
    )

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
