package io.github.taetae98coding.diary.feature.memo.ui.detail

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.taetae98coding.diary.compose.core.appbar.DiaryTopBar
import io.github.taetae98coding.diary.compose.core.appbar.DiaryTopBarActionScope
import io.github.taetae98coding.diary.compose.core.appbar.DiaryTopBarIcon
import io.github.taetae98coding.diary.compose.core.preview.ComponentPreview
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.core.model.memo.MemoDetail
import io.github.taetae98coding.diary.feature.memo.ui.Res
import io.github.taetae98coding.diary.feature.memo.ui.gemini.MemoGeminiUiState
import io.github.taetae98coding.diary.feature.memo.ui.memo_detail_copy_button_content_description
import io.github.taetae98coding.diary.feature.memo.ui.memo_detail_delete_button_content_description
import io.github.taetae98coding.diary.feature.memo.ui.memo_detail_finish_button_content_description
import io.github.taetae98coding.diary.feature.memo.ui.memo_detail_navigate_up_button_content_description
import io.github.taetae98coding.diary.feature.memo.ui.memo_detail_restart_button_content_description
import io.github.taetae98coding.diary.feature.memo.ui.memo_gemini_button_content_description
import org.jetbrains.compose.resources.stringResource
import kotlin.uuid.Uuid

@Composable
internal fun MemoDetailTopBar(
    onEvent: (MemoDetailScaffoldEvent) -> Unit,
    modifier: Modifier = Modifier,
    uiStateProvider: () -> MemoDetailUiState = { MemoDetailUiState.Loading },
    componentVisibleProvider: () -> MemoDetailScaffoldComponentVisible = { MemoDetailScaffoldComponentVisible() },
    geminiUiStateProvider: () -> MemoGeminiUiState = { MemoGeminiUiState() },
) {
    val uiState = uiStateProvider()
    val navigateUpContentDescription = stringResource(Res.string.memo_detail_navigate_up_button_content_description)
    val geminiContentDescription = stringResource(Res.string.memo_gemini_button_content_description)
    val copyContentDescription = stringResource(Res.string.memo_detail_copy_button_content_description)
    val deleteContentDescription = stringResource(Res.string.memo_detail_delete_button_content_description)

    DiaryTopBar(
        title = {
            if (uiState is MemoDetailUiState.Content) {
                Title(text = uiState.detail.title, isMarquee = true)
            }
        },
        modifier = modifier,
        navigationIcon = {
            if (componentVisibleProvider().isNavigateUpButtonVisible) {
                NavigateUp(
                    onClick = { onEvent(MemoDetailScaffoldEvent.ClickNavigateUp) },
                    contentDescription = navigateUpContentDescription,
                )
            }
        },
        actions = {
            if (uiState is MemoDetailUiState.Content) {
                if (geminiUiStateProvider().isButtonVisible) {
                    IconAction(
                        icon = DiaryTopBarIcon.AUTO_AWESOME,
                        contentDescription = geminiContentDescription,
                        onClick = { onEvent(MemoDetailScaffoldEvent.ClickGemini) },
                    )
                }
                MemoDetailFinishAction(content = uiState, onEvent = onEvent)
                IconAction(
                    icon = DiaryTopBarIcon.COPY,
                    contentDescription = copyContentDescription,
                    onClick = { onEvent(MemoDetailScaffoldEvent.ClickCopy) },
                    isInProgress = uiState.isCopyInProgress,
                )
                IconAction(
                    icon = DiaryTopBarIcon.DELETE,
                    contentDescription = deleteContentDescription,
                    onClick = { onEvent(MemoDetailScaffoldEvent.ClickDelete) },
                    isInProgress = uiState.isDeleteInProgress,
                )
            }
        },
    )
}

@Composable
private fun DiaryTopBarActionScope.MemoDetailFinishAction(
    content: MemoDetailUiState.Content,
    onEvent: (MemoDetailScaffoldEvent) -> Unit,
) {
    ToggleAction(
        icon = DiaryTopBarIcon.FINISH,
        contentDescription =
            if (content.isFinished) {
                stringResource(Res.string.memo_detail_restart_button_content_description)
            } else {
                stringResource(Res.string.memo_detail_finish_button_content_description)
            },
        isChecked = content.isFinished,
        onClick = {
            if (content.isFinished) {
                onEvent(MemoDetailScaffoldEvent.ClickRestart)
            } else {
                onEvent(MemoDetailScaffoldEvent.ClickFinish)
            }
        },
        isInProgress = content.isFinishInProgress,
    )
}

@ComponentPreview
@Composable
private fun MemoDetailTopBarPreview() {
    DiaryTheme {
        MemoDetailTopBar(
            onEvent = {},
            uiStateProvider = {
                MemoDetailUiState.Content(
                    id = Uuid.NIL,
                    detail = MemoDetail.EMPTY.copy(title = "메모 제목"),
                    isFinished = false,
                )
            },
        )
    }
}
