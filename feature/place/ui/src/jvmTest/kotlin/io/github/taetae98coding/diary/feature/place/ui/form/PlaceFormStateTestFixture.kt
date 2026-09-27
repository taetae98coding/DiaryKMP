package io.github.taetae98coding.diary.feature.place.ui.form

import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.material3.SnackbarHostState
import androidx.compose.ui.focus.FocusRequester
import com.navercorp.fixturemonkey.FixtureMonkey
import io.github.taetae98coding.diary.compose.core.dialog.DialogState
import io.github.taetae98coding.diary.compose.core.input.DiaryColorInputState
import io.github.taetae98coding.diary.compose.core.input.DiaryDescriptionInputState
import io.github.taetae98coding.diary.compose.core.input.DiaryTitleInputState
import io.github.taetae98coding.diary.compose.map.DiaryMapCoordinate
import io.github.taetae98coding.diary.compose.map.DiaryMapState
import io.github.taetae98coding.diary.compose.map.provider.DiaryMapProvider
import io.github.taetae98coding.diary.compose.place.toDiaryMapCoordinate
import io.github.taetae98coding.diary.core.testing.place.coordinateInFormPrecision
import io.github.taetae98coding.diary.library.compose.ui.color.randomColor
import io.mockk.every
import io.mockk.mockk

internal fun placeAddFormState(initialMapCoordinate: DiaryMapCoordinate?): PlaceFormState =
    PlaceFormState(
        titleState = DiaryTitleInputState(textFieldState = TextFieldState(), focusRequester = FocusRequester()),
        descriptionState = mockk<DiaryDescriptionInputState> { every { text } returns "" },
        addressState = TextFieldState(),
        latitudeState = TextFieldState(),
        longitudeState = TextFieldState(),
        colorState = DiaryColorInputState(initialColor = randomColor()),
        hostState = SnackbarHostState(),
        mapState = DiaryMapState(initialProvider = DiaryMapProvider.NAVER, initialCoordinate = initialMapCoordinate),
        searchDialogState = DialogState(),
        tagPickerDialogState = DialogState(),
    )

internal fun FixtureMonkey.mapCoordinateInFormPrecision(): DiaryMapCoordinate = coordinateInFormPrecision().toDiaryMapCoordinate()
