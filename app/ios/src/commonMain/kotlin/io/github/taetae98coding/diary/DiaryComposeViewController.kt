package io.github.taetae98coding.diary

import androidx.compose.ui.window.ComposeUIViewController
import io.github.taetae98coding.diary.app.App
import platform.UIKit.UIViewController

@Suppress("FunctionName")
public fun DiaryComposeViewController(): UIViewController =
    ComposeUIViewController {
        App()
    }
