package io.github.taetae98coding.diary.feature.memo.ui.place

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.paging.PagingData
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import io.github.taetae98coding.diary.compose.core.preview.ComponentPreview
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.core.model.place.Place
import io.github.taetae98coding.diary.feature.memo.ui.previewPlace
import kotlinx.coroutines.flow.flowOf

internal const val MEMO_PLACE_PICKER_LIST_TEST_TAG: String = "MemoPlacePickerList"

@Composable
internal fun MemoPlacePickerList(
    onEvent: (MemoPlacePickerEvent) -> Unit,
    modifier: Modifier = Modifier,
    selectedPlaceListProvider: () -> List<Place> = { emptyList() },
    placePagingItems: LazyPagingItems<Place> = remember { flowOf(PagingData.empty<Place>()) }.collectAsLazyPagingItems(),
) {
    val selectedPlaceList = selectedPlaceListProvider()
    val selectedPlaceIdSet = remember(selectedPlaceList) { selectedPlaceList.mapTo(mutableSetOf()) { place -> place.id } }

    LazyColumn(modifier = modifier.testTag(MEMO_PLACE_PICKER_LIST_TEST_TAG)) {
        items(
            count = placePagingItems.itemCount,
            key = placePagingItems.itemKey { place -> place.id },
        ) { index ->
            val place = placePagingItems[index]

            MemoPlacePickerRow(
                onEvent = onEvent,
                place = place,
                isSelected = place != null && place.id in selectedPlaceIdSet,
                modifier =
                    Modifier
                        .animateItem()
                        .fillMaxWidth(),
            )
        }
    }
}

@ComponentPreview
@Composable
private fun MemoPlacePickerListPreview() {
    val placeList = remember { listOf(previewPlace(title = "집", color = 0xFF3A7BD5, latitude = 37.5665, longitude = 126.9780)) }
    val placePagingData = remember(placeList) { flowOf(PagingData.from(placeList)) }

    DiaryTheme {
        Surface {
            MemoPlacePickerList(
                onEvent = {},
                selectedPlaceListProvider = { placeList },
                placePagingItems = placePagingData.collectAsLazyPagingItems(),
            )
        }
    }
}
