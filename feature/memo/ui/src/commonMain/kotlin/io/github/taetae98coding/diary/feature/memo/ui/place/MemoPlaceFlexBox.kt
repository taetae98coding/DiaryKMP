package io.github.taetae98coding.diary.feature.memo.ui.place

import androidx.compose.animation.animateBounds
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.LookaheadScope
import io.github.taetae98coding.diary.compose.core.chip.DiaryAddChip
import io.github.taetae98coding.diary.compose.core.layout.DiaryChipFlexBox
import io.github.taetae98coding.diary.compose.core.layout.DiaryScrollableChipFlexBox
import io.github.taetae98coding.diary.compose.core.preview.ComponentPreview
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.core.model.location.Coordinate
import io.github.taetae98coding.diary.core.model.place.Place
import io.github.taetae98coding.diary.feature.memo.ui.Res
import io.github.taetae98coding.diary.feature.memo.ui.memo_place_select_action
import io.github.taetae98coding.diary.feature.memo.ui.memo_place_select_label
import io.github.taetae98coding.diary.feature.memo.ui.previewPlace
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun MemoPlaceFlexBox(
    onShowOnMap: (Coordinate) -> Unit,
    onAddClick: () -> Unit,
    modifier: Modifier = Modifier,
    placeListProvider: () -> List<Place> = { emptyList() },
) {
    DiaryChipFlexBox(modifier = modifier) {
        PlaceChips(
            onShowOnMap = onShowOnMap,
            onAddClick = onAddClick,
            placeListProvider = placeListProvider,
        )
    }
}

@Composable
internal fun MemoPlaceScrollableFlexBox(
    onShowOnMap: (Coordinate) -> Unit,
    onAddClick: () -> Unit,
    modifier: Modifier = Modifier,
    placeListProvider: () -> List<Place> = { emptyList() },
) {
    DiaryScrollableChipFlexBox(modifier = modifier) {
        PlaceChips(
            onShowOnMap = onShowOnMap,
            onAddClick = onAddClick,
            placeListProvider = placeListProvider,
        )
    }
}

@Composable
private fun LookaheadScope.PlaceChips(
    onShowOnMap: (Coordinate) -> Unit,
    onAddClick: () -> Unit,
    placeListProvider: () -> List<Place>,
) {
    placeListProvider().forEach { place ->
        key(place.id) {
            MemoPlaceChip(
                place = place,
                onClick = { onShowOnMap(place.detail.coordinate) },
                modifier = Modifier.animateBounds(lookaheadScope = this@PlaceChips),
            )
        }
    }
    DiaryAddChip(
        onClick = onAddClick,
        label = stringResource(Res.string.memo_place_select_label),
        actionLabel = stringResource(Res.string.memo_place_select_action),
        modifier = Modifier.animateBounds(lookaheadScope = this@PlaceChips),
    )
}

@ComponentPreview
@Composable
private fun MemoPlaceFlexBoxPreview() {
    val placeList = remember { listOf(previewPlace(title = "집", color = 0xFF3A7BD5, latitude = 37.5665, longitude = 126.9780)) }

    DiaryTheme {
        Surface {
            MemoPlaceFlexBox(
                onShowOnMap = {},
                onAddClick = {},
                placeListProvider = { placeList },
            )
        }
    }
}

@ComponentPreview
@Composable
private fun MemoPlaceScrollableFlexBoxPreview() {
    val placeList = remember { listOf(previewPlace(title = "집", color = 0xFF3A7BD5, latitude = 37.5665, longitude = 126.9780)) }

    DiaryTheme {
        Surface {
            MemoPlaceScrollableFlexBox(
                onShowOnMap = {},
                onAddClick = {},
                placeListProvider = { placeList },
            )
        }
    }
}
