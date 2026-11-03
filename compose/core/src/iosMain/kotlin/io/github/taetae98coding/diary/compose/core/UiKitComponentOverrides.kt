package io.github.taetae98coding.diary.compose.core

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import io.github.taetae98coding.diary.compose.core.appbar.LocalDiaryTopBarOverride
import io.github.taetae98coding.diary.compose.core.appbar.UiKitTopBarOverride
import io.github.taetae98coding.diary.compose.core.appbar.UiKitTopBarRenderer
import io.github.taetae98coding.diary.compose.core.button.LocalDiaryFloatingActionButtonMenuOverride
import io.github.taetae98coding.diary.compose.core.button.LocalDiaryFloatingActionButtonOverride
import io.github.taetae98coding.diary.compose.core.button.UiKitFloatingActionButtonMenuOverride
import io.github.taetae98coding.diary.compose.core.button.UiKitFloatingActionButtonOverride
import io.github.taetae98coding.diary.compose.core.button.UiKitFloatingActionButtonRenderer

@Composable
public fun ProvideUiKitComponentOverrides(
    topBarRenderer: UiKitTopBarRenderer,
    floatingActionButtonRenderer: UiKitFloatingActionButtonRenderer,
    content: @Composable () -> Unit,
) {
    val topBarOverride = remember(topBarRenderer) { UiKitTopBarOverride(renderer = topBarRenderer) }
    val floatingActionButtonOverride = remember(floatingActionButtonRenderer) { UiKitFloatingActionButtonOverride(renderer = floatingActionButtonRenderer) }
    val floatingActionButtonMenuOverride =
        remember(floatingActionButtonRenderer) { UiKitFloatingActionButtonMenuOverride(renderer = floatingActionButtonRenderer) }

    CompositionLocalProvider(
        LocalDiaryTopBarOverride provides topBarOverride,
        LocalDiaryFloatingActionButtonOverride provides floatingActionButtonOverride,
        LocalDiaryFloatingActionButtonMenuOverride provides floatingActionButtonMenuOverride,
        content = content,
    )
}
