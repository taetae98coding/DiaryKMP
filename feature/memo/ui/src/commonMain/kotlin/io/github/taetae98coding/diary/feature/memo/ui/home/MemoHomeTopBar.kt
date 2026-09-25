package io.github.taetae98coding.diary.feature.memo.ui.home

import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import io.github.taetae98coding.diary.compose.core.button.SearchButton
import io.github.taetae98coding.diary.compose.core.icon.FilterIcon
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
    val appliedStateDescription = stringResource(Res.string.memo_home_filter_applied_state_description)

    TopAppBar(
        title = { Text(text = stringResource(Res.string.memo_home_title)) },
        actions = {
            SearchButton(
                onClick = { onEvent(MemoHomeScaffoldEvent.ClickSearch) },
                contentDescription = stringResource(Res.string.memo_home_search_action_content_description),
            )
            IconButton(
                onClick = { onEvent(MemoHomeScaffoldEvent.ClickFilter) },
                modifier =
                    Modifier.semantics {
                        if (filterUiState.isApplied) {
                            stateDescription = appliedStateDescription
                        }
                    },
                colors =
                    IconButtonDefaults.iconButtonColors(
                        contentColor =
                            if (filterUiState.isApplied) {
                                DiaryTheme.colorScheme.primary
                            } else {
                                DiaryTheme.colorScheme.onSurfaceVariant
                            },
                    ),
            ) {
                FilterIcon(
                    contentDescription =
                        stringResource(
                            Res.string.memo_home_filter_action_content_description,
                        ),
                )
            }
        },
        modifier = modifier,
    )
}

@ComponentPreview
@Composable
private fun MemoHomeTopBarPreview() {
    DiaryTheme {
        MemoHomeTopBar(onEvent = {})
    }
}
