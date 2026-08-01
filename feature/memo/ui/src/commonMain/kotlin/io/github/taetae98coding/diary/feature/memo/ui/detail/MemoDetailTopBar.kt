package io.github.taetae98coding.diary.feature.memo.ui.detail

import androidx.compose.foundation.basicMarquee
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.taetae98coding.diary.compose.core.button.NavigateUpButton
import io.github.taetae98coding.diary.compose.core.preview.ComponentPreview
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.core.model.memo.MemoDetail
import io.github.taetae98coding.diary.feature.memo.ui.Res
import io.github.taetae98coding.diary.feature.memo.ui.memo_detail_navigate_up_button_content_description
import org.jetbrains.compose.resources.stringResource
import kotlin.uuid.Uuid

@Composable
internal fun MemoDetailTopBar(
    onEvent: (MemoDetailScaffoldEvent) -> Unit,
    modifier: Modifier = Modifier,
    uiStateProvider: () -> MemoDetailUiState = { MemoDetailUiState.Loading },
    componentVisibleProvider: () -> MemoDetailScaffoldComponentVisible = { MemoDetailScaffoldComponentVisible() },
) {
    val uiState = uiStateProvider()

    TopAppBar(
        title = {
            if (uiState is MemoDetailUiState.Content) {
                Text(
                    text = uiState.detail.title,
                    modifier = Modifier.basicMarquee(iterations = Int.MAX_VALUE),
                    maxLines = 1,
                )
            }
        },
        modifier = modifier,
        navigationIcon = {
            if (componentVisibleProvider().isNavigateUpButtonVisible) {
                NavigateUpButton(
                    onClick = { onEvent(MemoDetailScaffoldEvent.ClickNavigateUp) },
                    contentDescription = stringResource(Res.string.memo_detail_navigate_up_button_content_description),
                )
            }
        },
        actions = {
            if (uiState is MemoDetailUiState.Content) {
                MemoDetailTopBarActions(
                    contentProvider = { uiState },
                    onEvent = onEvent,
                )
            }
        },
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
