package io.github.taetae98coding.diary.compose.tag.primary

import androidx.compose.material3.Card
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import io.github.taetae98coding.diary.compose.tag.previewTag
import io.github.taetae98coding.diary.compose.theme.DiaryTheme
import io.github.taetae98coding.diary.compose.theme.preview.ComponentPreview
import kotlin.uuid.Uuid

@Composable
public fun PrimaryTagInput(
    onTagClick: (Uuid) -> Unit,
    onAddClick: () -> Unit,
    modifier: Modifier = Modifier,
    uiStateProvider: () -> PrimaryTagInputUiState = { PrimaryTagInputUiState() },
) {
    Card(modifier = modifier) {
        PrimaryTagFlexBox(
            uiStateProvider = uiStateProvider,
            onTagClick = onTagClick,
            onAddClick = onAddClick,
        )
    }
}

private class PrimaryTagInputUiStatePreviewParameter : PreviewParameterProvider<PrimaryTagInputUiState> {
    override val values: Sequence<PrimaryTagInputUiState>
        get() {
            val tagList =
                listOf(
                    previewTag(emoji = "💼", title = "업무", color = 0xFF3A7BD5),
                    previewTag(emoji = "", title = "운동", color = 0xFFE57373),
                )

            return sequenceOf(
                PrimaryTagInputUiState(),
                PrimaryTagInputUiState(selectedTagList = tagList, primaryTagId = tagList.first().id),
            )
        }
}

@ComponentPreview
@Composable
private fun PrimaryTagInputPreview(
    @PreviewParameter(PrimaryTagInputUiStatePreviewParameter::class) uiState: PrimaryTagInputUiState,
) {
    DiaryTheme {
        Surface {
            PrimaryTagInput(
                uiStateProvider = { uiState },
                onTagClick = {},
                onAddClick = {},
            )
        }
    }
}
