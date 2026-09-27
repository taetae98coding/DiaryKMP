package io.github.taetae98coding.diary.compose.core.chip

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.ChipColors
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import io.github.taetae98coding.diary.compose.core.preview.ComponentPreview
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme

@Composable
public fun DiaryAssistChip(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    leadingIcon: @Composable (() -> Unit)? = null,
    colors: ChipColors = AssistChipDefaults.assistChipColors(),
    border: BorderStroke? = AssistChipDefaults.assistChipBorder(enabled = true),
) {
    CompositionLocalProvider(
        LocalMinimumInteractiveComponentSize provides Dp.Unspecified,
    ) {
        AssistChip(
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
private fun DiaryAssistChipPreview() {
    DiaryTheme {
        Surface {
            DiaryAssistChip(
                label = "Tag",
                onClick = {},
            )
        }
    }
}
