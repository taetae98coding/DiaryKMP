package io.github.taetae98coding.diary.feature.routine.ui.home

import androidx.compose.foundation.ScrollState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.taetae98coding.diary.compose.core.shortcut.isAddShortcut
import io.github.taetae98coding.diary.compose.core.shortcut.keyShortcut

@Composable
internal fun RoutineHomeScreen(
    navigateToAdd: () -> Unit,
    componentVisibleProvider: () -> RoutineHomeScaffoldComponentVisible,
    scrollState: ScrollState,
    viewModel: RoutineHomeViewModel,
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    RoutineHomeScaffold(
        scrollState = scrollState,
        onEvent = { event ->
            when (event) {
                is RoutineHomeScaffoldEvent.ClickAdd -> navigateToAdd()
                is RoutineHomeScaffoldEvent.Refresh -> viewModel.refresh()
            }
        },
        modifier =
            modifier.keyShortcut(isEnableProvider = { componentVisibleProvider().isAddButtonVisible }) { keyEvent ->
                if (keyEvent.isAddShortcut()) {
                    navigateToAdd()
                    true
                } else {
                    false
                }
            },
        uiStateProvider = { uiState },
        componentVisibleProvider = componentVisibleProvider,
    )
}
