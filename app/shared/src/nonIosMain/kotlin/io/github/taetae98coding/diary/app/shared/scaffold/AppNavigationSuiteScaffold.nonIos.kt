package io.github.taetae98coding.diary.app.shared.scaffold

import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteItem
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.taetae98coding.diary.app.shared.AppState
import io.github.taetae98coding.diary.app.shared.navigation.topLevelNavigationList

@Composable
internal actual fun AppNavigationSuiteScaffold(
    appState: AppState,
    modifier: Modifier,
    content: @Composable () -> Unit,
) {
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
        modifier = modifier,
        state = appState.scaffoldState,
    ) {
        content()
    }
}
