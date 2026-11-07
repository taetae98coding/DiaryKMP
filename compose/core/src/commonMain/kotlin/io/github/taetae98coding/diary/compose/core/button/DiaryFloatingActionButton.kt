package io.github.taetae98coding.diary.compose.core.button

import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.PreviewParameter
import io.github.taetae98coding.diary.compose.core.icon.Add
import io.github.taetae98coding.diary.compose.core.icon.DiaryIcons
import io.github.taetae98coding.diary.compose.core.preview.BooleanPreviewParameter
import io.github.taetae98coding.diary.compose.core.preview.ComponentPreview
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme

@Composable
internal fun DiaryFloatingActionButton(
    onClick: () -> Unit,
    imageVector: ImageVector,
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
    isInProgressProvider: () -> Boolean = { false },
) {
    val scope =
        DiaryFloatingActionButtonOverrideScope(
            onClick = onClick,
            imageVector = imageVector,
            modifier = modifier,
            contentDescription = contentDescription,
            isInProgressProvider = isInProgressProvider,
        )

    with(LocalDiaryFloatingActionButtonOverride.current) { scope.FloatingActionButton() }
}

@ComponentPreview
@Composable
private fun DiaryFloatingActionButtonPreview(
    @PreviewParameter(BooleanPreviewParameter::class) isInProgress: Boolean,
) {
    DiaryTheme {
        Surface {
            DiaryFloatingActionButton(
                onClick = {},
                imageVector = DiaryIcons.Add,
                isInProgressProvider = { isInProgress },
            )
        }
    }
}
