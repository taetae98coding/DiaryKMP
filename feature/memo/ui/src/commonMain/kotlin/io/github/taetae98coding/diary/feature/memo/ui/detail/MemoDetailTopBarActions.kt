package io.github.taetae98coding.diary.feature.memo.ui.detail

import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import io.github.taetae98coding.diary.compose.core.button.CopyButton
import io.github.taetae98coding.diary.compose.core.button.DeleteButton
import io.github.taetae98coding.diary.compose.core.button.FinishButton
import io.github.taetae98coding.diary.compose.core.preview.ComponentPreview
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.core.model.memo.MemoDetail
import io.github.taetae98coding.diary.feature.memo.ui.Res
import io.github.taetae98coding.diary.feature.memo.ui.gemini.MemoGeminiButtonHost
import io.github.taetae98coding.diary.feature.memo.ui.gemini.MemoGeminiUiState
import io.github.taetae98coding.diary.feature.memo.ui.memo_detail_copy_button_content_description
import io.github.taetae98coding.diary.feature.memo.ui.memo_detail_delete_button_content_description
import io.github.taetae98coding.diary.feature.memo.ui.memo_detail_finish_button_content_description
import io.github.taetae98coding.diary.feature.memo.ui.memo_detail_restart_button_content_description
import org.jetbrains.compose.resources.stringResource
import kotlin.uuid.Uuid

@Composable
internal fun MemoDetailTopBarActions(
    contentProvider: () -> MemoDetailUiState.Content,
    onEvent: (MemoDetailScaffoldEvent) -> Unit,
    geminiUiStateProvider: () -> MemoGeminiUiState = { MemoGeminiUiState() },
) {
    MemoGeminiButtonHost(
        onClick = { onEvent(MemoDetailScaffoldEvent.ClickGemini) },
        isVisibleProvider = { geminiUiStateProvider().isButtonVisible },
    )
    FinishButton(
        isFinishedProvider = { contentProvider().isFinished },
        onClick = {
            if (contentProvider().isFinished) {
                onEvent(MemoDetailScaffoldEvent.ClickRestart)
            } else {
                onEvent(MemoDetailScaffoldEvent.ClickFinish)
            }
        },
        contentDescription =
            if (contentProvider().isFinished) {
                stringResource(Res.string.memo_detail_restart_button_content_description)
            } else {
                stringResource(Res.string.memo_detail_finish_button_content_description)
            },
        isInProgressProvider = { contentProvider().isFinishInProgress },
    )
    CopyButton(
        onClick = { onEvent(MemoDetailScaffoldEvent.ClickCopy) },
        contentDescription = stringResource(Res.string.memo_detail_copy_button_content_description),
        isInProgressProvider = { contentProvider().isCopyInProgress },
    )
    DeleteButton(
        onClick = { onEvent(MemoDetailScaffoldEvent.ClickDelete) },
        contentDescription = stringResource(Res.string.memo_detail_delete_button_content_description),
        isInProgressProvider = { contentProvider().isDeleteInProgress },
    )
}

@ComponentPreview
@Composable
private fun MemoDetailTopBarActionsPreview() {
    DiaryTheme {
        Surface {
            Row {
                MemoDetailTopBarActions(
                    contentProvider = {
                        MemoDetailUiState.Content(
                            id = Uuid.NIL,
                            detail = MemoDetail.EMPTY.copy(title = "메모 제목"),
                            isFinished = false,
                        )
                    },
                    onEvent = {},
                )
            }
        }
    }
}
