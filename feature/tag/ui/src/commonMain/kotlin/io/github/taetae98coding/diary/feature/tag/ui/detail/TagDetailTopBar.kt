package io.github.taetae98coding.diary.feature.tag.ui.detail

import androidx.compose.foundation.basicMarquee
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.taetae98coding.diary.compose.core.button.NavigateUpButton
import io.github.taetae98coding.diary.compose.core.preview.ComponentPreview
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.core.model.tag.TagDetail
import io.github.taetae98coding.diary.feature.tag.ui.Res
import io.github.taetae98coding.diary.feature.tag.ui.tag_detail_navigate_up_button_content_description
import org.jetbrains.compose.resources.stringResource
import kotlin.uuid.Uuid

@Composable
internal fun TagDetailTopBar(
    onEvent: (TagDetailScaffoldEvent) -> Unit,
    modifier: Modifier = Modifier,
    uiStateProvider: () -> TagDetailUiState = { TagDetailUiState.Loading },
    componentVisibleProvider: () -> TagDetailScaffoldComponentVisible = { TagDetailScaffoldComponentVisible() },
    isScopeAppliedProvider: () -> Boolean = { false },
) {
    val uiState = uiStateProvider()

    TopAppBar(
        title = {
            if (uiState is TagDetailUiState.Content) {
                Text(
                    text = uiState.detail.emojiWithTitle,
                    modifier = Modifier.basicMarquee(iterations = Int.MAX_VALUE),
                    maxLines = 1,
                )
            }
        },
        modifier = modifier,
        navigationIcon = {
            if (componentVisibleProvider().isNavigateUpButtonVisible) {
                NavigateUpButton(
                    onClick = { onEvent(TagDetailScaffoldEvent.ClickNavigateUp) },
                    contentDescription = stringResource(Res.string.tag_detail_navigate_up_button_content_description),
                )
            }
        },
        actions = {
            if (uiState is TagDetailUiState.Content) {
                TagDetailTopBarActions(
                    contentProvider = { uiState },
                    onEvent = onEvent,
                )
            }

            TagDetailScopeButton(
                onClick = { onEvent(TagDetailScaffoldEvent.ClickScope) },
                isAppliedProvider = isScopeAppliedProvider,
            )
        },
    )
}

@ComponentPreview
@Composable
private fun TagDetailTopBarPreview() {
    DiaryTheme {
        TagDetailTopBar(
            onEvent = {},
            uiStateProvider = {
                TagDetailUiState.Content(
                    id = Uuid.NIL,
                    detail = TagDetail.EMPTY.copy(title = "태그 제목"),
                    isFinished = false,
                )
            },
        )
    }
}
