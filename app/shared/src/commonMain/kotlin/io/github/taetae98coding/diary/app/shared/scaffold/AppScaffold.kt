package io.github.taetae98coding.diary.app.shared.scaffold

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.taetae98coding.diary.app.shared.AppState
import io.github.taetae98coding.diary.app.shared.navigation.AppNavigation
import io.github.taetae98coding.diary.app.shared.rememberAppState
import io.github.taetae98coding.diary.compose.core.preview.ScreenPreview
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme

@Composable
internal fun AppScaffold(
    modifier: Modifier = Modifier,
    appState: AppState = rememberAppState(),
    content: @Composable () -> Unit = {
        AppNavigation(
            appState = appState,
            modifier = Modifier.fillMaxSize(),
        )
    },
) {
    NavigationVisibleEffect(
        appState = appState,
    )

    AppNavigationSuiteScaffold(
        appState = appState,
        modifier = modifier.navigationShortcut(appState),
        content = content,
    )
}

@ScreenPreview
@Composable
private fun AppScaffoldPreview() {
    DiaryTheme {
        AppScaffold(
            content = { Text(text = "본문") },
        )
    }
}
