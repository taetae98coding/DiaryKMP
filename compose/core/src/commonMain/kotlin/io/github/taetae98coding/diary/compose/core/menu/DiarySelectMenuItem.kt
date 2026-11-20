package io.github.taetae98coding.diary.compose.core.menu

import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.PreviewParameter
import io.github.taetae98coding.diary.compose.icon.CheckIcon
import io.github.taetae98coding.diary.compose.theme.DiaryTheme
import io.github.taetae98coding.diary.compose.theme.preview.BooleanPreviewParameter
import io.github.taetae98coding.diary.compose.theme.preview.ComponentPreview

@Composable
public fun DiarySelectMenuItem(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    DropdownMenuItem(
        text = { Text(text = text) },
        onClick = onClick,
        modifier = modifier.semantics { selected = isSelected },
        leadingIcon = { CheckIcon(modifier = Modifier.alpha(if (isSelected) 1F else 0F)) },
    )
}

@ComponentPreview
@Composable
private fun DiarySelectMenuItemPreview(
    @PreviewParameter(BooleanPreviewParameter::class) isSelected: Boolean,
) {
    DiaryTheme {
        Surface {
            DiarySelectMenuItem(
                text = "말일로 당김",
                isSelected = isSelected,
                onClick = {},
            )
        }
    }
}
