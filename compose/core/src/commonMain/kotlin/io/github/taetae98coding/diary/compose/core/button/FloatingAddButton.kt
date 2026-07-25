package io.github.taetae98coding.diary.compose.core.button

import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularWavyProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import io.github.taetae98coding.diary.compose.core.animation.DiaryCrossfade
import io.github.taetae98coding.diary.compose.core.icon.AddIcon
import io.github.taetae98coding.diary.compose.core.preview.BooleanPreviewParameter
import io.github.taetae98coding.diary.compose.core.preview.ComponentPreview
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.compose.core.tooltip.DiaryTooltipBox

private val InProgressIndicatorSize = 24.dp

@Composable
public fun FloatingAddButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
    isInProgressProvider: () -> Boolean = { false },
) {
    DiaryTooltipBox(text = contentDescription.orEmpty()) {
        FloatingActionButton(
            onClick = onClick,
            modifier = modifier,
        ) {
            DiaryCrossfade(targetState = isInProgressProvider()) { isInProgress ->
                if (isInProgress) {
                    CircularWavyProgressIndicator(modifier = Modifier.size(InProgressIndicatorSize))
                } else {
                    AddIcon(contentDescription = contentDescription)
                }
            }
        }
    }
}

@ComponentPreview
@Composable
private fun FloatingAddButtonPreview(
    @PreviewParameter(BooleanPreviewParameter::class) isInProgress: Boolean,
) {
    DiaryTheme {
        Surface {
            FloatingAddButton(
                onClick = {},
                isInProgressProvider = { isInProgress },
            )
        }
    }
}
