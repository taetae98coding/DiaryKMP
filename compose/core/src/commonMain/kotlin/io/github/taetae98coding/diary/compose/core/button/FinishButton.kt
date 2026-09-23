package io.github.taetae98coding.diary.compose.core.button

import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularWavyProgressIndicator
import androidx.compose.material3.IconToggleButton
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import io.github.taetae98coding.diary.compose.core.animation.DiaryCrossfade
import io.github.taetae98coding.diary.compose.core.icon.FinishIcon
import io.github.taetae98coding.diary.compose.core.preview.ComponentPreview
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.compose.core.tooltip.DiaryTooltipBox

@Composable
public fun FinishButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isFinishedProvider: () -> Boolean = { false },
    contentDescription: String? = null,
    isInProgressProvider: () -> Boolean = { false },
) {
    DiaryTooltipBox(text = contentDescription.orEmpty()) {
        IconToggleButton(
            checked = isFinishedProvider(),
            onCheckedChange = { onClick() },
            modifier = modifier,
        ) {
            DiaryCrossfade(targetState = isInProgressProvider()) { isInProgress ->
                if (isInProgress) {
                    CircularWavyProgressIndicator(modifier = Modifier.size(DiaryTheme.dimens.inProgressIndicatorSize))
                } else {
                    FinishIcon(contentDescription = contentDescription)
                }
            }
        }
    }
}

private class FinishButtonPreviewParameter : PreviewParameterProvider<FinishButtonPreviewState> {
    override val values: Sequence<FinishButtonPreviewState> =
        sequenceOf(
            FinishButtonPreviewState(),
            FinishButtonPreviewState(isFinished = true),
            FinishButtonPreviewState(isInProgress = true),
        )
}

private data class FinishButtonPreviewState(
    val isFinished: Boolean = false,
    val isInProgress: Boolean = false,
)

@ComponentPreview
@Composable
private fun FinishButtonPreview(
    @PreviewParameter(FinishButtonPreviewParameter::class) state: FinishButtonPreviewState,
) {
    DiaryTheme {
        Surface {
            FinishButton(
                onClick = {},
                isFinishedProvider = { state.isFinished },
                isInProgressProvider = { state.isInProgress },
            )
        }
    }
}
