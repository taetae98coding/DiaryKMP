package io.github.taetae98coding.diary.feature.memo.ui.web

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
import io.github.taetae98coding.diary.compose.web.previewWeb
import io.github.taetae98coding.diary.core.model.web.Web
import kotlinx.coroutines.flow.flowOf

@Composable
internal fun MemoWebPickerDialogHost(
    dialogState: DialogState,
    onEvent: (MemoWebPickerEvent) -> Unit,
    webPagingItems: LazyPagingItems<Web> = remember { flowOf(PagingData.empty<Web>()) }.collectAsLazyPagingItems(),
    uiStateProvider: () -> MemoWebInputUiState = { MemoWebInputUiState() },
) {
    DiaryPickerDialogHost(
        dialogState = dialogState,
        onQueryChange = { query -> onEvent(MemoWebPickerEvent.ChangeQuery(query = query)) },
    ) { searchFieldState, hide ->
        MemoWebPickerDialog(
            onDismissRequest = hide,
            onEvent = { event ->
                if (event is MemoWebPickerEvent.ClickAdd) hide()
                onEvent(event)
            },
            searchFieldState = searchFieldState,
            webPagingItems = webPagingItems,
            uiStateProvider = uiStateProvider,
        )
    }
}

@ScreenPreview
@Composable
private fun MemoWebPickerDialogHostPreview() {
    val webList = remember { listOf(previewWeb(title = "사내 위키", url = "https://wiki.example.com")) }
    val webPagingData = remember(webList) { flowOf(PagingData.from(webList)) }

    DiaryTheme {
        MemoWebPickerDialogHost(
            dialogState = rememberDialogState(initialVisible = true),
            onEvent = {},
            webPagingItems = webPagingData.collectAsLazyPagingItems(),
            uiStateProvider = { MemoWebInputUiState(selectedWebList = webList) },
        )
    }
}
