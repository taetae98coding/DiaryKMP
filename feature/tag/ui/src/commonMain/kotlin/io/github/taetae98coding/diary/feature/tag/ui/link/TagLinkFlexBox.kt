package io.github.taetae98coding.diary.feature.tag.ui.link

import androidx.compose.animation.animateBounds
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import io.github.taetae98coding.diary.compose.core.chip.DiaryAddChip
import io.github.taetae98coding.diary.compose.core.layout.DiaryChipFlexBox
import io.github.taetae98coding.diary.compose.core.preview.ComponentPreview
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.feature.tag.ui.Res
import io.github.taetae98coding.diary.feature.tag.ui.previewTag
import io.github.taetae98coding.diary.feature.tag.ui.tag_link_action
import io.github.taetae98coding.diary.feature.tag.ui.tag_link_label
import org.jetbrains.compose.resources.stringResource
import kotlin.uuid.Uuid

@Composable
internal fun TagLinkFlexBox(
    onTagClick: (Uuid) -> Unit,
    onLinkClick: () -> Unit,
    modifier: Modifier = Modifier,
    uiStateProvider: () -> TagLinkInputUiState = { TagLinkInputUiState() },
) {
    val uiState = uiStateProvider()

    DiaryChipFlexBox(modifier = modifier) {
        uiState.linkedTagList.forEach { tag ->
            key(tag.id) {
                TagLinkChip(
                    tag = tag,
                    onClick = { onTagClick(tag.id) },
                    modifier = Modifier.animateBounds(lookaheadScope = this),
                )
            }
        }
        DiaryAddChip(
            onClick = onLinkClick,
            label = stringResource(Res.string.tag_link_label),
            actionLabel = stringResource(Res.string.tag_link_action),
            modifier = Modifier.animateBounds(lookaheadScope = this),
        )
    }
}

@ComponentPreview
@Composable
private fun TagLinkFlexBoxPreview() {
    val tagList = remember { listOf(previewTag(emoji = "💼", title = "업무", color = 0xFF3A7BD5)) }

    DiaryTheme {
        Surface {
            TagLinkFlexBox(
                onTagClick = {},
                onLinkClick = {},
                uiStateProvider = { TagLinkInputUiState(linkedTagList = tagList) },
            )
        }
    }
}
