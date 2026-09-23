package io.github.taetae98coding.diary.feature.place.ui.form

import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.taetae98coding.diary.compose.core.layout.DiaryAdaptiveSplitLayout
import io.github.taetae98coding.diary.compose.core.preview.ScreenPreview
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.compose.tag.entity.EntityTagInputUiState

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
        compactPrimaryWeight = PlaceFormDefaults.MAP_HEIGHT_WEIGHT,
        compactSecondaryWeight = PlaceFormDefaults.INPUT_HEIGHT_WEIGHT,
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
