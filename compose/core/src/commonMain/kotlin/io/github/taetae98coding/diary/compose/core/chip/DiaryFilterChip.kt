package io.github.taetae98coding.diary.compose.core.chip

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.material3.SelectableChipColors
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import io.github.taetae98coding.diary.compose.core.preview.ComponentPreview
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme

@Composable
public fun DiaryFilterChip(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    selected: Boolean = false,
    enabled: Boolean = true,
    leadingIcon: @Composable (() -> Unit)? = null,
    colors: SelectableChipColors = FilterChipDefaults.filterChipColors(),
    border: BorderStroke? = FilterChipDefaults.filterChipBorder(enabled = enabled, selected = selected),
) {
    CompositionLocalProvider(
        LocalMinimumInteractiveComponentSize provides Dp.Unspecified,
    ) {
        FilterChip(
            selected = selected,
            onClick = onClick,
            label = { DiaryChipLabel(label = label) },
            modifier = modifier.shrinkToAvailableWidth(),
            enabled = enabled,
            leadingIcon = leadingIcon,
            shape = CircleShape,
            colors = colors,
            border = border,
        )
    }
}

@ComponentPreview
@Composable
private fun DiaryFilterChipPreview() {
    DiaryTheme {
        Surface {
            DiaryFilterChip(
                label = "Tag",
                onClick = {},
            )
        }
    }
}
