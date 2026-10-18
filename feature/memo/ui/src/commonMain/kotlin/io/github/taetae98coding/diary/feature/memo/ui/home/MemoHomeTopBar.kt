package io.github.taetae98coding.diary.feature.memo.ui.home

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.taetae98coding.diary.compose.core.appbar.DiaryTopBar
import io.github.taetae98coding.diary.compose.core.appbar.DiaryTopBarIcon
import io.github.taetae98coding.diary.compose.core.preview.ComponentPreview
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.feature.memo.ui.Res
import io.github.taetae98coding.diary.feature.memo.ui.memo_home_filter_action_content_description
import io.github.taetae98coding.diary.feature.memo.ui.memo_home_filter_applied_state_description
import io.github.taetae98coding.diary.feature.memo.ui.memo_home_search_action_content_description
import io.github.taetae98coding.diary.feature.memo.ui.memo_home_title
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun MemoHomeTopBar(
    onEvent: (MemoHomeScaffoldEvent) -> Unit,
    modifier: Modifier = Modifier,
    filterUiStateProvider: () -> MemoHomeScaffoldFilterUiState = { MemoHomeScaffoldFilterUiState() },
) {
    val filterUiState = filterUiStateProvider()
    val title = stringResource(Res.string.memo_home_title)
    val searchContentDescription = stringResource(Res.string.memo_home_search_action_content_description)
    val filterContentDescription = stringResource(Res.string.memo_home_filter_action_content_description)
    val filterAppliedStateDescription = stringResource(Res.string.memo_home_filter_applied_state_description)

    DiaryTopBar(
        title = { Title(text = title) },
        modifier = modifier,
        actions = {
            IconAction(
                icon = DiaryTopBarIcon.SEARCH,
                contentDescription = searchContentDescription,
                onClick = { onEvent(MemoHomeScaffoldEvent.ClickSearch) },
            )
            AppliedAction(
                icon = DiaryTopBarIcon.FILTER,
                contentDescription = filterContentDescription,
                isApplied = filterUiState.isApplied,
                appliedStateDescription = filterAppliedStateDescription,
                onClick = { onEvent(MemoHomeScaffoldEvent.ClickFilter) },
            )
        },
    )
}

@ComponentPreview
@Composable
private fun MemoHomeTopBarPreview() {
    DiaryTheme {
        MemoHomeTopBar(onEvent = {})
    }
}
