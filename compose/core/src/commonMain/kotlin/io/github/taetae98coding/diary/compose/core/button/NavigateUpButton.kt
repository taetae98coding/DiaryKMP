package io.github.taetae98coding.diary.compose.core.button

import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.taetae98coding.diary.compose.core.icon.NavigateUpIcon
import io.github.taetae98coding.diary.compose.core.preview.ComponentPreview
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.compose.core.tooltip.DiaryTooltipBox

@Composable
public fun NavigateUpButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
) {
    DiaryTooltipBox(text = contentDescription.orEmpty()) {
        IconButton(
            onClick = onClick,
            modifier = modifier,
        ) {
            NavigateUpIcon(contentDescription = contentDescription)
        }
    }
}

@ComponentPreview
@Composable
private fun NavigateUpButtonPreview() {
    DiaryTheme {
        Surface {
            NavigateUpButton(onClick = {})
        }
    }
}
