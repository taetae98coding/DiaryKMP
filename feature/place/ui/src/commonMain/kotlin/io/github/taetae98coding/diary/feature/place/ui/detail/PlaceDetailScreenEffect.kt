package io.github.taetae98coding.diary.feature.place.ui.detail

import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import io.github.taetae98coding.diary.compose.core.effect.CollectEffect
import io.github.taetae98coding.diary.compose.core.snackbar.showImmediate
import io.github.taetae98coding.diary.feature.place.ui.Res
import io.github.taetae98coding.diary.feature.place.ui.form.PlaceFormState
import io.github.taetae98coding.diary.feature.place.ui.place_coordinate_invalid_message
import io.github.taetae98coding.diary.feature.place.ui.place_detail_update_succeeded_message
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun PlaceDetailScreenEffect(
    scaffoldState: PlaceFormState,
    navigateUp: () -> Unit,
    effect: Flow<PlaceDetailEffect> = emptyFlow(),
) {
    val coroutineScope = rememberCoroutineScope()
    val updateSucceededMessage = stringResource(Res.string.place_detail_update_succeeded_message)
    val coordinateInvalidMessage = stringResource(Res.string.place_coordinate_invalid_message)

    CollectEffect(effect) { value ->
        when (value) {
            is PlaceDetailEffect.UpdateSucceeded -> {
                coroutineScope.launch { scaffoldState.snackbarHostState.showImmediate(message = updateSucceededMessage) }
            }

            is PlaceDetailEffect.CoordinateInvalid -> {
                coroutineScope.launch { scaffoldState.snackbarHostState.showImmediate(message = coordinateInvalidMessage) }
            }

            is PlaceDetailEffect.DeleteSucceeded -> navigateUp()
        }
    }
}
