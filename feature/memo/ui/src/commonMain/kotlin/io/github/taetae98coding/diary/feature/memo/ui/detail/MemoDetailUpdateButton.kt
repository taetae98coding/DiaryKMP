package io.github.taetae98coding.diary.feature.memo.ui.detail

import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import io.github.taetae98coding.diary.compose.core.animation.DiaryScaleVisibility
import io.github.taetae98coding.diary.compose.core.button.FloatingCheckButton
import io.github.taetae98coding.diary.compose.core.preview.ComponentPreview
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.feature.memo.ui.Res
import io.github.taetae98coding.diary.feature.memo.ui.memo_detail_update_button_content_description
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun MemoDetailUpdateButton(
    onEvent: (MemoDetailScaffoldEvent) -> Unit,
    uiStateProvider: () -> MemoDetailUiState = { MemoDetailUiState.Loading },
    isVisibleProvider: () -> Boolean = { false },
) {
    DiaryScaleVisibility(visible = isVisibleProvider()) {
        FloatingCheckButton(
            onClick = { onEvent(MemoDetailScaffoldEvent.ClickUpdate) },
            contentDescription = stringResource(Res.string.memo_detail_update_button_content_description),
            isInProgressProvider = {
                (uiStateProvider() as? MemoDetailUiState.Content)?.isInProgress == true
            },
        )
    }
}

@ComponentPreview
@Composable
private fun MemoDetailUpdateButtonPreview() {
    DiaryTheme {
        Surface {
            MemoDetailUpdateButton(
                onEvent = {},
                isVisibleProvider = { true },
            )
        }
    }
}
