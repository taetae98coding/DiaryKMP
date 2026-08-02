package io.github.taetae98coding.diary.feature.tag.ui.link

import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import io.github.taetae98coding.diary.compose.core.dialog.DiaryPickerRow
import io.github.taetae98coding.diary.compose.core.preview.ComponentPreview
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.core.model.tag.Tag
import io.github.taetae98coding.diary.feature.tag.ui.previewTag
import io.github.taetae98coding.diary.library.compose.ui.color.toColor

@Composable
internal fun TagLinkPickerRow(
    onEvent: (TagLinkPickerEvent) -> Unit,
    modifier: Modifier = Modifier,
    tag: Tag? = null,
    isLinked: Boolean = false,
) {
    DiaryPickerRow(
        isSelected = isLinked,
        onSelectedChange = {
            if (isLinked) {
                tag?.let { value -> onEvent(TagLinkPickerEvent.Unlink(id = value.id)) }
            } else {
                tag?.let { value -> onEvent(TagLinkPickerEvent.Link(id = value.id)) }
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
private fun TagLinkPickerRowPreview() {
    val tag = remember { previewTag(emoji = "💼", title = "업무", color = 0xFF3A7BD5) }

    DiaryTheme {
        Surface {
            TagLinkPickerRow(
                onEvent = {},
                tag = tag,
                isLinked = true,
            )
        }
    }
}
