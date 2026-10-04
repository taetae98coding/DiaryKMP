package io.github.taetae98coding.diary.compose.core.dialog

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.paging.PagingData
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import io.github.taetae98coding.diary.compose.core.animation.DiaryCrossfade
import io.github.taetae98coding.diary.compose.core.effect.RequestFocusEffect
import io.github.taetae98coding.diary.compose.core.paging.isLoadedEmpty
import io.github.taetae98coding.diary.compose.core.preview.ScreenPreview
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import kotlinx.coroutines.flow.flowOf

public const val DIARY_PAGING_PICKER_LIST_TEST_TAG: String = "DiaryPagingPickerList"

@Composable
public fun <T : Any> DiaryPagingPickerDialog(
    text: DiaryPagingPickerText,
    onAddClick: () -> Unit,
    onDismissRequest: () -> Unit,
    itemKey: (T) -> Any,
    modifier: Modifier = Modifier,
    searchFieldState: DiaryPickerSearchFieldState = rememberDiaryPickerSearchFieldState(),
    pagingItems: LazyPagingItems<T> = remember { flowOf(PagingData.empty<T>()) }.collectAsLazyPagingItems(),
    isSearchFocusRequested: Boolean = false,
    itemContent: @Composable (item: T?, modifier: Modifier) -> Unit,
) {
    DiaryPickerDialog(
        title = text.title,
        onDismissRequest = onDismissRequest,
        modifier = modifier,
    ) {
        if (isSearchFocusRequested) {
            RequestFocusEffect(focusRequester = searchFieldState.focusRequester)
        }

        val isSearchEmpty by remember(searchFieldState, pagingItems) {
            derivedStateOf { searchFieldState.textFieldState.text.isNotBlank() && pagingItems.isLoadedEmpty() }
        }

        Column(modifier = Modifier.fillMaxWidth()) {
            DiaryPickerSearchField(
                placeholder = text.searchPlaceholder,
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
                        title = text.searchEmptyTitle,
                        description = text.searchEmptyDescription,
                    )
                } else {
                    DiaryPagingPickerList(
                        pagingItems = pagingItems,
                        itemKey = itemKey,
                        modifier = Modifier.fillMaxSize(),
                        itemContent = itemContent,
                    )
                }
            }
            DiaryPickerAddButton(
                onClick = onAddClick,
                label = text.addLabel,
                actionLabel = text.addActionLabel,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun <T : Any> DiaryPagingPickerList(
    pagingItems: LazyPagingItems<T>,
    itemKey: (T) -> Any,
    modifier: Modifier = Modifier,
    itemContent: @Composable (item: T?, modifier: Modifier) -> Unit,
) {
    LazyColumn(modifier = modifier.testTag(DIARY_PAGING_PICKER_LIST_TEST_TAG)) {
        items(
            count = pagingItems.itemCount,
            key = pagingItems.itemKey(itemKey),
        ) { index ->
            itemContent(
                pagingItems[index],
                Modifier
                    .animateItem()
                    .fillMaxWidth(),
            )
        }
    }
}

@ScreenPreview
@Composable
private fun DiaryPagingPickerDialogPreview() {
    val pagingData = remember { flowOf(PagingData.from(listOf("업무", "개인"))) }

    DiaryTheme {
        DiaryPagingPickerDialog(
            text =
                DiaryPagingPickerText(
                    title = "태그 선택",
                    searchPlaceholder = "태그 검색",
                    searchEmptyTitle = "검색 결과 없음",
                    searchEmptyDescription = "다른 검색어를 입력해 보세요.",
                    addLabel = "새 태그",
                    addActionLabel = "태그 추가",
                ),
            onAddClick = {},
            onDismissRequest = {},
            itemKey = { item -> item },
            pagingItems = pagingData.collectAsLazyPagingItems(),
        ) { item, itemModifier ->
            DiaryPickerRow(
                onSelectedChange = {},
                label = item.orEmpty(),
                modifier = itemModifier,
                isSelected = item == "업무",
                enabled = item != null,
            )
        }
    }
}
