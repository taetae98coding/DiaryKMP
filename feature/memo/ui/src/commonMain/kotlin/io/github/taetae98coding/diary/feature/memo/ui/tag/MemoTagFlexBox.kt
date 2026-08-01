package io.github.taetae98coding.diary.feature.memo.ui.tag

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
import io.github.taetae98coding.diary.feature.memo.ui.Res
import io.github.taetae98coding.diary.feature.memo.ui.memo_tag_select_action
import io.github.taetae98coding.diary.feature.memo.ui.memo_tag_select_label
import io.github.taetae98coding.diary.feature.memo.ui.previewTag
import org.jetbrains.compose.resources.stringResource
import kotlin.uuid.Uuid

@Composable
internal fun MemoTagFlexBox(
    onTagClick: (Uuid) -> Unit,
    onAddClick: () -> Unit,
    modifier: Modifier = Modifier,
    uiStateProvider: () -> MemoTagInputUiState = { MemoTagInputUiState() },
) {
    val uiState = uiStateProvider()

    DiaryChipFlexBox(modifier = modifier) {
        uiState.selectedTagList.forEach { tag ->
            key(tag.id) {
                MemoTagChip(
                    tag = tag,
                    isPrimary = tag.id == uiState.primaryTagId,
                    onClick = { onTagClick(tag.id) },
                    modifier = Modifier.animateBounds(lookaheadScope = this),
                )
            }
        }
        DiaryAddChip(
            onClick = onAddClick,
            label = stringResource(Res.string.memo_tag_select_label),
            actionLabel = stringResource(Res.string.memo_tag_select_action),
            modifier = Modifier.animateBounds(lookaheadScope = this),
        )
    }
}

@ComponentPreview
@Composable
private fun MemoTagFlexBoxPreview() {
    val tagList = remember { listOf(previewTag(emoji = "💼", title = "업무", color = 0xFF3A7BD5)) }

    DiaryTheme {
        Surface {
            MemoTagFlexBox(
                onTagClick = {},
                onAddClick = {},
                uiStateProvider = { MemoTagInputUiState(selectedTagList = tagList, primaryTagId = tagList.first().id) },
            )
        }
    }
}
