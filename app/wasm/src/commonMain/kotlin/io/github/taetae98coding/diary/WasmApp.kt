@file:OptIn(
    ExperimentalComposeUiApi::class,
    ExperimentalResourceApi::class,
)

package io.github.taetae98coding.diary

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import io.github.taetae98coding.diary.app.App
import io.github.taetae98coding.diary.app.initializer.StartupInitializer
import kotlinx.browser.document
import org.jetbrains.compose.resources.ExperimentalResourceApi

internal fun main() {
    StartupInitializer.initialize(isDebug = true)

    ComposeViewport(requireNotNull(document.body)) {
        App()
    }
}
