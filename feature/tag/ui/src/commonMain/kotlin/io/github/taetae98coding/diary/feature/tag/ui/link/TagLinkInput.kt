package io.github.taetae98coding.diary.feature.tag.ui.link

import androidx.compose.material3.Card
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import io.github.taetae98coding.diary.compose.core.preview.ComponentPreview
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.feature.tag.ui.previewTag
import kotlin.uuid.Uuid

@Composable
internal fun TagLinkInput(
    onTagClick: (Uuid) -> Unit,
    onLinkClick: () -> Unit,
    modifier: Modifier = Modifier,
    uiStateProvider: () -> TagLinkInputUiState = { TagLinkInputUiState() },
) {
    Card(modifier = modifier) {
        TagLinkFlexBox(
            uiStateProvider = uiStateProvider,
            onTagClick = onTagClick,
            onLinkClick = onLinkClick,
        )
    }
}

private class TagLinkInputUiStatePreviewParameter : PreviewParameterProvider<TagLinkInputUiState> {
    override val values: Sequence<TagLinkInputUiState>
        get() =
            sequenceOf(
                TagLinkInputUiState(),
                TagLinkInputUiState(
                    linkedTagList =
                        listOf(
                            previewTag(emoji = "💼", title = "업무", color = 0xFF3A7BD5),
                            previewTag(emoji = "", title = "운동", color = 0xFFE57373),
                        ),
                ),
            )
}

@ComponentPreview
@Composable
private fun TagLinkInputPreview(
    @PreviewParameter(TagLinkInputUiStatePreviewParameter::class) uiState: TagLinkInputUiState,
) {
    DiaryTheme {
        Surface {
            TagLinkInput(
                uiStateProvider = { uiState },
                onTagClick = {},
                onLinkClick = {},
            )
        }
    }
}
