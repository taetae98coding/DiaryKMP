package io.github.taetae98coding.diary.feature.memo.ui.web

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
import io.github.taetae98coding.diary.compose.web.previewWeb
import io.github.taetae98coding.diary.core.model.web.Web
import io.github.taetae98coding.diary.feature.memo.ui.Res
import io.github.taetae98coding.diary.feature.memo.ui.memo_web_add_action
import io.github.taetae98coding.diary.feature.memo.ui.memo_web_picker_add_label
import io.github.taetae98coding.diary.feature.memo.ui.memo_web_picker_search_empty_description
import io.github.taetae98coding.diary.feature.memo.ui.memo_web_picker_search_empty_title
import io.github.taetae98coding.diary.feature.memo.ui.memo_web_picker_search_placeholder
import io.github.taetae98coding.diary.feature.memo.ui.memo_web_picker_title
import kotlinx.coroutines.flow.flowOf
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun MemoWebPickerDialog(
    onDismissRequest: () -> Unit,
    onEvent: (MemoWebPickerEvent) -> Unit,
    modifier: Modifier = Modifier,
    searchFieldState: DiaryPickerSearchFieldState = rememberDiaryPickerSearchFieldState(),
    webPagingItems: LazyPagingItems<Web> = remember { flowOf(PagingData.empty<Web>()) }.collectAsLazyPagingItems(),
    uiStateProvider: () -> MemoWebInputUiState = { MemoWebInputUiState() },
) {
    DiaryPagingPickerDialog(
        text =
            DiaryPagingPickerText(
                title = stringResource(Res.string.memo_web_picker_title),
                searchPlaceholder = stringResource(Res.string.memo_web_picker_search_placeholder),
                searchEmptyTitle = stringResource(Res.string.memo_web_picker_search_empty_title),
                searchEmptyDescription = stringResource(Res.string.memo_web_picker_search_empty_description),
                addLabel = stringResource(Res.string.memo_web_picker_add_label),
                addActionLabel = stringResource(Res.string.memo_web_add_action),
            ),
        onAddClick = { onEvent(MemoWebPickerEvent.ClickAdd) },
        onDismissRequest = onDismissRequest,
        itemKey = { web -> web.id },
        modifier = modifier,
        searchFieldState = searchFieldState,
        pagingItems = webPagingItems,
        isSearchFocusRequested = true,
    ) { web, itemModifier ->
        MemoWebPickerRow(
            onEvent = onEvent,
            modifier = itemModifier,
            web = web,
            isSelected = web != null && uiStateProvider().selectedWebList.any { selectedWeb -> selectedWeb.id == web.id },
        )
    }
}

@ScreenPreview
@Composable
private fun MemoWebPickerDialogPreview() {
    val webList = remember { listOf(previewWeb(title = "사내 위키", url = "https://wiki.example.com")) }
    val webPagingData = remember(webList) { flowOf(PagingData.from(webList)) }

    DiaryTheme {
        MemoWebPickerDialog(
            onDismissRequest = {},
            onEvent = {},
            webPagingItems = webPagingData.collectAsLazyPagingItems(),
            uiStateProvider = { MemoWebInputUiState(selectedWebList = webList) },
        )
    }
}
