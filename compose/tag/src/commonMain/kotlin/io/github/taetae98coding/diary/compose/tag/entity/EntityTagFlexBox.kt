package io.github.taetae98coding.diary.compose.tag.entity

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
import io.github.taetae98coding.diary.compose.tag.Res
import io.github.taetae98coding.diary.compose.tag.entity_tag_action
import io.github.taetae98coding.diary.compose.tag.entity_tag_label
import io.github.taetae98coding.diary.compose.tag.previewTag
import org.jetbrains.compose.resources.stringResource
import kotlin.uuid.Uuid

@Composable
internal fun EntityTagFlexBox(
    onTagClick: (Uuid) -> Unit,
    onAddClick: () -> Unit,
    modifier: Modifier = Modifier,
    uiStateProvider: () -> EntityTagInputUiState = { EntityTagInputUiState() },
) {
    val uiState = uiStateProvider()

    DiaryChipFlexBox(modifier = modifier) {
        uiState.tagList.forEach { tag ->
            key(tag.id) {
                EntityTagChip(
                    tag = tag,
                    onClick = { onTagClick(tag.id) },
                    modifier = Modifier.animateBounds(lookaheadScope = this),
                )
            }
        }
        DiaryAddChip(
            onClick = onAddClick,
            label = stringResource(Res.string.entity_tag_label),
            actionLabel = stringResource(Res.string.entity_tag_action),
            modifier = Modifier.animateBounds(lookaheadScope = this),
        )
    }
}

@ComponentPreview
@Composable
private fun EntityTagFlexBoxPreview() {
    val tagList = remember { listOf(previewTag(emoji = "💼", title = "업무", color = 0xFF3A7BD5)) }

    DiaryTheme {
        Surface {
            EntityTagFlexBox(
                onTagClick = {},
                onAddClick = {},
                uiStateProvider = { EntityTagInputUiState(tagList = tagList) },
            )
        }
    }
}
