package io.github.taetae98coding.diary.feature.routine.ui.home

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.taetae98coding.diary.compose.core.button.FloatingAddButton
import io.github.taetae98coding.diary.compose.core.empty.DiaryEmptyBox
import io.github.taetae98coding.diary.compose.core.icon.RoutineIcon
import io.github.taetae98coding.diary.compose.core.placeholder.DiaryPlaceholderDefaults
import io.github.taetae98coding.diary.compose.core.preview.ScreenPreview
import io.github.taetae98coding.diary.compose.core.pulltorefresh.DiaryPullToRefreshBox
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.feature.routine.ui.Res
import io.github.taetae98coding.diary.feature.routine.ui.routine_home_add_button_content_description
import io.github.taetae98coding.diary.feature.routine.ui.routine_home_empty_description
import io.github.taetae98coding.diary.feature.routine.ui.routine_home_empty_title
import io.github.taetae98coding.diary.feature.routine.ui.routine_home_title
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun RoutineHomeScaffold(
    onEvent: (RoutineHomeScaffoldEvent) -> Unit,
    modifier: Modifier = Modifier,
    scrollState: ScrollState = rememberScrollState(),
    uiStateProvider: () -> RoutineHomeUiState = { RoutineHomeUiState() },
    componentVisibleProvider: () -> RoutineHomeScaffoldComponentVisible = { RoutineHomeScaffoldComponentVisible() },
) {
    Scaffold(
        modifier = modifier,
        topBar = { TopBar() },
        floatingActionButton = {
            if (componentVisibleProvider().isAddButtonVisible) {
                FloatingAddButton(
                    onClick = { onEvent(RoutineHomeScaffoldEvent.ClickAdd) },
                    contentDescription = stringResource(Res.string.routine_home_add_button_content_description),
                )
            }
        },
    ) { paddingValues ->
        DiaryPullToRefreshBox(
            isRefreshingProvider = { uiStateProvider().isRefreshing },
            onRefresh = { onEvent(RoutineHomeScaffoldEvent.Refresh) },
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
        ) {
            DiaryEmptyBox(
                title = stringResource(Res.string.routine_home_empty_title),
                // 당겨서 새로고침은 스크롤 가능한 자식의 중첩 스크롤로만 감지하므로 빈 상태에도 스크롤을 둔다.
                modifier =
                    Modifier
                        .fillMaxSize()
                        .verticalScroll(scrollState),
                description = stringResource(Res.string.routine_home_empty_description),
                icon = { RoutineIcon(modifier = Modifier.size(DiaryPlaceholderDefaults.IconSize)) },
            )
        }
    }
}

@Composable
private fun TopBar(modifier: Modifier = Modifier) {
    TopAppBar(
        title = { Text(text = stringResource(Res.string.routine_home_title)) },
        modifier = modifier,
    )
}

@ScreenPreview
@Composable
private fun RoutineHomeScaffoldPreview() {
    DiaryTheme {
        RoutineHomeScaffold(onEvent = {})
    }
}
