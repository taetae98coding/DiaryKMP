package io.github.taetae98coding.diary.app.shared.scaffold

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.taetae98coding.diary.app.shared.AppState

@Composable
internal expect fun AppNavigationSuiteScaffold(
    appState: AppState,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
)
