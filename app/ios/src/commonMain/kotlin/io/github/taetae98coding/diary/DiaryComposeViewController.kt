package io.github.taetae98coding.diary

import androidx.compose.ui.window.ComposeUIViewController
import io.github.taetae98coding.diary.app.shared.App
import io.github.taetae98coding.diary.app.shared.scaffold.AppTabBarController
import io.github.taetae98coding.diary.compose.core.ProvideUiKitComponentOverrides
import platform.UIKit.UIViewController

@Suppress("FunctionName")
public fun DiaryComposeViewController(
    topBarRenderer: DiaryTopBarRenderer,
    floatingActionButtonRenderer: DiaryFloatingActionButtonRenderer,
): UIViewController {
    val uiKitTopBarRenderer = DiaryTopBarRendererAdapter(renderer = topBarRenderer)
    val uiKitFloatingActionButtonRenderer = DiaryFloatingActionButtonRendererAdapter(renderer = floatingActionButtonRenderer)

    return AppTabBarController(
        content =
            ComposeUIViewController {
                ProvideUiKitComponentOverrides(
                    topBarRenderer = uiKitTopBarRenderer,
                    floatingActionButtonRenderer = uiKitFloatingActionButtonRenderer,
                ) {
                    App(dependencies = DiaryStartupInitializer.dependencies)
                }
            },
    )
}
