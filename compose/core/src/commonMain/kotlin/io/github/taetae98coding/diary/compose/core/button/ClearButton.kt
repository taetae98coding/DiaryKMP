package io.github.taetae98coding.diary.compose.core.button

import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.taetae98coding.diary.compose.core.Res
import io.github.taetae98coding.diary.compose.core.clear_text_field_button_content_description
import io.github.taetae98coding.diary.compose.core.icon.ClearIcon
import io.github.taetae98coding.diary.compose.core.preview.ComponentPreview
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.compose.core.tooltip.DiaryTooltipBox
import org.jetbrains.compose.resources.stringResource

@Composable
public fun ClearButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    contentDescription: String? = stringResource(Res.string.clear_text_field_button_content_description),
) {
    DiaryTooltipBox(text = contentDescription.orEmpty()) {
        IconButton(
            onClick = onClick,
            modifier = modifier,
        ) {
            ClearIcon(contentDescription = contentDescription)
        }
    }
}

@ComponentPreview
@Composable
private fun ClearButtonPreview() {
    DiaryTheme {
        Surface {
            ClearButton(onClick = {})
        }
    }
}
