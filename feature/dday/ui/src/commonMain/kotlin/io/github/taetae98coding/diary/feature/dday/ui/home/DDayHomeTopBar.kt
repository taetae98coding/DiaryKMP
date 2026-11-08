package io.github.taetae98coding.diary.feature.dday.ui.home

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewParameter
import io.github.taetae98coding.diary.compose.core.appbar.DiaryTopBar
import io.github.taetae98coding.diary.compose.core.appbar.DiaryTopBarIcon
import io.github.taetae98coding.diary.compose.core.preview.BooleanPreviewParameter
import io.github.taetae98coding.diary.compose.core.preview.ComponentPreview
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.feature.dday.ui.Res
import io.github.taetae98coding.diary.feature.dday.ui.dday_home_filter_action_content_description
import io.github.taetae98coding.diary.feature.dday.ui.dday_home_filter_applied_state_description
import io.github.taetae98coding.diary.feature.dday.ui.dday_home_title
import io.github.taetae98coding.diary.feature.dday.ui.dday_navigate_up_button_content_description
import org.jetbrains.compose.resources.stringResource
import kotlin.uuid.Uuid

@Composable
internal fun DDayHomeTopBar(
    onEvent: (DDayHomeScaffoldEvent) -> Unit,
    modifier: Modifier = Modifier,
    filterUiStateProvider: () -> DDayHomeScaffoldFilterUiState = { DDayHomeScaffoldFilterUiState() },
) {
    val title = stringResource(Res.string.dday_home_title)
    val navigateUpContentDescription = stringResource(Res.string.dday_navigate_up_button_content_description)
    val filterContentDescription = stringResource(Res.string.dday_home_filter_action_content_description)
    val filterAppliedStateDescription = stringResource(Res.string.dday_home_filter_applied_state_description)

    DiaryTopBar(
        title = { Title(text = title) },
        modifier = modifier,
        navigationIcon = {
            NavigateUp(
                onClick = { onEvent(DDayHomeScaffoldEvent.ClickNavigateUp) },
                contentDescription = navigateUpContentDescription,
            )
        },
        actions = {
            AppliedAction(
                icon = DiaryTopBarIcon.FILTER,
                contentDescription = filterContentDescription,
                isApplied = filterUiStateProvider().isApplied,
                appliedStateDescription = filterAppliedStateDescription,
                onClick = { onEvent(DDayHomeScaffoldEvent.ClickFilter) },
            )
        },
    )
}

@ComponentPreview
@Composable
private fun DDayHomeTopBarPreview(
    @PreviewParameter(BooleanPreviewParameter::class) isFilterApplied: Boolean,
) {
    val filterUiState =
        if (isFilterApplied) {
            DDayHomeScaffoldFilterUiState(selectedTagIdSet = setOf(Uuid.NIL))
        } else {
            DDayHomeScaffoldFilterUiState()
        }

    DiaryTheme {
        DDayHomeTopBar(
            onEvent = {},
            filterUiStateProvider = { filterUiState },
        )
    }
}
