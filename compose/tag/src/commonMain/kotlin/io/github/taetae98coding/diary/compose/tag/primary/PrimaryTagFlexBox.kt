package io.github.taetae98coding.diary.compose.tag.primary

import androidx.compose.animation.animateBounds
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import io.github.taetae98coding.diary.compose.core.chip.DiaryAddChip
import io.github.taetae98coding.diary.compose.core.layout.DiaryChipFlexBox
import io.github.taetae98coding.diary.compose.tag.Res
import io.github.taetae98coding.diary.compose.tag.previewTag
import io.github.taetae98coding.diary.compose.tag.primary_tag_select_action
import io.github.taetae98coding.diary.compose.tag.primary_tag_select_label
import io.github.taetae98coding.diary.compose.theme.DiaryTheme
import io.github.taetae98coding.diary.compose.theme.preview.ComponentPreview
import org.jetbrains.compose.resources.stringResource
import kotlin.uuid.Uuid

@Composable
internal fun PrimaryTagFlexBox(
    onTagClick: (Uuid) -> Unit,
    onAddClick: () -> Unit,
    modifier: Modifier = Modifier,
    uiStateProvider: () -> PrimaryTagInputUiState = { PrimaryTagInputUiState() },
) {
    val uiState = uiStateProvider()

    DiaryChipFlexBox(modifier = modifier) {
        uiState.selectedTagList.forEach { tag ->
            key(tag.id) {
                PrimaryTagChip(
                    tag = tag,
                    isPrimary = tag.id == uiState.primaryTagId,
                    onClick = { onTagClick(tag.id) },
                    modifier = Modifier.animateBounds(lookaheadScope = this),
                )
            }
        }
        DiaryAddChip(
            onClick = onAddClick,
            label = stringResource(Res.string.primary_tag_select_label),
            actionLabel = stringResource(Res.string.primary_tag_select_action),
            modifier = Modifier.animateBounds(lookaheadScope = this),
        )
    }
}

@ComponentPreview
@Composable
private fun PrimaryTagFlexBoxPreview() {
    val tagList = remember { listOf(previewTag(emoji = "💼", title = "업무", color = 0xFF3A7BD5)) }

    DiaryTheme {
        Surface {
            PrimaryTagFlexBox(
                onTagClick = {},
                onAddClick = {},
                uiStateProvider = { PrimaryTagInputUiState(selectedTagList = tagList, primaryTagId = tagList.first().id) },
            )
        }
    }
}
