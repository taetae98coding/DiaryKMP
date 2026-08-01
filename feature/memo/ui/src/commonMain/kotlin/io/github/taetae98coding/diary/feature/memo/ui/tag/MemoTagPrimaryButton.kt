package io.github.taetae98coding.diary.feature.memo.ui.tag

import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import io.github.taetae98coding.diary.compose.core.animation.DiaryCrossfade
import io.github.taetae98coding.diary.compose.core.icon.StarBorderIcon
import io.github.taetae98coding.diary.compose.core.icon.StarIcon
import io.github.taetae98coding.diary.compose.core.preview.ComponentPreview
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.core.model.tag.Tag
import io.github.taetae98coding.diary.feature.memo.ui.Res
import io.github.taetae98coding.diary.feature.memo.ui.memo_tag_primary_set_button_content_description
import io.github.taetae98coding.diary.feature.memo.ui.memo_tag_primary_unset_button_content_description
import io.github.taetae98coding.diary.feature.memo.ui.previewTag
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun MemoTagPrimaryButton(
    onEvent: (MemoTagPickerEvent) -> Unit,
    tag: Tag? = null,
    isPrimary: Boolean = false,
) {
    IconButton(
        onClick = {
            if (isPrimary) {
                onEvent(MemoTagPickerEvent.UnselectPrimary)
            } else {
                tag?.let { value -> onEvent(MemoTagPickerEvent.SelectPrimary(id = value.id)) }
            }
        },
        enabled = tag != null,
    ) {
        DiaryCrossfade(targetState = isPrimary) { primary ->
            if (primary) {
                StarIcon(contentDescription = stringResource(Res.string.memo_tag_primary_unset_button_content_description))
            } else {
                StarBorderIcon(contentDescription = tag?.let { stringResource(Res.string.memo_tag_primary_set_button_content_description) })
            }
        }
    }
}

@ComponentPreview
@Composable
private fun MemoTagPrimaryButtonPreview() {
    val tag = remember { previewTag(emoji = "💼", title = "업무", color = 0xFF3A7BD5) }

    DiaryTheme {
        Surface {
            MemoTagPrimaryButton(
                onEvent = {},
                tag = tag,
                isPrimary = true,
            )
        }
    }
}
