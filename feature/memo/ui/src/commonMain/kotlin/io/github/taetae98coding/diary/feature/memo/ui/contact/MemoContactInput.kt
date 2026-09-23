package io.github.taetae98coding.diary.feature.memo.ui.contact

import androidx.compose.material3.Card
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import io.github.taetae98coding.diary.compose.core.preview.ComponentPreview
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.feature.memo.ui.previewContact
import kotlin.uuid.Uuid

@Composable
internal fun MemoContactInput(
    onContactClick: (Uuid) -> Unit,
    onAddClick: () -> Unit,
    modifier: Modifier = Modifier,
    uiStateProvider: () -> MemoContactInputUiState = { MemoContactInputUiState() },
) {
    Card(modifier = modifier) {
        MemoContactFlexBox(
            uiStateProvider = uiStateProvider,
            onContactClick = onContactClick,
            onAddClick = onAddClick,
        )
    }
}

private class MemoContactInputUiStatePreviewParameter : PreviewParameterProvider<MemoContactInputUiState> {
    override val values: Sequence<MemoContactInputUiState>
        get() =
            sequenceOf(
                MemoContactInputUiState(),
                MemoContactInputUiState(
                    selectedContactList =
                        listOf(
                            previewContact(name = "김철수", phoneNumber = "010-1234-5678"),
                            previewContact(name = "이영희", phoneNumber = "010-9876-5432"),
                        ),
                ),
            )
}

@ComponentPreview
@Composable
private fun MemoContactInputPreview(
    @PreviewParameter(MemoContactInputUiStatePreviewParameter::class) uiState: MemoContactInputUiState,
) {
    DiaryTheme {
        Surface {
            MemoContactInput(
                uiStateProvider = { uiState },
                onContactClick = {},
                onAddClick = {},
            )
        }
    }
}
