@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package io.github.taetae98coding.diary.compose.core.listitem

import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.ListItemColors
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.ListItemShapes
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.Dp
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme

public object DiarySegmentedListItemDefaults {
    public val Gap: Dp = ListItemDefaults.SegmentedGap

    @Composable
    public fun colors(): ListItemColors = ListItemDefaults.segmentedColors(containerColor = DiaryTheme.colorScheme.surfaceContainerHighest)

    @Composable
    public fun shapes(
        index: Int,
        count: Int,
    ): ListItemShapes = ListItemDefaults.segmentedShapes(index = index, count = count)
}
