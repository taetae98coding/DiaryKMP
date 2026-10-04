package io.github.taetae98coding.diary.feature.qr.ui.add.form

import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewParameter
import io.github.taetae98coding.diary.compose.core.icon.UndoIcon
import io.github.taetae98coding.diary.compose.core.preview.BooleanPreviewParameter
import io.github.taetae98coding.diary.compose.core.preview.ComponentPreview
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.compose.core.tooltip.DiaryTooltipBox
import io.github.taetae98coding.diary.feature.qr.ui.Res
import io.github.taetae98coding.diary.feature.qr.ui.qr_undo_button_content_description
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun QrUndoButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isEnabledProvider: () -> Boolean = { false },
) {
    val contentDescription = stringResource(Res.string.qr_undo_button_content_description)

    DiaryTooltipBox(text = contentDescription) {
        IconButton(
            onClick = onClick,
            modifier = modifier,
            enabled = isEnabledProvider(),
        ) {
            UndoIcon(contentDescription = contentDescription)
        }
    }
}

@ComponentPreview
@Composable
private fun QrUndoButtonPreview(
    @PreviewParameter(BooleanPreviewParameter::class) isEnabled: Boolean,
) {
    DiaryTheme {
        Surface {
            QrUndoButton(onClick = {}, isEnabledProvider = { isEnabled })
        }
    }
}
