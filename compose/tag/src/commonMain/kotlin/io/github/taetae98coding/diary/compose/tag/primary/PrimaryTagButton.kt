package io.github.taetae98coding.diary.compose.tag.primary

import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import io.github.taetae98coding.diary.compose.core.animation.DiaryCrossfade
import io.github.taetae98coding.diary.compose.icon.StarBorderIcon
import io.github.taetae98coding.diary.compose.icon.StarIcon
import io.github.taetae98coding.diary.compose.tag.Res
import io.github.taetae98coding.diary.compose.tag.previewTag
import io.github.taetae98coding.diary.compose.tag.primary_tag_set_button_content_description
import io.github.taetae98coding.diary.compose.tag.primary_tag_unset_button_content_description
import io.github.taetae98coding.diary.compose.theme.DiaryTheme
import io.github.taetae98coding.diary.compose.theme.preview.ComponentPreview
import io.github.taetae98coding.diary.core.model.tag.Tag
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun PrimaryTagButton(
    onEvent: (PrimaryTagPickerEvent) -> Unit,
    tag: Tag? = null,
    isPrimary: Boolean = false,
) {
    IconButton(
        onClick = {
            if (isPrimary) {
                onEvent(PrimaryTagPickerEvent.UnselectPrimary)
            } else {
                tag?.let { value -> onEvent(PrimaryTagPickerEvent.SelectPrimary(id = value.id)) }
            }
        },
        enabled = tag != null,
    ) {
        DiaryCrossfade(targetState = isPrimary) { primary ->
            if (primary) {
                StarIcon(contentDescription = stringResource(Res.string.primary_tag_unset_button_content_description))
            } else {
                StarBorderIcon(contentDescription = tag?.let { stringResource(Res.string.primary_tag_set_button_content_description) })
            }
        }
    }
}

@ComponentPreview
@Composable
private fun PrimaryTagButtonPreview() {
    val tag = remember { previewTag(emoji = "💼", title = "업무", color = 0xFF3A7BD5) }

    DiaryTheme {
        Surface {
            PrimaryTagButton(
                onEvent = {},
                tag = tag,
                isPrimary = true,
            )
        }
    }
}
