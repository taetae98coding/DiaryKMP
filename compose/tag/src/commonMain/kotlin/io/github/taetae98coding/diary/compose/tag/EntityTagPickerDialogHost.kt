package io.github.taetae98coding.diary.compose.tag

import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.paging.PagingData
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import io.github.taetae98coding.diary.compose.core.dialog.DialogState
import io.github.taetae98coding.diary.compose.core.dialog.rememberDialogState
import io.github.taetae98coding.diary.compose.core.effect.DiarySearchQueryEffect
import io.github.taetae98coding.diary.compose.core.preview.ScreenPreview
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.core.model.tag.Tag
import kotlinx.coroutines.flow.flowOf

@Composable
public fun EntityTagPickerDialogHost(
    dialogState: DialogState,
    onEvent: (EntityTagPickerEvent) -> Unit,
    tagPagingItems: LazyPagingItems<Tag> = remember { flowOf(PagingData.empty<Tag>()) }.collectAsLazyPagingItems(),
    uiStateProvider: () -> EntityTagInputUiState = { EntityTagInputUiState() },
) {
    if (!dialogState.isVisible) return

    val queryState = rememberTextFieldState()

    val hide = {
        onEvent(EntityTagPickerEvent.ChangeQuery(query = ""))
        dialogState.hide()
    }

    DiarySearchQueryEffect(
        queryState = queryState,
        onQueryChange = { query -> onEvent(EntityTagPickerEvent.ChangeQuery(query = query)) },
    )

    EntityTagPickerDialog(
        onDismissRequest = hide,
        onEvent = { event ->
            if (event is EntityTagPickerEvent.ClickAdd) hide()
            onEvent(event)
        },
        queryState = queryState,
        tagPagingItems = tagPagingItems,
        uiStateProvider = uiStateProvider,
    )
}

@ScreenPreview
@Composable
private fun EntityTagPickerDialogHostPreview() {
    val tagList = remember { listOf(previewTag(emoji = "💼", title = "업무", color = 0xFF3A7BD5)) }
    val tagPagingData = remember(tagList) { flowOf(PagingData.from(tagList)) }

    DiaryTheme {
        EntityTagPickerDialogHost(
            dialogState = rememberDialogState().apply { show() },
            onEvent = {},
            tagPagingItems = tagPagingData.collectAsLazyPagingItems(),
            uiStateProvider = { EntityTagInputUiState(tagList = tagList) },
        )
    }
}
