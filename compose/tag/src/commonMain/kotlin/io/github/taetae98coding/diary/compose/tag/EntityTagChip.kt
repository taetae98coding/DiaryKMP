package io.github.taetae98coding.diary.compose.tag

import androidx.compose.material3.Surface
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
import io.github.taetae98coding.diary.library.compose.ui.color.toColor
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun EntityTagChip(
    tag: Tag,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val detailActionLabel = stringResource(Res.string.entity_tag_detail_action)

    DiaryAssistChip(
        onClick = onClick,
        label = tag.detail.emojiWithTitle,
        modifier =
            modifier.semantics {
                // 칩의 클릭 동작은 유지하고 이름만 덧붙이도록 action을 비워 둔다.
                onClick(label = detailActionLabel, action = null)
            },
        leadingIcon = { DiaryColorIndicator(color = tag.detail.color.toColor()) },
    )
}

@ComponentPreview
@Composable
private fun EntityTagChipPreview() {
    val tag = remember { previewTag(emoji = "💼", title = "업무", color = 0xFF3A7BD5) }

    DiaryTheme {
        Surface {
            EntityTagChip(
                tag = tag,
                onClick = {},
            )
        }
    }
}
