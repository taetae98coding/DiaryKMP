package io.github.taetae98coding.diary.feature.tag.ui.detail

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.taetae98coding.diary.compose.core.appbar.DiaryTopBar
import io.github.taetae98coding.diary.compose.core.appbar.DiaryTopBarActionScope
import io.github.taetae98coding.diary.compose.core.appbar.DiaryTopBarIcon
import io.github.taetae98coding.diary.compose.core.preview.ComponentPreview
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.core.model.tag.TagDetail
import io.github.taetae98coding.diary.feature.tag.ui.Res
import io.github.taetae98coding.diary.feature.tag.ui.tag_detail_delete_button_content_description
import io.github.taetae98coding.diary.feature.tag.ui.tag_detail_finish_button_content_description
import io.github.taetae98coding.diary.feature.tag.ui.tag_detail_navigate_up_button_content_description
import io.github.taetae98coding.diary.feature.tag.ui.tag_detail_restart_button_content_description
import io.github.taetae98coding.diary.feature.tag.ui.tag_detail_scope_action_content_description
import io.github.taetae98coding.diary.feature.tag.ui.tag_detail_scope_applied_state_description
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
    val navigateUpContentDescription = stringResource(Res.string.tag_detail_navigate_up_button_content_description)
    val deleteContentDescription = stringResource(Res.string.tag_detail_delete_button_content_description)
    val scopeContentDescription = stringResource(Res.string.tag_detail_scope_action_content_description)
    val scopeAppliedStateDescription = stringResource(Res.string.tag_detail_scope_applied_state_description)

    DiaryTopBar(
        title = {
            if (uiState is TagDetailUiState.Content) {
                Title(text = uiState.detail.emojiWithTitle, isMarquee = true)
            }
        },
        modifier = modifier,
        navigationIcon = {
            if (componentVisibleProvider().isNavigateUpButtonVisible) {
                NavigateUp(
                    onClick = { onEvent(TagDetailScaffoldEvent.ClickNavigateUp) },
                    contentDescription = navigateUpContentDescription,
                )
            }
        },
        actions = {
            if (uiState is TagDetailUiState.Content) {
                TagDetailFinishAction(content = uiState, onEvent = onEvent)
                IconAction(
                    icon = DiaryTopBarIcon.DELETE,
                    contentDescription = deleteContentDescription,
                    onClick = { onEvent(TagDetailScaffoldEvent.ClickDelete) },
                    isInProgress = uiState.isDeleteInProgress,
                )
            }
            AppliedAction(
                icon = DiaryTopBarIcon.FILTER,
                contentDescription = scopeContentDescription,
                isApplied = isScopeAppliedProvider(),
                appliedStateDescription = scopeAppliedStateDescription,
                onClick = { onEvent(TagDetailScaffoldEvent.ClickScope) },
            )
        },
    )
}

@Composable
private fun DiaryTopBarActionScope.TagDetailFinishAction(
    content: TagDetailUiState.Content,
    onEvent: (TagDetailScaffoldEvent) -> Unit,
) {
    ToggleAction(
        icon = DiaryTopBarIcon.FINISH,
        contentDescription =
            if (content.isFinished) {
                stringResource(Res.string.tag_detail_restart_button_content_description)
            } else {
                stringResource(Res.string.tag_detail_finish_button_content_description)
            },
        isChecked = content.isFinished,
        onClick = {
            if (content.isFinished) {
                onEvent(TagDetailScaffoldEvent.ClickRestart)
            } else {
                onEvent(TagDetailScaffoldEvent.ClickFinish)
            }
        },
        isInProgress = content.isFinishInProgress,
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
