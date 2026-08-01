package io.github.taetae98coding.diary.feature.memo.ui.web

import androidx.compose.material3.Card
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import io.github.taetae98coding.diary.compose.core.preview.ComponentPreview
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.feature.memo.ui.previewWeb
import kotlin.uuid.Uuid

@Composable
internal fun MemoWebInput(
    onWebClick: (Uuid) -> Unit,
    onAddClick: () -> Unit,
    modifier: Modifier = Modifier,
    uiStateProvider: () -> MemoWebInputUiState = { MemoWebInputUiState() },
) {
    Card(modifier = modifier) {
        MemoWebFlexBox(
            uiStateProvider = uiStateProvider,
            onWebClick = onWebClick,
            onAddClick = onAddClick,
        )
    }
}

private class MemoWebInputUiStatePreviewParameter : PreviewParameterProvider<MemoWebInputUiState> {
    override val values: Sequence<MemoWebInputUiState>
        get() =
            sequenceOf(
                MemoWebInputUiState(),
                MemoWebInputUiState(
                    selectedWebList =
                        listOf(
                            previewWeb(title = "사내 위키", url = "https://wiki.example.com"),
                            previewWeb(title = "안드로이드 문서", url = "https://developer.android.com"),
                        ),
                ),
            )
}

@ComponentPreview
@Composable
private fun MemoWebInputPreview(
    @PreviewParameter(MemoWebInputUiStatePreviewParameter::class) uiState: MemoWebInputUiState,
) {
    DiaryTheme {
        Surface {
            MemoWebInput(
                uiStateProvider = { uiState },
                onWebClick = {},
                onAddClick = {},
            )
        }
    }
}
