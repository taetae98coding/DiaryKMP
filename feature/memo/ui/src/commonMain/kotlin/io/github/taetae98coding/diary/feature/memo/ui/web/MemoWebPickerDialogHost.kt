package io.github.taetae98coding.diary.feature.memo.ui.web

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
import io.github.taetae98coding.diary.core.model.web.Web
import io.github.taetae98coding.diary.feature.memo.ui.previewWeb
import kotlinx.coroutines.flow.flowOf

@Composable
internal fun MemoWebPickerDialogHost(
    dialogState: DialogState,
    onEvent: (MemoWebPickerEvent) -> Unit,
    webPagingItems: LazyPagingItems<Web> = remember { flowOf(PagingData.empty<Web>()) }.collectAsLazyPagingItems(),
    uiStateProvider: () -> MemoWebInputUiState = { MemoWebInputUiState() },
) {
    if (!dialogState.isVisible) return

    val queryState = rememberTextFieldState()

    val hide = {
        onEvent(MemoWebPickerEvent.ChangeQuery(query = ""))
        dialogState.hide()
    }

    DiarySearchQueryEffect(
        queryState = queryState,
        onQueryChange = { query -> onEvent(MemoWebPickerEvent.ChangeQuery(query = query)) },
    )

    MemoWebPickerDialog(
        onDismissRequest = hide,
        onEvent = { event ->
            // 추가로 이동하기 전에 대화상자를 닫아 돌아왔을 때 다시 열려 있지 않게 한다.
            if (event is MemoWebPickerEvent.ClickAdd) hide()
            onEvent(event)
        },
        queryState = queryState,
        webPagingItems = webPagingItems,
        uiStateProvider = uiStateProvider,
    )
}

@ScreenPreview
@Composable
private fun MemoWebPickerDialogHostPreview() {
    val webList = remember { listOf(previewWeb(title = "사내 위키", url = "https://wiki.example.com")) }
    val webPagingData = remember(webList) { flowOf(PagingData.from(webList)) }

    DiaryTheme {
        MemoWebPickerDialogHost(
            dialogState = rememberDialogState().apply { show() },
            onEvent = {},
            webPagingItems = webPagingData.collectAsLazyPagingItems(),
            uiStateProvider = { MemoWebInputUiState(selectedWebList = webList) },
        )
    }
}
