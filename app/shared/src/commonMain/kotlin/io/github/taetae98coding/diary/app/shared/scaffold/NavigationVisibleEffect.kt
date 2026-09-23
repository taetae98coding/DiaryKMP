package io.github.taetae98coding.diary.app.shared.scaffold

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.flowWithLifecycle
import io.github.taetae98coding.diary.app.shared.AppState
import io.github.taetae98coding.diary.app.shared.rememberAppState
import kotlinx.coroutines.flow.collectLatest

@Composable
internal fun NavigationVisibleEffect(appState: AppState = rememberAppState()) {
    val lifecycleOwner = LocalLifecycleOwner.current

    LaunchedEffect(appState, lifecycleOwner) {
        snapshotFlow { appState.isNavigationVisible }
            .flowWithLifecycle(lifecycleOwner.lifecycle)
            .collectLatest { isVisible ->
                if (isVisible) {
                    appState.scaffoldState.show()
                } else {
                    appState.scaffoldState.hide()
                }
            }
    }
}
