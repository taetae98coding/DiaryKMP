package io.github.taetae98coding.diary.feature.checklist.ui.detail

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.taetae98coding.diary.compose.core.appbar.DiaryTopBar
import io.github.taetae98coding.diary.compose.core.appbar.DiaryTopBarIcon
import io.github.taetae98coding.diary.compose.core.preview.ComponentPreview
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.feature.checklist.ui.Res
import io.github.taetae98coding.diary.feature.checklist.ui.checklist_detail_delete_button_content_description
import io.github.taetae98coding.diary.feature.checklist.ui.checklist_detail_reset_button_content_description
import io.github.taetae98coding.diary.feature.checklist.ui.checklist_navigate_up_button_content_description
import io.github.taetae98coding.diary.feature.checklist.ui.previewChecklist
import org.jetbrains.compose.resources.stringResource
import kotlin.uuid.Uuid

@Composable
internal fun ChecklistDetailTopBar(
    onEvent: (ChecklistDetailScaffoldEvent) -> Unit,
    modifier: Modifier = Modifier,
    uiStateProvider: () -> ChecklistDetailUiState = { ChecklistDetailUiState.Loading },
    itemUiStateProvider: () -> ChecklistDetailItemUiState = { ChecklistDetailItemUiState() },
) {
    val uiState = uiStateProvider()
    val navigateUpContentDescription = stringResource(Res.string.checklist_navigate_up_button_content_description)
    val resetContentDescription = stringResource(Res.string.checklist_detail_reset_button_content_description)
    val deleteContentDescription = stringResource(Res.string.checklist_detail_delete_button_content_description)

    DiaryTopBar(
        title = {
            if (uiState is ChecklistDetailUiState.Content) {
                Title(text = uiState.detail.title, isMarquee = true)
            }
        },
        modifier = modifier,
        navigationIcon = {
            NavigateUp(
                onClick = { onEvent(ChecklistDetailScaffoldEvent.ClickNavigateUp) },
                contentDescription = navigateUpContentDescription,
            )
        },
        actions = {
            if (uiState is ChecklistDetailUiState.Content) {
                val itemUiState = itemUiStateProvider()

                IconAction(
                    icon = DiaryTopBarIcon.RESET,
                    contentDescription = resetContentDescription,
                    onClick = { onEvent(ChecklistDetailScaffoldEvent.ClickReset) },
                    isEnabled = itemUiState.isResetEnabled,
                    isInProgress = itemUiState.isResetInProgress,
                )
                IconAction(
                    icon = DiaryTopBarIcon.DELETE,
                    contentDescription = deleteContentDescription,
                    onClick = { onEvent(ChecklistDetailScaffoldEvent.ClickDelete) },
                    isInProgress = uiState.isDeleteInProgress,
                )
            }
        },
    )
}

@ComponentPreview
@Composable
private fun ChecklistDetailTopBarPreview() {
    DiaryTheme {
        ChecklistDetailTopBar(
            onEvent = {},
            uiStateProvider = { ChecklistDetailUiState.Content(id = Uuid.NIL, detail = previewChecklist(title = "여행 준비").detail) },
        )
    }
}
