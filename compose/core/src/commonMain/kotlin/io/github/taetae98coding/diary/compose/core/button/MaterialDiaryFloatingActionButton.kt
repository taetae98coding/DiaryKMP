package io.github.taetae98coding.diary.compose.core.button

import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularWavyProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.PreviewParameter
import io.github.taetae98coding.diary.compose.core.animation.DiaryCrossfade
import io.github.taetae98coding.diary.compose.core.tooltip.DiaryTooltipBox
import io.github.taetae98coding.diary.compose.icon.Add
import io.github.taetae98coding.diary.compose.icon.DiaryIcons
import io.github.taetae98coding.diary.compose.theme.DiaryTheme
import io.github.taetae98coding.diary.compose.theme.preview.BooleanPreviewParameter
import io.github.taetae98coding.diary.compose.theme.preview.ComponentPreview

@Composable
internal fun MaterialDiaryFloatingActionButton(
    onClick: () -> Unit,
    imageVector: ImageVector,
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
                    Icon(imageVector = imageVector, contentDescription = null)
                }
            }
        }
    }
}

@ComponentPreview
@Composable
private fun MaterialDiaryFloatingActionButtonPreview(
    @PreviewParameter(BooleanPreviewParameter::class) isInProgress: Boolean,
) {
    DiaryTheme {
        Surface {
            MaterialDiaryFloatingActionButton(
                onClick = {},
                imageVector = DiaryIcons.Add,
                isInProgressProvider = { isInProgress },
            )
        }
    }
}
