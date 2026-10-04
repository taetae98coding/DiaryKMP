package io.github.taetae98coding.diary.feature.web.ui.detail

import androidx.compose.foundation.layout.Box
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.taetae98coding.diary.compose.core.animation.DiaryScaleVisibility
import io.github.taetae98coding.diary.compose.core.button.FloatingCheckButton
import io.github.taetae98coding.diary.compose.core.preview.ComponentPreview
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.feature.web.ui.Res
import io.github.taetae98coding.diary.feature.web.ui.detail.memo.WebDetailMemoFloatingActionButton
import io.github.taetae98coding.diary.feature.web.ui.detail.tab.WebDetailTab
import io.github.taetae98coding.diary.feature.web.ui.web_detail_update_button_content_description
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun WebDetailTabFloatingActionButton(
    onEvent: (WebDetailScaffoldEvent) -> Unit,
    modifier: Modifier = Modifier,
    tabProvider: () -> WebDetailTab = { WebDetailTab.FORM },
    isChangedProvider: () -> Boolean = { false },
    uiStateProvider: () -> WebDetailUiState = { WebDetailUiState.Loading },
) {
    val tab = tabProvider()

    Box(modifier = modifier) {
        DiaryScaleVisibility(visible = tab == WebDetailTab.FORM && isChangedProvider()) {
            FloatingCheckButton(
                onClick = { onEvent(WebDetailScaffoldEvent.ClickUpdate) },
                contentDescription = stringResource(Res.string.web_detail_update_button_content_description),
                isInProgressProvider = { (uiStateProvider() as? WebDetailUiState.Content)?.isUpdateInProgress == true },
            )
        }

        DiaryScaleVisibility(visible = tab == WebDetailTab.MEMO) {
            WebDetailMemoFloatingActionButton(onClick = { onEvent(WebDetailScaffoldEvent.ClickMemoAdd) })
        }
    }
}

@ComponentPreview
@Composable
private fun WebDetailTabFloatingActionButtonPreview() {
    DiaryTheme {
        Surface {
            WebDetailTabFloatingActionButton(
                onEvent = {},
                isChangedProvider = { true },
            )
        }
    }
}
