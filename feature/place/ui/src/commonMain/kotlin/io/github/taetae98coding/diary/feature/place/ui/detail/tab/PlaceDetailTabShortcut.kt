package io.github.taetae98coding.diary.feature.place.ui.detail.tab

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.taetae98coding.diary.compose.core.shortcut.isAddShortcut
import io.github.taetae98coding.diary.compose.core.shortcut.isSubmitShortcut
import io.github.taetae98coding.diary.compose.core.shortcut.keyShortcut

@Composable
internal fun Modifier.placeDetailTabShortcut(
    onUpdate: () -> Unit,
    onMemoAdd: () -> Unit,
    state: PlaceDetailTabState = rememberPlaceDetailTabState(),
    isUpdateEnabledProvider: () -> Boolean = { false },
): Modifier =
    keyShortcut { keyEvent ->
        when (state.tab) {
            PlaceDetailTab.DETAIL -> {
                if (isUpdateEnabledProvider() && keyEvent.isSubmitShortcut()) {
                    onUpdate()
                    true
                } else {
                    false
                }
            }

            PlaceDetailTab.MEMO -> {
                if (keyEvent.isAddShortcut()) {
                    onMemoAdd()
                    true
                } else {
                    false
                }
            }
        }
    }
