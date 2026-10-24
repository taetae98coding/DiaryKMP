package io.github.taetae98coding.diary.feature.checklist.ui.home

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.paging.PagingData
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import io.github.taetae98coding.diary.compose.core.appbar.DiaryNavigateUpTopBar
import io.github.taetae98coding.diary.compose.core.button.FloatingAddButton
import io.github.taetae98coding.diary.compose.core.preview.ScreenPreview
import io.github.taetae98coding.diary.compose.core.shortcut.addShortcut
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.core.model.checklist.ChecklistSummary
import io.github.taetae98coding.diary.feature.checklist.ui.Res
import io.github.taetae98coding.diary.feature.checklist.ui.checklist_add_button_content_description
import io.github.taetae98coding.diary.feature.checklist.ui.checklist_home_title
import io.github.taetae98coding.diary.feature.checklist.ui.checklist_navigate_up_button_content_description
import io.github.taetae98coding.diary.feature.core.sync.SyncRefreshUiState
import kotlinx.coroutines.flow.flowOf
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun ChecklistHomeScaffold(
    onEvent: (ChecklistHomeScaffoldEvent) -> Unit,
    modifier: Modifier = Modifier,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    checklistPagingItems: LazyPagingItems<ChecklistSummary> = remember { flowOf(PagingData.empty<ChecklistSummary>()) }.collectAsLazyPagingItems(),
    uiStateProvider: () -> SyncRefreshUiState = { SyncRefreshUiState() },
) {
    Scaffold(
        modifier = modifier.addShortcut { onEvent(ChecklistHomeScaffoldEvent.ClickAdd) },
        topBar = {
            DiaryNavigateUpTopBar(
                title = stringResource(Res.string.checklist_home_title),
                onNavigateUp = { onEvent(ChecklistHomeScaffoldEvent.ClickNavigateUp) },
                navigateUpContentDescription = stringResource(Res.string.checklist_navigate_up_button_content_description),
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        floatingActionButton = {
            FloatingAddButton(
                onClick = { onEvent(ChecklistHomeScaffoldEvent.ClickAdd) },
                contentDescription = stringResource(Res.string.checklist_add_button_content_description),
            )
        },
    ) { paddingValues ->
        ChecklistHomeList(
            onEvent = onEvent,
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
            checklistPagingItems = checklistPagingItems,
            isRefreshingProvider = { uiStateProvider().isRefreshing },
            bottomPadding = DiaryTheme.dimens.floatingActionButtonClearance,
        )
    }
}

@ScreenPreview
@Composable
private fun ChecklistHomeScaffoldPreview() {
    DiaryTheme {
        ChecklistHomeScaffold(onEvent = {})
    }
}
