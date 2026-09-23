package io.github.taetae98coding.diary.compose.tag.entity

import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import io.github.taetae98coding.diary.compose.core.dialog.DiaryPickerRow
import io.github.taetae98coding.diary.compose.core.preview.ComponentPreview
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.compose.tag.previewTag
import io.github.taetae98coding.diary.core.model.tag.Tag
import io.github.taetae98coding.diary.library.compose.ui.color.toColor

@Composable
internal fun EntityTagPickerRow(
    onEvent: (EntityTagPickerEvent) -> Unit,
    modifier: Modifier = Modifier,
    tag: Tag? = null,
    isSelected: Boolean = false,
) {
    DiaryPickerRow(
        isSelected = isSelected,
        onSelectedChange = {
            if (isSelected) {
                tag?.let { value -> onEvent(EntityTagPickerEvent.Remove(id = value.id)) }
            } else {
                tag?.let { value -> onEvent(EntityTagPickerEvent.Add(id = value.id)) }
            }
        },
        color = tag?.detail?.color?.toColor() ?: Color.Transparent,
        label = tag?.detail?.emojiWithTitle.orEmpty(),
        modifier = modifier,
        enabled = tag != null,
    )
}

@ComponentPreview
@Composable
private fun EntityTagPickerRowPreview() {
    val tag = remember { previewTag(emoji = "💼", title = "업무", color = 0xFF3A7BD5) }

    DiaryTheme {
        Surface {
            EntityTagPickerRow(
                onEvent = {},
                tag = tag,
                isSelected = true,
            )
        }
    }
}
