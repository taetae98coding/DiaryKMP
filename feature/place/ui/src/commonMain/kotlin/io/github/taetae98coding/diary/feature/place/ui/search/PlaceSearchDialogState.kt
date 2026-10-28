package io.github.taetae98coding.diary.feature.place.ui.search

import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.focus.FocusRequester
import io.github.taetae98coding.diary.compose.map.DiaryMapCoordinate
import io.github.taetae98coding.diary.compose.map.DiaryMapState
import io.github.taetae98coding.diary.compose.map.provider.DiaryMapProvider
import io.github.taetae98coding.diary.compose.map.rememberDiaryMapState

@Stable
internal class PlaceSearchDialogState(
    val queryState: TextFieldState,
    val focusRequester: FocusRequester,
    val mapState: DiaryMapState,
) {
    val query: String
        get() = queryState.text.toString()

    val hasQuery: Boolean by derivedStateOf { queryState.text.isNotBlank() }
}

@Composable
internal fun rememberPlaceSearchDialogState(
    initialProvider: DiaryMapProvider,
    initialCoordinate: DiaryMapCoordinate? = null,
): PlaceSearchDialogState {
    val queryState = rememberTextFieldState()
    val focusRequester = remember { FocusRequester() }
    val mapState =
        rememberDiaryMapState(
            initialProvider = initialProvider,
            initialCoordinate = initialCoordinate,
        )

    return remember(queryState, focusRequester, mapState) {
        PlaceSearchDialogState(
            queryState = queryState,
            focusRequester = focusRequester,
            mapState = mapState,
        )
    }
}

@Composable
internal fun rememberPlaceSearchDialogState(hostMapState: DiaryMapState): PlaceSearchDialogState =
    rememberPlaceSearchDialogState(
        initialProvider = hostMapState.provider,
        initialCoordinate = hostMapState.coordinate,
    )
