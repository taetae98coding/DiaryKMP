package io.github.taetae98coding.diary.feature.place.ui.form

import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.taetae98coding.diary.compose.core.layout.DiaryAdaptiveSplitLayout
import io.github.taetae98coding.diary.compose.core.preview.ScreenPreview
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.compose.tag.entity.EntityTagInputUiState

private const val MAP_HEIGHT_WEIGHT = 0.4F
private const val INPUT_HEIGHT_WEIGHT = 0.6F

@Composable
internal fun PlaceForm(
    onEvent: (PlaceFormEvent) -> Unit,
    modifier: Modifier = Modifier,
    state: PlaceFormState = rememberPlaceAddFormState(),
    isMapDisplayed: Boolean = false,
    tagUiStateProvider: () -> EntityTagInputUiState = { EntityTagInputUiState() },
) {
    if (!isMapDisplayed) {
        PlaceFormInputArea(
            onEvent = onEvent,
            modifier = modifier,
            state = state,
            tagUiStateProvider = tagUiStateProvider,
        )
        return
    }

    DiaryAdaptiveSplitLayout(
        primary = { PlaceFormMapArea(state = state) },
        secondary = {
            PlaceFormInputArea(
                onEvent = onEvent,
                state = state,
                tagUiStateProvider = tagUiStateProvider,
            )
        },
        modifier = modifier,
        compactPrimaryWeight = MAP_HEIGHT_WEIGHT,
        compactSecondaryWeight = INPUT_HEIGHT_WEIGHT,
    )
}

@ScreenPreview
@Composable
private fun PlaceFormPreview() {
    DiaryTheme {
        Surface {
            PlaceForm(onEvent = {})
        }
    }
}
