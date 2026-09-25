package io.github.taetae98coding.diary.compose.core.button

import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularWavyProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewParameter
import io.github.taetae98coding.diary.compose.core.animation.DiaryCrossfade
import io.github.taetae98coding.diary.compose.core.icon.CheckIcon
import io.github.taetae98coding.diary.compose.core.preview.BooleanPreviewParameter
import io.github.taetae98coding.diary.compose.core.preview.ComponentPreview
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.compose.core.tooltip.DiaryTooltipBox

@Composable
public fun FloatingCheckButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
    isInProgressProvider: () -> Boolean = { false },
) {
    DiaryTooltipBox(text = contentDescription.orEmpty()) {
        FloatingActionButton(
            onClick = onClick,
            modifier = modifier.buttonContentDescription(contentDescription = contentDescription),
        ) {
            DiaryCrossfade(targetState = isInProgressProvider()) { isInProgress ->
                if (isInProgress) {
                    CircularWavyProgressIndicator(modifier = Modifier.size(DiaryTheme.dimens.inProgressIndicatorSize))
                } else {
                    CheckIcon(contentDescription = null)
                }
            }
        }
    }
}

@ComponentPreview
@Composable
private fun FloatingCheckButtonPreview(
    @PreviewParameter(BooleanPreviewParameter::class) isInProgress: Boolean,
) {
    DiaryTheme {
        Surface {
            FloatingCheckButton(
                onClick = {},
                isInProgressProvider = { isInProgress },
            )
        }
    }
}
