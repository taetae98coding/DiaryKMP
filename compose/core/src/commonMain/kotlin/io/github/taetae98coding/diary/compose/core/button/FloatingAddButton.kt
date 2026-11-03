package io.github.taetae98coding.diary.compose.core.button

import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewParameter
import io.github.taetae98coding.diary.compose.core.icon.Add
import io.github.taetae98coding.diary.compose.core.icon.DiaryIcons
import io.github.taetae98coding.diary.compose.core.preview.BooleanPreviewParameter
import io.github.taetae98coding.diary.compose.core.preview.ComponentPreview
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme

@Composable
public fun FloatingAddButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
    isInProgressProvider: () -> Boolean = { false },
) {
    DiaryFloatingActionButton(
        onClick = onClick,
        imageVector = DiaryIcons.Add,
        modifier = modifier,
        contentDescription = contentDescription,
        isInProgressProvider = isInProgressProvider,
    )
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
