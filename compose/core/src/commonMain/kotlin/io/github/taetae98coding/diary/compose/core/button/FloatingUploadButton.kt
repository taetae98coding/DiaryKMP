package io.github.taetae98coding.diary.compose.core.button

import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewParameter
import io.github.taetae98coding.diary.compose.core.icon.DiaryIcons
import io.github.taetae98coding.diary.compose.core.icon.Upload
import io.github.taetae98coding.diary.compose.core.preview.BooleanPreviewParameter
import io.github.taetae98coding.diary.compose.core.preview.ComponentPreview
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme

@Composable
public fun FloatingUploadButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
    isInProgressProvider: () -> Boolean = { false },
) {
    DiaryFloatingActionButton(
        onClick = onClick,
        imageVector = DiaryIcons.Upload,
        modifier = modifier,
        contentDescription = contentDescription,
        isInProgressProvider = isInProgressProvider,
    )
}

@ComponentPreview
@Composable
private fun FloatingUploadButtonPreview(
    @PreviewParameter(BooleanPreviewParameter::class) isInProgress: Boolean,
) {
    DiaryTheme {
        Surface {
            FloatingUploadButton(
                onClick = {},
                isInProgressProvider = { isInProgress },
            )
        }
    }
}
