package io.github.taetae98coding.diary.feature.memo.ui.web

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.paging.PagingData
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import io.github.taetae98coding.diary.compose.core.animation.DiaryCrossfade
import io.github.taetae98coding.diary.compose.core.dialog.DiaryPickerAddButton
import io.github.taetae98coding.diary.compose.core.dialog.DiaryPickerDialog
import io.github.taetae98coding.diary.compose.core.dialog.DiaryPickerEmptyBox
import io.github.taetae98coding.diary.compose.core.dialog.DiaryPickerSearchField
import io.github.taetae98coding.diary.compose.core.dialog.DiaryPickerSearchFieldState
import io.github.taetae98coding.diary.compose.core.dialog.rememberDiaryPickerSearchFieldState
import io.github.taetae98coding.diary.compose.core.effect.RequestFocusEffect
import io.github.taetae98coding.diary.compose.core.paging.isLoadedEmpty
import io.github.taetae98coding.diary.compose.core.preview.ScreenPreview
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.core.model.web.Web
import io.github.taetae98coding.diary.feature.memo.ui.Res
import io.github.taetae98coding.diary.feature.memo.ui.memo_web_add_action
import io.github.taetae98coding.diary.feature.memo.ui.memo_web_picker_add_label
import io.github.taetae98coding.diary.feature.memo.ui.memo_web_picker_search_empty_description
import io.github.taetae98coding.diary.feature.memo.ui.memo_web_picker_search_empty_title
import io.github.taetae98coding.diary.feature.memo.ui.memo_web_picker_search_placeholder
import io.github.taetae98coding.diary.feature.memo.ui.memo_web_picker_title
import io.github.taetae98coding.diary.feature.memo.ui.previewWeb
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
    DiaryPickerDialog(
        title = stringResource(Res.string.memo_web_picker_title),
        onDismissRequest = onDismissRequest,
        modifier = modifier,
    ) {
        RequestFocusEffect(focusRequester = searchFieldState.focusRequester)

        val isSearchEmpty by remember(searchFieldState, webPagingItems) {
            derivedStateOf { searchFieldState.textFieldState.text.isNotBlank() && webPagingItems.isLoadedEmpty() }
        }

        Column(modifier = Modifier.fillMaxWidth()) {
            DiaryPickerSearchField(
                placeholder = stringResource(Res.string.memo_web_picker_search_placeholder),
                modifier = Modifier.fillMaxWidth(),
                state = searchFieldState,
            )
            Spacer(modifier = Modifier.height(DiaryTheme.dimens.componentSpacing))
            DiaryCrossfade(
                targetState = isSearchEmpty,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .weight(weight = 1F, fill = false)
                        .height(DiaryTheme.dimens.pickerListHeight),
            ) { isEmpty ->
                if (isEmpty) {
                    DiaryPickerEmptyBox(
                        title = stringResource(Res.string.memo_web_picker_search_empty_title),
                        description = stringResource(Res.string.memo_web_picker_search_empty_description),
                    )
                } else {
                    MemoWebPickerList(
                        onEvent = onEvent,
                        webPagingItems = webPagingItems,
                        uiStateProvider = uiStateProvider,
                        modifier = Modifier.fillMaxSize(),
                    )
                }
            }
            DiaryPickerAddButton(
                onClick = { onEvent(MemoWebPickerEvent.ClickAdd) },
                label = stringResource(Res.string.memo_web_picker_add_label),
                actionLabel = stringResource(Res.string.memo_web_add_action),
                modifier = Modifier.fillMaxWidth(),
            )
        }
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
