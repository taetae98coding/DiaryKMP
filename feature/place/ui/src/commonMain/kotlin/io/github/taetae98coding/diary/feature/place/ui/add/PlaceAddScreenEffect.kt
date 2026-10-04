package io.github.taetae98coding.diary.feature.place.ui.add

import androidx.compose.foundation.text.input.clearText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.navigation3.runtime.result.LocalResultEventBus
import androidx.navigation3.runtime.result.ResultEventBus
import io.github.taetae98coding.diary.compose.core.effect.CollectEffect
import io.github.taetae98coding.diary.compose.core.input.showDiaryFormAdded
import io.github.taetae98coding.diary.compose.core.input.showDiaryFormTitleBlank
import io.github.taetae98coding.diary.compose.core.snackbar.showImmediate
import io.github.taetae98coding.diary.feature.place.api.PlaceAddedResult
import io.github.taetae98coding.diary.feature.place.api.placeAddedResultKey
import io.github.taetae98coding.diary.feature.place.ui.Res
import io.github.taetae98coding.diary.feature.place.ui.form.PlaceFormState
import io.github.taetae98coding.diary.feature.place.ui.place_add_succeeded_message
import io.github.taetae98coding.diary.feature.place.ui.place_add_title_blank_message
import io.github.taetae98coding.diary.feature.place.ui.place_coordinate_invalid_message
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import kotlin.uuid.Uuid

@Composable
internal fun PlaceAddScreenEffect(
    addedResultRequestKey: Uuid?,
    effect: Flow<PlaceAddEffect>,
    scaffoldState: PlaceFormState,
    resultEventBus: ResultEventBus = LocalResultEventBus.current,
) {
    val coroutineScope = rememberCoroutineScope()
    val addSucceededMessage = stringResource(Res.string.place_add_succeeded_message)
    val titleBlankMessage = stringResource(Res.string.place_add_title_blank_message)
    val coordinateInvalidMessage = stringResource(Res.string.place_coordinate_invalid_message)

    CollectEffect(effect) { value ->
        when (value) {
            is PlaceAddEffect.AddSucceeded -> {
                addedResultRequestKey?.let { requestKey ->
                    resultEventBus.sendResult(resultKey = placeAddedResultKey(requestKey = requestKey), result = PlaceAddedResult(id = value.id))
                }
                scaffoldState.titleState.clearText()
                scaffoldState.descriptionState.clearText()
                scaffoldState.addressState.clearText()
                scaffoldState.clearCoordinate()
                scaffoldState.mapState.selectSpot(null)
                coroutineScope.showDiaryFormAdded(
                    titleState = scaffoldState.titleState,
                    colorState = scaffoldState.colorState,
                    snackbarHostState = scaffoldState.snackbarHostState,
                    message = addSucceededMessage,
                )
            }

            is PlaceAddEffect.TitleBlank -> {
                coroutineScope.showDiaryFormTitleBlank(
                    titleState = scaffoldState.titleState,
                    snackbarHostState = scaffoldState.snackbarHostState,
                    message = titleBlankMessage,
                )
            }

            is PlaceAddEffect.CoordinateInvalid -> {
                coroutineScope.launch { scaffoldState.snackbarHostState.showImmediate(message = coordinateInvalidMessage) }
            }
        }
    }
}
