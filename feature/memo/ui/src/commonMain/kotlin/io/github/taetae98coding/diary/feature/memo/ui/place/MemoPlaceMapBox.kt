package io.github.taetae98coding.diary.feature.memo.ui.place

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import io.github.taetae98coding.diary.compose.core.preview.ComponentPreview
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.compose.map.DiaryMap
import io.github.taetae98coding.diary.compose.map.DiaryMapState
import io.github.taetae98coding.diary.compose.place.PlacePinMarkerEffect
import io.github.taetae98coding.diary.core.model.place.Place
import io.github.taetae98coding.diary.feature.memo.ui.Res
import io.github.taetae98coding.diary.feature.memo.ui.memo_place_map_content_description
import org.jetbrains.compose.resources.stringResource
import kotlin.uuid.Uuid

@Composable
internal fun MemoPlaceMapBox(
    onPinClick: (Uuid) -> Unit,
    modifier: Modifier = Modifier,
    mapState: DiaryMapState? = null,
    placeListProvider: () -> List<Place> = { emptyList() },
) {
    val mapContentDescription = stringResource(Res.string.memo_place_map_content_description)

    Box(
        modifier =
            modifier
                .fillMaxWidth()
                .semantics { contentDescription = mapContentDescription },
    ) {
        if (mapState != null) {
            PlacePinMarkerEffect(
                mapState = mapState,
                placeListProvider = placeListProvider,
            )
            DiaryMap(
                state = mapState,
                modifier = Modifier.fillMaxSize(),
                onPinClick = onPinClick,
            )
        }
    }
}

@ComponentPreview
@Composable
private fun MemoPlaceMapBoxPreview() {
    DiaryTheme {
        Surface {
            MemoPlaceMapBox(
                onPinClick = {},
                modifier = Modifier.height(240.dp),
            )
        }
    }
}
