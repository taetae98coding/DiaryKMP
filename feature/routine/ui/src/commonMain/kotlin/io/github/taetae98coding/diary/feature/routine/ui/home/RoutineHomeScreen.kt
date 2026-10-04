package io.github.taetae98coding.diary.feature.routine.ui.home

import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.taetae98coding.diary.compose.core.shortcut.isAddShortcut
import io.github.taetae98coding.diary.compose.core.shortcut.keyShortcut
import io.github.taetae98coding.diary.feature.core.sync.SyncRefreshViewModel
import kotlinx.coroutines.flow.Flow

@Composable
internal fun RoutineHomeScreen(
    navigateToAdd: () -> Unit,
    componentVisibleProvider: () -> RoutineHomeScaffoldComponentVisible,
    homeReselectEvent: Flow<Unit>,
    viewModel: SyncRefreshViewModel,
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val scrollState = rememberScrollState()

    ScrollToTopOnReselectEffect(
        reselectEvent = homeReselectEvent,
        scrollState = scrollState,
    )

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
