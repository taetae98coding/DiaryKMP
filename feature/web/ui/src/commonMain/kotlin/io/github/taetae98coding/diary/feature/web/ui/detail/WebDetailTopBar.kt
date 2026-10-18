package io.github.taetae98coding.diary.feature.web.ui.detail

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.taetae98coding.diary.compose.core.appbar.DiaryTopBar
import io.github.taetae98coding.diary.compose.core.appbar.DiaryTopBarAction
import io.github.taetae98coding.diary.compose.core.appbar.DiaryTopBarIcon
import io.github.taetae98coding.diary.compose.core.appbar.DiaryTopBarNavigateUp
import io.github.taetae98coding.diary.compose.core.appbar.DiaryTopBarTitle
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
    val uiState = uiStateProvider()
    val navigateUpContentDescription = stringResource(Res.string.web_navigate_up_button_content_description)
    val openInNewContentDescription = stringResource(Res.string.web_detail_open_in_new_button_content_description)
    val deleteContentDescription = stringResource(Res.string.web_detail_delete_button_content_description)

    DiaryTopBar(
        modifier = modifier,
        title =
            if (uiState is WebDetailUiState.Content) {
                DiaryTopBarTitle.Text(text = uiState.detail.title, isMarquee = true)
            } else {
                null
            },
        navigateUp =
            DiaryTopBarNavigateUp(
                contentDescription = navigateUpContentDescription,
                onClick = { onEvent(WebDetailScaffoldEvent.ClickNavigateUp) },
            ),
        actions =
            buildList {
                if (uiState is WebDetailUiState.Content) {
                    add(
                        DiaryTopBarAction.Icon(
                            icon = DiaryTopBarIcon.OPEN_IN_NEW,
                            contentDescription = openInNewContentDescription,
                            onClick = { onEvent(WebDetailScaffoldEvent.ClickOpenInNew) },
                        ),
                    )
                    add(
                        DiaryTopBarAction.Icon(
                            icon = DiaryTopBarIcon.DELETE,
                            contentDescription = deleteContentDescription,
                            onClick = { onEvent(WebDetailScaffoldEvent.ClickDelete) },
                            isInProgress = uiState.isDeleteInProgress,
                        ),
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
