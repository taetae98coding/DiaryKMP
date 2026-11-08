package io.github.taetae98coding.diary.feature.dday.ui.detail

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.taetae98coding.diary.compose.core.appbar.DiaryTopBar
import io.github.taetae98coding.diary.compose.core.appbar.DiaryTopBarIcon
import io.github.taetae98coding.diary.compose.core.preview.ComponentPreview
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.feature.dday.ui.Res
import io.github.taetae98coding.diary.feature.dday.ui.dday_detail_delete_button_content_description
import io.github.taetae98coding.diary.feature.dday.ui.dday_navigate_up_button_content_description
import io.github.taetae98coding.diary.feature.dday.ui.previewDDayDetail
import org.jetbrains.compose.resources.stringResource
import kotlin.uuid.Uuid

@Composable
internal fun DDayDetailTopBar(
    onEvent: (DDayDetailScaffoldEvent) -> Unit,
    modifier: Modifier = Modifier,
    uiStateProvider: () -> DDayDetailUiState = { DDayDetailUiState.Loading },
    componentVisibleProvider: () -> DDayDetailScaffoldComponentVisible = { DDayDetailScaffoldComponentVisible() },
) {
    val uiState = uiStateProvider()
    val navigateUpContentDescription = stringResource(Res.string.dday_navigate_up_button_content_description)
    val deleteContentDescription = stringResource(Res.string.dday_detail_delete_button_content_description)

    DiaryTopBar(
        title = {
            if (uiState is DDayDetailUiState.Content) {
                Title(text = uiState.detail.title, isMarquee = true)
            }
        },
        modifier = modifier,
        navigationIcon = {
            if (componentVisibleProvider().isNavigateUpButtonVisible) {
                NavigateUp(
                    onClick = { onEvent(DDayDetailScaffoldEvent.ClickNavigateUp) },
                    contentDescription = navigateUpContentDescription,
                )
            }
        },
        actions = {
            if (uiState is DDayDetailUiState.Content) {
                IconAction(
                    icon = DiaryTopBarIcon.DELETE,
                    contentDescription = deleteContentDescription,
                    onClick = { onEvent(DDayDetailScaffoldEvent.ClickDelete) },
                    isInProgress = uiState.isDeleteInProgress,
                )
            }
        },
    )
}

@ComponentPreview
@Composable
private fun DDayDetailTopBarPreview() {
    DiaryTheme {
        DDayDetailTopBar(
            onEvent = {},
            uiStateProvider = { DDayDetailUiState.Content(id = Uuid.NIL, detail = previewDDayDetail(title = "만난 날")) },
        )
    }
}
