package io.github.taetae98coding.diary.feature.web.ui.detail

import androidx.compose.foundation.basicMarquee
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.taetae98coding.diary.compose.core.button.DeleteButton
import io.github.taetae98coding.diary.compose.core.button.NavigateUpButton
import io.github.taetae98coding.diary.compose.core.button.OpenInNewButton
import io.github.taetae98coding.diary.compose.core.preview.ComponentPreview
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.feature.web.ui.Res
import io.github.taetae98coding.diary.feature.web.ui.previewWebDetail
import io.github.taetae98coding.diary.feature.web.ui.web_detail_delete_button_content_description
import io.github.taetae98coding.diary.feature.web.ui.web_detail_open_in_new_button_content_description
import io.github.taetae98coding.diary.feature.web.ui.web_navigate_up_button_content_description
import org.jetbrains.compose.resources.stringResource
import kotlin.uuid.Uuid

@Composable
internal fun WebDetailTopBar(
    onEvent: (WebDetailScaffoldEvent) -> Unit,
    modifier: Modifier = Modifier,
    uiStateProvider: () -> WebDetailUiState = { WebDetailUiState.Loading },
) {
    TopAppBar(
        // 제목과 액션은 서로 다른 자리이므로 각 슬롯에서 상태를 읽어, 한쪽이 바뀔 때 다른 쪽까지 다시 그리지 않는다.
        title = {
            val uiState = uiStateProvider()

            if (uiState is WebDetailUiState.Content) {
                Text(
                    text = uiState.detail.title,
                    modifier = Modifier.basicMarquee(iterations = Int.MAX_VALUE),
                    maxLines = 1,
                )
            }
        },
        modifier = modifier,
        navigationIcon = {
            NavigateUpButton(
                onClick = { onEvent(WebDetailScaffoldEvent.ClickNavigateUp) },
                contentDescription = stringResource(Res.string.web_navigate_up_button_content_description),
            )
        },
        actions = {
            val uiState = uiStateProvider()

            if (uiState is WebDetailUiState.Content) {
                OpenInNewButton(
                    onClick = { onEvent(WebDetailScaffoldEvent.ClickOpenInNew) },
                    contentDescription = stringResource(Res.string.web_detail_open_in_new_button_content_description),
                )
                DeleteButton(
                    onClick = { onEvent(WebDetailScaffoldEvent.ClickDelete) },
                    contentDescription = stringResource(Res.string.web_detail_delete_button_content_description),
                    isInProgressProvider = { uiState.isDeleteInProgress },
                )
            }
        },
    )
}

@ComponentPreview
@Composable
private fun WebDetailTopBarPreview() {
    DiaryTheme {
        WebDetailTopBar(
            onEvent = {},
            uiStateProvider = { WebDetailUiState.Content(id = Uuid.NIL, detail = previewWebDetail()) },
        )
    }
}
