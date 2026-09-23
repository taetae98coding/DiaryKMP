package io.github.taetae98coding.diary.feature.place.ui.form

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.taetae98coding.diary.compose.core.input.DiaryColorInput
import io.github.taetae98coding.diary.compose.core.input.DiaryDescriptionInput
import io.github.taetae98coding.diary.compose.core.input.DiaryTitleInput
import io.github.taetae98coding.diary.compose.core.layout.DiaryInputColumn
import io.github.taetae98coding.diary.compose.core.preview.ScreenPreview
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.compose.tag.entity.EntityTagInput
import io.github.taetae98coding.diary.compose.tag.entity.EntityTagInputUiState
import io.github.taetae98coding.diary.feature.place.ui.PlaceAddressInput
import io.github.taetae98coding.diary.feature.place.ui.PlaceCoordinateInput
import io.github.taetae98coding.diary.feature.place.ui.Res
import io.github.taetae98coding.diary.feature.place.ui.place_latitude_input_label
import io.github.taetae98coding.diary.feature.place.ui.place_longitude_input_label
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun PlaceFormInputArea(
    onEvent: (PlaceFormEvent) -> Unit,
    modifier: Modifier = Modifier,
    state: PlaceFormState = rememberPlaceAddFormState(),
    tagUiStateProvider: () -> EntityTagInputUiState = { EntityTagInputUiState() },
) {
    DiaryInputColumn(modifier = modifier) {
        DiaryTitleInput(
            state = state.titleState,
            modifier = Modifier.fillMaxWidth(),
        )
        DiaryDescriptionInput(
            state = state.descriptionState,
            modifier = Modifier.fillMaxWidth(),
        )
        PlaceAddressInput(
            state = state.addressState,
            modifier = Modifier.fillMaxWidth(),
        )
        PlaceCoordinateInput(
            state = state.latitudeState,
            label = stringResource(Res.string.place_latitude_input_label),
            modifier = Modifier.fillMaxWidth(),
        )
        PlaceCoordinateInput(
            state = state.longitudeState,
            label = stringResource(Res.string.place_longitude_input_label),
            modifier = Modifier.fillMaxWidth(),
        )
        DiaryColorInput(
            state = state.colorState,
            modifier = Modifier.fillMaxWidth(),
        )
        EntityTagInput(
            onTagClick = { id -> onEvent(PlaceFormEvent.ClickTag(id = id)) },
            onAddClick = { onEvent(PlaceFormEvent.ClickTagAdd) },
            modifier = Modifier.fillMaxWidth(),
            uiStateProvider = tagUiStateProvider,
        )
    }
}

@ScreenPreview
@Composable
private fun PlaceFormInputAreaPreview() {
    DiaryTheme {
        Surface {
            PlaceFormInputArea(onEvent = {})
        }
    }
}
