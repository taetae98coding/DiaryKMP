package io.github.taetae98coding.diary.app.shared.scaffold

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.uikit.LocalUIViewController
import io.github.taetae98coding.diary.app.shared.AppState
import io.github.taetae98coding.diary.app.shared.navigation.topLevelNavigationList
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import platform.UIKit.UIColor
import platform.UIKit.tabBarController

@Composable
internal actual fun AppNavigationSuiteScaffold(
    appState: AppState,
    modifier: Modifier,
    content: @Composable () -> Unit,
) {
    Box(modifier = modifier) {
        content()
    }

    val tabBarController = LocalUIViewController.current.tabBarController as? TopLevelTabBarController

    if (tabBarController != null) {
        TopLevelTabBarEffect(
            appState = appState,
            tabBarController = tabBarController,
        )
    }
}

@Composable
private fun TopLevelTabBarEffect(
    appState: AppState,
    tabBarController: TopLevelTabBarController,
) {
    val titleList = topLevelNavigationList.map { topLevelNavigation -> topLevelNavigation.label() }
    val imageList = topLevelNavigationList.map { topLevelNavigation -> rememberTopLevelTabBarImage(topLevelNavigation) }
    val tintColor = DiaryTheme.colorScheme.primary
    val currentTopLevelNavigation = appState.currentTopLevelNavigation
    val isNavigationVisible = appState.isNavigationVisible
    val navigateTo by rememberUpdatedState(appState::navigateTo)

    DisposableEffect(tabBarController) {
        tabBarController.onSelect = { topLevelNavigation -> navigateTo(topLevelNavigation) }

        onDispose { tabBarController.onSelect = null }
    }

    SideEffect {
        tabBarController.updateTabs(titleList = titleList, imageList = imageList)
        tabBarController.view.tintColor = tintColor.toUIColor()
        currentTopLevelNavigation?.let(tabBarController::select)
        tabBarController.setTabBarHidden(!isNavigationVisible, animated = true)
    }
}

private fun Color.toUIColor(): UIColor =
    UIColor(
        red = red.toDouble(),
        green = green.toDouble(),
        blue = blue.toDouble(),
        alpha = alpha.toDouble(),
    )
