package io.github.taetae98coding.diary.feature.memo.ui.tag

import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.semantics
import io.github.taetae98coding.diary.compose.core.chip.DiaryAssistChip
import io.github.taetae98coding.diary.compose.core.color.DiaryColorIndicator
import io.github.taetae98coding.diary.compose.core.preview.ComponentPreview
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.core.model.tag.Tag
import io.github.taetae98coding.diary.feature.memo.ui.Res
import io.github.taetae98coding.diary.feature.memo.ui.memo_tag_detail_action
import io.github.taetae98coding.diary.feature.memo.ui.memo_tag_primary_content_description
import io.github.taetae98coding.diary.feature.memo.ui.previewTag
import io.github.taetae98coding.diary.library.compose.ui.color.contentColor
import io.github.taetae98coding.diary.library.compose.ui.color.toColor
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun MemoTagChip(
    tag: Tag,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isPrimary: Boolean = false,
) {
    val color = tag.detail.color.toColor()
    val primaryContentDescription = stringResource(Res.string.memo_tag_primary_content_description)
    val detailActionLabel = stringResource(Res.string.memo_tag_detail_action)
    val leadingIcon: (@Composable () -> Unit)? =
        if (isPrimary) {
            null
        } else {
            { DiaryColorIndicator(color = color) }
        }

    DiaryAssistChip(
        onClick = onClick,
        label = { Text(text = tag.detail.emojiWithTitle) },
        modifier =
            modifier.semantics {
                // 칩의 클릭 동작은 유지하고 이름만 덧붙이도록 action을 비워 둔다.
                onClick(label = detailActionLabel, action = null)
                if (isPrimary) contentDescription = primaryContentDescription
            },
        leadingIcon = leadingIcon,
        colors =
            if (isPrimary) {
                AssistChipDefaults.assistChipColors(
                    containerColor = color,
                    labelColor = color.contentColor(),
                )
            } else {
                AssistChipDefaults.assistChipColors()
            },
        border = if (isPrimary) null else AssistChipDefaults.assistChipBorder(enabled = true),
    )
}

@ComponentPreview
@Composable
private fun MemoTagChipPreview() {
    val tag = remember { previewTag(emoji = "💼", title = "업무", color = 0xFF3A7BD5) }

    DiaryTheme {
        Surface {
            MemoTagChip(
                tag = tag,
                onClick = {},
                isPrimary = true,
            )
        }
    }
}
