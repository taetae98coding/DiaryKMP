package io.github.taetae98coding.diary.feature.place.ui.form

import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import io.github.taetae98coding.diary.compose.core.preview.ScreenPreview
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.compose.map.DiaryMap
import io.github.taetae98coding.diary.feature.place.ui.Res
import io.github.taetae98coding.diary.feature.place.ui.place_map_content_description
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun PlaceFormMapArea(
    modifier: Modifier = Modifier,
    state: PlaceFormState = rememberPlaceAddFormState(),
) {
    val contentDescription = stringResource(Res.string.place_map_content_description)

    DiaryMap(
        state = state.mapState,
        modifier = modifier.semantics { this.contentDescription = contentDescription },
        onSpotClick = { coordinate ->
            state.setCoordinate(coordinate)
            state.mapState.selectSpot(state.spot)
        },
    )
}

@ScreenPreview
@Composable
private fun PlaceFormMapAreaPreview() {
    DiaryTheme {
        Surface {
            PlaceFormMapArea()
        }
    }
}
