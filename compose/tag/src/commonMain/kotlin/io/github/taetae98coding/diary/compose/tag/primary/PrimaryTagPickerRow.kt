package io.github.taetae98coding.diary.compose.tag.primary

import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import io.github.taetae98coding.diary.compose.core.dialog.DiaryPickerRow
import io.github.taetae98coding.diary.compose.tag.previewTag
import io.github.taetae98coding.diary.compose.theme.DiaryTheme
import io.github.taetae98coding.diary.compose.theme.preview.ComponentPreview
import io.github.taetae98coding.diary.core.model.tag.Tag
import io.github.taetae98coding.diary.library.compose.ui.color.toColor

@Composable
internal fun PrimaryTagPickerRow(
    onEvent: (PrimaryTagPickerEvent) -> Unit,
    modifier: Modifier = Modifier,
    tag: Tag? = null,
    isSelected: Boolean = false,
    isPrimary: Boolean = false,
) {
    DiaryPickerRow(
        isSelected = isSelected,
        onSelectedChange = {
            if (isSelected) {
                tag?.let { value -> onEvent(PrimaryTagPickerEvent.Unselect(id = value.id)) }
            } else {
                tag?.let { value -> onEvent(PrimaryTagPickerEvent.Select(id = value.id)) }
            }
        },
        color = tag?.detail?.color?.toColor() ?: Color.Transparent,
        label = tag?.detail?.emojiWithTitle.orEmpty(),
        modifier = modifier,
        enabled = tag != null,
        trailing = {
            PrimaryTagButton(
                onEvent = onEvent,
                tag = tag,
                isPrimary = isPrimary,
            )
        },
    )
}

@ComponentPreview
@Composable
private fun PrimaryTagPickerRowPreview() {
    val tag = remember { previewTag(emoji = "💼", title = "업무", color = 0xFF3A7BD5) }

    DiaryTheme {
        Surface {
            PrimaryTagPickerRow(
                onEvent = {},
                tag = tag,
                isSelected = true,
                isPrimary = true,
            )
        }
    }
}
