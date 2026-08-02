package io.github.taetae98coding.diary.feature.tag.ui.link

import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.semantics
import io.github.taetae98coding.diary.compose.core.chip.DiaryAssistChip
import io.github.taetae98coding.diary.compose.core.color.DiaryColorIndicator
import io.github.taetae98coding.diary.compose.core.preview.ComponentPreview
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.core.model.tag.Tag
import io.github.taetae98coding.diary.feature.tag.ui.Res
import io.github.taetae98coding.diary.feature.tag.ui.previewTag
import io.github.taetae98coding.diary.feature.tag.ui.tag_link_detail_action
import io.github.taetae98coding.diary.library.compose.ui.color.toColor
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun TagLinkChip(
    tag: Tag,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val detailActionLabel = stringResource(Res.string.tag_link_detail_action)

    DiaryAssistChip(
        onClick = onClick,
        label = { Text(text = tag.detail.emojiWithTitle) },
        modifier =
            modifier.semantics {
                // 칩의 클릭 동작은 유지하고 이름만 덧붙이도록 action을 비워 둔다.
                onClick(label = detailActionLabel, action = null)
            },
        leadingIcon = { DiaryColorIndicator(color = tag.detail.color.toColor()) },
        colors = AssistChipDefaults.assistChipColors(),
        border = AssistChipDefaults.assistChipBorder(enabled = true),
    )
}

@ComponentPreview
@Composable
private fun TagLinkChipPreview() {
    val tag = remember { previewTag(emoji = "💼", title = "업무", color = 0xFF3A7BD5) }

    DiaryTheme {
        Surface {
            TagLinkChip(
                tag = tag,
                onClick = {},
            )
        }
    }
}
