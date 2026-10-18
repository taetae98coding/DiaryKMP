package io.github.taetae98coding.diary.feature.memo.ui.add

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.taetae98coding.diary.compose.core.appbar.DiaryTopBar
import io.github.taetae98coding.diary.compose.core.appbar.DiaryTopBarAction
import io.github.taetae98coding.diary.compose.core.appbar.DiaryTopBarIcon
import io.github.taetae98coding.diary.compose.core.appbar.DiaryTopBarNavigateUp
import io.github.taetae98coding.diary.compose.core.appbar.DiaryTopBarTitle
import io.github.taetae98coding.diary.compose.core.preview.ComponentPreview
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.feature.memo.ui.Res
import io.github.taetae98coding.diary.feature.memo.ui.gemini.MemoGeminiUiState
import io.github.taetae98coding.diary.feature.memo.ui.memo_add_navigate_up_button_content_description
import io.github.taetae98coding.diary.feature.memo.ui.memo_add_title
import io.github.taetae98coding.diary.feature.memo.ui.memo_gemini_button_content_description
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun MemoAddTopBar(
    onEvent: (MemoAddScaffoldEvent) -> Unit,
    modifier: Modifier = Modifier,
    componentVisibleProvider: () -> MemoAddScaffoldComponentVisible = { MemoAddScaffoldComponentVisible() },
    geminiUiStateProvider: () -> MemoGeminiUiState = { MemoGeminiUiState() },
) {
    val title = stringResource(Res.string.memo_add_title)
    val navigateUpContentDescription = stringResource(Res.string.memo_add_navigate_up_button_content_description)
    val geminiContentDescription = stringResource(Res.string.memo_gemini_button_content_description)

    DiaryTopBar(
        modifier = modifier,
        title = DiaryTopBarTitle.Text(text = title),
        navigateUp =
            if (componentVisibleProvider().isNavigateUpButtonVisible) {
                DiaryTopBarNavigateUp(
                    contentDescription = navigateUpContentDescription,
                    onClick = { onEvent(MemoAddScaffoldEvent.ClickNavigateUp) },
                )
            } else {
                null
            },
        actions =
            buildList {
                if (geminiUiStateProvider().isButtonVisible) {
                    add(
                        DiaryTopBarAction.Icon(
                            icon = DiaryTopBarIcon.AUTO_AWESOME,
                            contentDescription = geminiContentDescription,
                            onClick = { onEvent(MemoAddScaffoldEvent.ClickGemini) },
                        ),
                    )
                }
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
