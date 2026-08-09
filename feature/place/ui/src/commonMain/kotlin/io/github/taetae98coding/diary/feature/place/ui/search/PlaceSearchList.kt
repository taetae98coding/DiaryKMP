package io.github.taetae98coding.diary.feature.place.ui.search

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import io.github.taetae98coding.diary.compose.core.preview.ScreenPreview
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.core.model.place.SearchedPlace
import io.github.taetae98coding.diary.feature.place.ui.previewSearchedPlace

@Composable
internal fun PlaceSearchList(
    onSelect: (SearchedPlace) -> Unit,
    modifier: Modifier = Modifier,
    placeList: List<SearchedPlace> = emptyList(),
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = DiaryTheme.dimens.screenPaddingValues,
        verticalArrangement = Arrangement.spacedBy(DiaryTheme.dimens.itemSpacing),
    ) {
        items(
            items = placeList,
            key = { place -> place.id },
        ) { place ->
            PlaceSearchRow(
                place = place,
                onClick = { onSelect(place) },
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@ScreenPreview
@Composable
private fun PlaceSearchListPreview() {
    val placeList =
        remember {
            listOf(
                previewSearchedPlace(name = "서울시청", latitude = 37.5665, longitude = 126.9780),
                previewSearchedPlace(name = "광화문", latitude = 37.5759, longitude = 126.9769),
            )
        }

    DiaryTheme {
        Surface {
            PlaceSearchList(
                onSelect = {},
                placeList = placeList,
            )
        }
    }
}
