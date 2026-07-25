package io.github.taetae98coding.diary.compose.core.layout

import androidx.compose.material3.adaptive.currentWindowAdaptiveInfoV2
import androidx.compose.runtime.Composable
import androidx.window.core.layout.WindowSizeClass

@Composable
public fun isCompactWidth(): Boolean =
    !currentWindowAdaptiveInfoV2()
        .windowSizeClass
        .isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_MEDIUM_LOWER_BOUND)
