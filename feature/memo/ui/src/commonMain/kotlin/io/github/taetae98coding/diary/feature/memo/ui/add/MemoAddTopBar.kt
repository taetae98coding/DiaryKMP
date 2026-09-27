package io.github.taetae98coding.diary.feature.memo.ui.add

import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.taetae98coding.diary.compose.core.button.NavigateUpButton
import io.github.taetae98coding.diary.compose.core.preview.ComponentPreview
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.feature.memo.ui.Res
import io.github.taetae98coding.diary.feature.memo.ui.gemini.MemoGeminiButtonHost
import io.github.taetae98coding.diary.feature.memo.ui.gemini.MemoGeminiUiState
import io.github.taetae98coding.diary.feature.memo.ui.memo_add_navigate_up_button_content_description
import io.github.taetae98coding.diary.feature.memo.ui.memo_add_title
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun MemoAddTopBar(
    onEvent: (MemoAddScaffoldEvent) -> Unit,
    modifier: Modifier = Modifier,
    componentVisibleProvider: () -> MemoAddScaffoldComponentVisible = { MemoAddScaffoldComponentVisible() },
    geminiUiStateProvider: () -> MemoGeminiUiState = { MemoGeminiUiState() },
) {
    TopAppBar(
        title = { Text(text = stringResource(Res.string.memo_add_title)) },
        modifier = modifier,
        navigationIcon = {
            if (componentVisibleProvider().isNavigateUpButtonVisible) {
                NavigateUpButton(
                    onClick = { onEvent(MemoAddScaffoldEvent.ClickNavigateUp) },
                    contentDescription = stringResource(Res.string.memo_add_navigate_up_button_content_description),
                )
            }
        },
        actions = {
            MemoGeminiButtonHost(
                onClick = { onEvent(MemoAddScaffoldEvent.ClickGemini) },
                isVisibleProvider = { geminiUiStateProvider().isButtonVisible },
            )
        },
    )
}

@ComponentPreview
@Composable
private fun MemoAddTopBarPreview() {
    DiaryTheme {
        MemoAddTopBar(onEvent = {})
    }
}
