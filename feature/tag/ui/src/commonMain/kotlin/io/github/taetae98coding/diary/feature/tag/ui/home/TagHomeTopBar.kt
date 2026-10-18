package io.github.taetae98coding.diary.feature.tag.ui.home

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.taetae98coding.diary.compose.core.appbar.DiaryTopBar
import io.github.taetae98coding.diary.compose.core.appbar.DiaryTopBarIcon
import io.github.taetae98coding.diary.compose.core.preview.ComponentPreview
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.feature.tag.ui.Res
import io.github.taetae98coding.diary.feature.tag.ui.tag_home_filter_action_content_description
import io.github.taetae98coding.diary.feature.tag.ui.tag_home_filter_applied_state_description
import io.github.taetae98coding.diary.feature.tag.ui.tag_home_search_action_content_description
import io.github.taetae98coding.diary.feature.tag.ui.tag_home_title
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun TagHomeTopBar(
    onEvent: (TagHomeScaffoldEvent) -> Unit,
    modifier: Modifier = Modifier,
    filterUiStateProvider: () -> TagHomeScaffoldFilterUiState = { TagHomeScaffoldFilterUiState() },
) {
    val filterUiState = filterUiStateProvider()
    val title = stringResource(Res.string.tag_home_title)
    val searchContentDescription = stringResource(Res.string.tag_home_search_action_content_description)
    val filterContentDescription = stringResource(Res.string.tag_home_filter_action_content_description)
    val filterAppliedStateDescription = stringResource(Res.string.tag_home_filter_applied_state_description)

    DiaryTopBar(
        title = { Title(text = title) },
        modifier = modifier,
        actions = {
            IconAction(
                icon = DiaryTopBarIcon.SEARCH,
                contentDescription = searchContentDescription,
                onClick = { onEvent(TagHomeScaffoldEvent.ClickSearch) },
            )
            AppliedAction(
                icon = DiaryTopBarIcon.FILTER,
                contentDescription = filterContentDescription,
                isApplied = filterUiState.isApplied,
                appliedStateDescription = filterAppliedStateDescription,
                onClick = { onEvent(TagHomeScaffoldEvent.ClickFilter) },
            )
        },
    )
}

@ComponentPreview
@Composable
private fun TagHomeTopBarPreview() {
    DiaryTheme {
        TagHomeTopBar(onEvent = {})
    }
}
