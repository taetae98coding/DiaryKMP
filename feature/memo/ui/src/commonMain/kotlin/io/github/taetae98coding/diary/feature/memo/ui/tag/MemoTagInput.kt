package io.github.taetae98coding.diary.feature.memo.ui.tag

import androidx.compose.material3.Card
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import io.github.taetae98coding.diary.compose.core.preview.ComponentPreview
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.core.model.tag.Tag
import io.github.taetae98coding.diary.core.model.tag.TagDetail
import io.github.taetae98coding.diary.feature.memo.ui.previewTag
import kotlin.time.Instant
import kotlin.uuid.Uuid

@Composable
internal fun MemoTagInput(
    onTagClick: (Uuid) -> Unit,
    onAddClick: () -> Unit,
    modifier: Modifier = Modifier,
    uiStateProvider: () -> MemoTagInputUiState = { MemoTagInputUiState() },
) {
    Card(modifier = modifier) {
        MemoTagFlexBox(
            uiStateProvider = uiStateProvider,
            onTagClick = onTagClick,
            onAddClick = onAddClick,
        )
    }
}

private class MemoTagInputUiStatePreviewParameter : PreviewParameterProvider<MemoTagInputUiState> {
    override val values: Sequence<MemoTagInputUiState>
        get() {
            val tagList =
                listOf(
                    previewTag(emoji = "💼", title = "업무", color = 0xFF3A7BD5),
                    previewTag(emoji = "", title = "운동", color = 0xFFE57373),
                )

            return sequenceOf(
                MemoTagInputUiState(),
                MemoTagInputUiState(selectedTagList = tagList, primaryTagId = tagList.first().id),
            )
        }
}

@ComponentPreview
@Composable
private fun MemoTagInputPreview(
    @PreviewParameter(MemoTagInputUiStatePreviewParameter::class) uiState: MemoTagInputUiState,
) {
    DiaryTheme {
        Surface {
            MemoTagInput(
                uiStateProvider = { uiState },
                onTagClick = {},
                onAddClick = {},
            )
        }
    }
}
