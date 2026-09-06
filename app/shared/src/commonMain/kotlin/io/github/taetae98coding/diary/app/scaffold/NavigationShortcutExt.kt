package io.github.taetae98coding.diary.app.scaffold

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.isMetaPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.type
import io.github.taetae98coding.diary.app.AppState
import io.github.taetae98coding.diary.app.navigation.TopLevelNavigation
import io.github.taetae98coding.diary.compose.core.shortcut.keyShortcut

@Composable
internal fun Modifier.navigationShortcut(appState: AppState): Modifier =
    keyShortcut(isEnableProvider = { appState.isNavigationVisible }) { keyEvent ->
        if (keyEvent.type != KeyEventType.KeyDown || !keyEvent.isMetaPressed) {
            return@keyShortcut false
        }

        val topLevelNavigation =
            when (keyEvent.key) {
                Key.One -> TopLevelNavigation.Memo
                Key.Two -> TopLevelNavigation.Tag
                Key.Three -> TopLevelNavigation.Calendar
                Key.Four -> TopLevelNavigation.Routine
                Key.Five -> TopLevelNavigation.More
                else -> null
            }

        topLevelNavigation?.let(appState::navigateTo)
        topLevelNavigation != null
    }
