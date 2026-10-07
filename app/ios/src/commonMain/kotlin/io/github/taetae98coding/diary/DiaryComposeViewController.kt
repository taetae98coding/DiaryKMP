package io.github.taetae98coding.diary

import androidx.compose.ui.window.ComposeUIViewController
import io.github.taetae98coding.diary.app.shared.App
import io.github.taetae98coding.diary.app.shared.scaffold.AppTabBarController
import platform.UIKit.UIViewController

@Suppress("FunctionName")
public fun DiaryComposeViewController(): UIViewController =
    AppTabBarController(
        content = ComposeUIViewController { App() },
    )
