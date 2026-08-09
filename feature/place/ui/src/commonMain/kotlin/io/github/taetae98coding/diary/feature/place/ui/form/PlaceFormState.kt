package io.github.taetae98coding.diary.feature.place.ui.form

import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.clearText
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import io.github.taetae98coding.diary.compose.core.dialog.DialogState
import io.github.taetae98coding.diary.compose.core.dialog.rememberDialogState
import io.github.taetae98coding.diary.compose.core.input.DiaryColorInputState
import io.github.taetae98coding.diary.compose.core.input.DiaryDescriptionInputState
import io.github.taetae98coding.diary.compose.core.input.DiaryTitleInputState
import io.github.taetae98coding.diary.compose.core.input.rememberDiaryColorInputState
import io.github.taetae98coding.diary.compose.core.input.rememberDiaryDescriptionInputState
import io.github.taetae98coding.diary.compose.core.input.rememberDiaryTitleInputState
import io.github.taetae98coding.diary.compose.map.DiaryMapCoordinate
import io.github.taetae98coding.diary.compose.map.DiaryMapState
import io.github.taetae98coding.diary.compose.map.rememberDiaryMapState
import io.github.taetae98coding.diary.compose.place.toDiaryMapCoordinate
import io.github.taetae98coding.diary.compose.place.toDiaryMapProvider
import io.github.taetae98coding.diary.core.model.location.Coordinate
import io.github.taetae98coding.diary.core.model.map.MapProvider
import io.github.taetae98coding.diary.core.model.place.PlaceDetail
import io.github.taetae98coding.diary.core.model.place.SearchedPlace
import io.github.taetae98coding.diary.feature.place.ui.decimalOrNaN
import io.github.taetae98coding.diary.feature.place.ui.toCoordinateText
import io.github.taetae98coding.diary.library.compose.ui.color.randomColor
import io.github.taetae98coding.diary.library.compose.ui.color.toColor
import io.github.taetae98coding.diary.library.compose.ui.color.toColorLong

@Stable
internal class PlaceFormState(
    val titleState: DiaryTitleInputState,
    val descriptionState: DiaryDescriptionInputState,
    val addressState: TextFieldState,
    val latitudeState: TextFieldState,
    val longitudeState: TextFieldState,
    val colorState: DiaryColorInputState,
    val hostState: SnackbarHostState,
    val mapState: DiaryMapState,
    val searchDialogState: DialogState,
    val tagPickerDialogState: DialogState,
) {
    val detail: PlaceDetail
        get() =
            PlaceDetail(
                title = titleState.text.toString(),
                description = descriptionState.text.toString(),
                color = colorState.color.toColorLong(),
                coordinate = coordinate(),
                address = addressState.text.toString(),
            )

    val spot: DiaryMapCoordinate?
        get() = coordinate().takeIf { coordinate -> coordinate.isRepresentable }?.toDiaryMapCoordinate()

    fun clearCoordinate() {
        latitudeState.clearText()
        longitudeState.clearText()
    }

    fun setCoordinate(coordinate: DiaryMapCoordinate) {
        latitudeState.setTextAndPlaceCursorAtEnd(coordinate.latitude.toCoordinateText())
        longitudeState.setTextAndPlaceCursorAtEnd(coordinate.longitude.toCoordinateText())
    }

    fun applySearchedPlace(place: SearchedPlace) {
        setCoordinate(place.coordinate.toDiaryMapCoordinate())
        addressState.setTextAndPlaceCursorAtEnd(place.address)
        if (titleState.text.isBlank()) {
            titleState.setText(place.name)
        }
        searchDialogState.hide()
    }

    private fun coordinate(): Coordinate =
        Coordinate(
            latitude = latitudeState.decimalOrNaN(),
            longitude = longitudeState.decimalOrNaN(),
        )
}

@Composable
internal fun rememberPlaceAddFormState(
    initialColor: Color = randomColor(),
    defaultProvider: MapProvider? = null,
    initialCoordinate: DiaryMapCoordinate? = null,
): PlaceFormState =
    rememberPlaceFormState(
        initialTitle = "",
        initialDescription = "",
        initialAddress = "",
        initialColor = initialColor,
        defaultProvider = defaultProvider,
        initialCoordinate = initialCoordinate,
    )

@Composable
internal fun rememberPlaceDetailFormState(
    initialDetail: PlaceDetail,
    defaultProvider: MapProvider? = null,
): PlaceFormState =
    rememberPlaceFormState(
        initialTitle = initialDetail.title,
        initialDescription = initialDetail.description,
        initialAddress = initialDetail.address,
        initialColor = initialDetail.color.toColor(),
        defaultProvider = defaultProvider,
        initialCoordinate =
            initialDetail.coordinate
                .takeIf { coordinate -> coordinate.isRepresentable }
                ?.toDiaryMapCoordinate(),
    )

@Composable
private fun rememberPlaceFormState(
    initialTitle: String,
    initialDescription: String,
    initialAddress: String,
    initialColor: Color,
    defaultProvider: MapProvider?,
    initialCoordinate: DiaryMapCoordinate?,
): PlaceFormState {
    val titleState = rememberDiaryTitleInputState(initialText = initialTitle)
    val descriptionState = rememberDiaryDescriptionInputState(initialText = initialDescription)
    val addressState = rememberTextFieldState(initialText = initialAddress)
    val latitudeState = rememberTextFieldState(initialText = initialCoordinate?.latitude?.toCoordinateText().orEmpty())
    val longitudeState = rememberTextFieldState(initialText = initialCoordinate?.longitude?.toCoordinateText().orEmpty())
    val colorState = rememberDiaryColorInputState(initialColor = initialColor)
    val hostState = remember { SnackbarHostState() }
    val searchDialogState = rememberDialogState()
    val tagPickerDialogState = rememberDialogState()
    val mapState =
        if (defaultProvider == null) {
            rememberDiaryMapState()
        } else {
            key(defaultProvider) {
                rememberDiaryMapState(
                    initialProvider = defaultProvider.toDiaryMapProvider(),
                    initialCoordinate = initialCoordinate,
                )
            }
        }

    return remember(
        titleState,
        descriptionState,
        addressState,
        latitudeState,
        longitudeState,
        colorState,
        hostState,
        mapState,
        searchDialogState,
        tagPickerDialogState,
    ) {
        PlaceFormState(
            titleState = titleState,
            descriptionState = descriptionState,
            addressState = addressState,
            latitudeState = latitudeState,
            longitudeState = longitudeState,
            colorState = colorState,
            hostState = hostState,
            mapState = mapState,
            searchDialogState = searchDialogState,
            tagPickerDialogState = tagPickerDialogState,
        )
    }
}
