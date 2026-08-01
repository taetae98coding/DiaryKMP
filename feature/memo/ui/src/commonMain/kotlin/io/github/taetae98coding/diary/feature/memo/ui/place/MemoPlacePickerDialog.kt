package io.github.taetae98coding.diary.feature.memo.ui.place

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
import io.github.taetae98coding.diary.core.model.location.Coordinate
import io.github.taetae98coding.diary.core.model.place.Place
import io.github.taetae98coding.diary.feature.memo.ui.Res
import io.github.taetae98coding.diary.feature.memo.ui.memo_place_add_action
import io.github.taetae98coding.diary.feature.memo.ui.memo_place_picker_add_label
import io.github.taetae98coding.diary.feature.memo.ui.memo_place_picker_search_empty_description
import io.github.taetae98coding.diary.feature.memo.ui.memo_place_picker_search_empty_title
import io.github.taetae98coding.diary.feature.memo.ui.memo_place_picker_search_placeholder
import io.github.taetae98coding.diary.feature.memo.ui.memo_place_picker_title
import io.github.taetae98coding.diary.feature.memo.ui.previewPlace
import kotlinx.coroutines.flow.flowOf
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun MemoPlacePickerDialog(
    onDismissRequest: () -> Unit,
    onEvent: (MemoPlacePickerEvent) -> Unit,
    modifier: Modifier = Modifier,
    queryState: TextFieldState = rememberTextFieldState(),
    uiStateProvider: () -> MemoPlaceInputUiState = { MemoPlaceInputUiState() },
    placePagingItems: LazyPagingItems<Place> = remember { flowOf(PagingData.empty<Place>()) }.collectAsLazyPagingItems(),
    coordinateProvider: () -> Coordinate? = { null },
) {
    DiaryPickerDialog(
        title = stringResource(Res.string.memo_place_picker_title),
        onDismissRequest = onDismissRequest,
        modifier = modifier,
    ) {
        // 검색어를 파생 상태로 읽어, 글자를 입력해도 결과 없음 여부가 바뀔 때만 목록 자리를 다시 그린다.
        val isSearchEmpty by remember(queryState, placePagingItems) {
            derivedStateOf { queryState.text.isNotBlank() && placePagingItems.isLoadedEmpty() }
        }

        Column(modifier = Modifier.fillMaxWidth()) {
            DiaryPickerSearchField(
                placeholder = stringResource(Res.string.memo_place_picker_search_placeholder),
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
                        title = stringResource(Res.string.memo_place_picker_search_empty_title),
                        description = stringResource(Res.string.memo_place_picker_search_empty_description),
                    )
                } else {
                    MemoPlacePickerList(
                        onEvent = onEvent,
                        selectedPlaceListProvider = { uiStateProvider().selectedPlaceList },
                        placePagingItems = placePagingItems,
                        modifier = Modifier.fillMaxSize(),
                    )
                }
            }
            DiaryPickerAddButton(
                onClick = { onEvent(MemoPlacePickerEvent.ClickAdd(coordinate = coordinateProvider())) },
                label = stringResource(Res.string.memo_place_picker_add_label),
                actionLabel = stringResource(Res.string.memo_place_add_action),
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@ScreenPreview
@Composable
private fun MemoPlacePickerDialogPreview() {
    val placeList = remember { listOf(previewPlace(title = "집", color = 0xFF3A7BD5, latitude = 37.5665, longitude = 126.9780)) }
    val placePagingData = remember(placeList) { flowOf(PagingData.from(placeList)) }

    DiaryTheme {
        MemoPlacePickerDialog(
            onDismissRequest = {},
            onEvent = {},
            uiStateProvider = { MemoPlaceInputUiState(isSelectedPlaceLoaded = true, selectedPlaceList = placeList) },
            placePagingItems = placePagingData.collectAsLazyPagingItems(),
        )
    }
}
