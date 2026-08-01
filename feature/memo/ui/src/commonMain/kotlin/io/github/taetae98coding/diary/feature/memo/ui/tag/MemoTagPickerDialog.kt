package io.github.taetae98coding.diary.feature.memo.ui.tag

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.rememberTextFieldState
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
import io.github.taetae98coding.diary.compose.core.paging.isLoadedEmpty
import io.github.taetae98coding.diary.compose.core.preview.ScreenPreview
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.core.model.tag.Tag
import io.github.taetae98coding.diary.feature.memo.ui.Res
import io.github.taetae98coding.diary.feature.memo.ui.memo_tag_add_action
import io.github.taetae98coding.diary.feature.memo.ui.memo_tag_picker_add_label
import io.github.taetae98coding.diary.feature.memo.ui.memo_tag_picker_search_empty_description
import io.github.taetae98coding.diary.feature.memo.ui.memo_tag_picker_search_empty_title
import io.github.taetae98coding.diary.feature.memo.ui.memo_tag_picker_search_placeholder
import io.github.taetae98coding.diary.feature.memo.ui.memo_tag_picker_title
import io.github.taetae98coding.diary.feature.memo.ui.previewTag
import kotlinx.coroutines.flow.flowOf
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun MemoTagPickerDialog(
    onDismissRequest: () -> Unit,
    onEvent: (MemoTagPickerEvent) -> Unit,
    modifier: Modifier = Modifier,
    queryState: TextFieldState = rememberTextFieldState(),
    tagPagingItems: LazyPagingItems<Tag> = remember { flowOf(PagingData.empty<Tag>()) }.collectAsLazyPagingItems(),
    uiStateProvider: () -> MemoTagInputUiState = { MemoTagInputUiState() },
) {
    DiaryPickerDialog(
        title = stringResource(Res.string.memo_tag_picker_title),
        onDismissRequest = onDismissRequest,
        modifier = modifier,
    ) {
        val isSearchEmpty by remember(queryState, tagPagingItems) {
            derivedStateOf { queryState.text.isNotBlank() && tagPagingItems.isLoadedEmpty() }
        }

        Column(modifier = Modifier.fillMaxWidth()) {
            DiaryPickerSearchField(
                placeholder = stringResource(Res.string.memo_tag_picker_search_placeholder),
                modifier = Modifier.fillMaxWidth(),
                state = queryState,
            )
            Spacer(modifier = Modifier.height(DiaryTheme.dimens.componentSpacing))
            DiaryCrossfade(
                targetState = isSearchEmpty,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        // 목록 자리를 고정해 항목 수나 검색 결과 수가 바뀌어도 대화상자 크기가 바뀌지 않게 한다.
                        .weight(weight = 1F, fill = false)
                        .height(DiaryTheme.dimens.pickerListHeight),
            ) { isEmpty ->
                if (isEmpty) {
                    DiaryPickerEmptyBox(
                        title = stringResource(Res.string.memo_tag_picker_search_empty_title),
                        description = stringResource(Res.string.memo_tag_picker_search_empty_description),
                    )
                } else {
                    MemoTagPickerList(
                        onEvent = onEvent,
                        tagPagingItems = tagPagingItems,
                        uiStateProvider = uiStateProvider,
                        modifier = Modifier.fillMaxSize(),
                    )
                }
            }
            DiaryPickerAddButton(
                onClick = { onEvent(MemoTagPickerEvent.ClickAdd) },
                label = stringResource(Res.string.memo_tag_picker_add_label),
                actionLabel = stringResource(Res.string.memo_tag_add_action),
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@ScreenPreview
@Composable
private fun MemoTagPickerDialogPreview() {
    val tagList = remember { listOf(previewTag(emoji = "💼", title = "업무", color = 0xFF3A7BD5)) }
    val tagPagingData = remember(tagList) { flowOf(PagingData.from(tagList)) }

    DiaryTheme {
        MemoTagPickerDialog(
            onDismissRequest = {},
            onEvent = {},
            tagPagingItems = tagPagingData.collectAsLazyPagingItems(),
            uiStateProvider = { MemoTagInputUiState(selectedTagList = tagList, primaryTagId = tagList.first().id) },
        )
    }
}
