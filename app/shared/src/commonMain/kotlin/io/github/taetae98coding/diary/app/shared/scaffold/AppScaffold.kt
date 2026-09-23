package io.github.taetae98coding.diary.app.shared.scaffold

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteItem
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.taetae98coding.diary.app.shared.AppState
import io.github.taetae98coding.diary.app.shared.navigation.AppNavigation
import io.github.taetae98coding.diary.app.shared.navigation.topLevelNavigationList
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

    NavigationSuiteScaffold(
        navigationItems = {
            topLevelNavigationList.forEach { topLevelNavigation ->
                NavigationSuiteItem(
                    selected = appState.currentTopLevelNavigation == topLevelNavigation,
                    onClick = { appState.navigateTo(topLevelNavigation) },
                    icon = { TopLevelNavigationIcon(topLevelNavigation = topLevelNavigation) },
                    label = { TopLevelNavigationLabel(topLevelNavigation = topLevelNavigation) },
                )
            }
        },
        modifier = modifier.navigationShortcut(appState),
        state = appState.scaffoldState,
    ) {
        content()
    }
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
