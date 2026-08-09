package io.github.taetae98coding.diary.feature.place.ui.search

import androidx.compose.runtime.Composable
import io.github.taetae98coding.diary.compose.core.effect.RequestFocusEffect

@Composable
internal fun PlaceSearchFocusEffect(state: PlaceSearchDialogState) {
    RequestFocusEffect(focusRequester = state.focusRequester)
}
