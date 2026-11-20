package io.github.taetae98coding.diary.compose.core.button

import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewParameter
import io.github.taetae98coding.diary.compose.icon.Check
import io.github.taetae98coding.diary.compose.icon.DiaryIcons
import io.github.taetae98coding.diary.compose.theme.DiaryTheme
import io.github.taetae98coding.diary.compose.theme.preview.BooleanPreviewParameter
import io.github.taetae98coding.diary.compose.theme.preview.ComponentPreview

@Composable
public fun FloatingCheckButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
    isInProgressProvider: () -> Boolean = { false },
) {
    DiaryFloatingActionButton(
        onClick = onClick,
        imageVector = DiaryIcons.Check,
        modifier = modifier,
        contentDescription = contentDescription,
        isInProgressProvider = isInProgressProvider,
    )
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
