@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package io.github.taetae98coding.diary.compose.core.listitem

import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.ListItemColors
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.ListItemShapes
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme

public object DiarySegmentedListItemDefaults {
    public val Gap: Dp = ListItemDefaults.SegmentedGap

    // Material 3 한 줄 목록 항목의 최소 높이. ListItemDefaults가 값을 공개하지 않아 여기 둔다.
    public val MinHeight: Dp = 56.dp

    @Composable
    public fun colors(): ListItemColors = ListItemDefaults.segmentedColors(containerColor = DiaryTheme.colorScheme.surfaceContainerHighest)

    @Composable
    public fun shapes(
        index: Int,
        count: Int,
    ): ListItemShapes = ListItemDefaults.segmentedShapes(index = index, count = count)
}
