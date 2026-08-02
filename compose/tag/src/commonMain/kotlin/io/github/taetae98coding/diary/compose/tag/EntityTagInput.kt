package io.github.taetae98coding.diary.compose.tag

import androidx.compose.material3.Card
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import io.github.taetae98coding.diary.compose.core.preview.ComponentPreview
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import kotlin.uuid.Uuid

@Composable
public fun EntityTagInput(
    onTagClick: (Uuid) -> Unit,
    onAddClick: () -> Unit,
    modifier: Modifier = Modifier,
    uiStateProvider: () -> EntityTagInputUiState = { EntityTagInputUiState() },
) {
    Card(modifier = modifier) {
        EntityTagFlexBox(
            uiStateProvider = uiStateProvider,
            onTagClick = onTagClick,
            onAddClick = onAddClick,
        )
    }
}

private class EntityTagInputUiStatePreviewParameter : PreviewParameterProvider<EntityTagInputUiState> {
    override val values: Sequence<EntityTagInputUiState>
        get() =
            sequenceOf(
                EntityTagInputUiState(),
                EntityTagInputUiState(
                    tagList =
                        listOf(
                            previewTag(emoji = "💼", title = "업무", color = 0xFF3A7BD5),
                            previewTag(emoji = "", title = "운동", color = 0xFFE57373),
                        ),
                ),
            )
}

@ComponentPreview
@Composable
private fun EntityTagInputPreview(
    @PreviewParameter(EntityTagInputUiStatePreviewParameter::class) uiState: EntityTagInputUiState,
) {
    DiaryTheme {
        Surface {
            EntityTagInput(
                uiStateProvider = { uiState },
                onTagClick = {},
                onAddClick = {},
            )
        }
    }
}
