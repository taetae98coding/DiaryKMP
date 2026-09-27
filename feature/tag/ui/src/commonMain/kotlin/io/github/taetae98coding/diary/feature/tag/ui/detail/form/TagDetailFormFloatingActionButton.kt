package io.github.taetae98coding.diary.feature.tag.ui.detail.form

import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewParameter
import io.github.taetae98coding.diary.compose.core.button.FloatingCheckButton
import io.github.taetae98coding.diary.compose.core.preview.BooleanPreviewParameter
import io.github.taetae98coding.diary.compose.core.preview.ComponentPreview
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.feature.tag.ui.Res
import io.github.taetae98coding.diary.feature.tag.ui.tag_detail_update_button_content_description
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun TagDetailFormFloatingActionButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isInProgressProvider: () -> Boolean = { false },
) {
    FloatingCheckButton(
        onClick = onClick,
        contentDescription = stringResource(Res.string.tag_detail_update_button_content_description),
        modifier = modifier,
        isInProgressProvider = isInProgressProvider,
    )
}

@ComponentPreview
@Composable
private fun TagDetailFormFloatingActionButtonPreview(
    @PreviewParameter(BooleanPreviewParameter::class) isInProgress: Boolean,
) {
    DiaryTheme {
        Surface {
            TagDetailFormFloatingActionButton(
                onClick = {},
                isInProgressProvider = { isInProgress },
            )
        }
    }
}
