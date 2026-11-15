package io.github.taetae98coding.diary.feature.tag.ui.detail.routine

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.ViewModelStoreProvider
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.rememberViewModelStoreOwner
import androidx.paging.compose.collectAsLazyPagingItems
import io.github.taetae98coding.diary.compose.routine.RoutineListEffect
import io.github.taetae98coding.diary.compose.routine.RoutineListEvent
import io.github.taetae98coding.diary.compose.routine.RoutineListState
import io.github.taetae98coding.diary.compose.routine.RoutineListUndoSnackbarEffect
import io.github.taetae98coding.diary.feature.core.sync.SyncRefreshViewModel
import io.github.taetae98coding.diary.feature.tag.ui.detail.scope.TagDetailScopeEffect
import io.github.taetae98coding.diary.feature.tag.ui.detail.scope.TagDetailScopeState
import io.github.taetae98coding.diary.feature.tag.ui.detail.tab.TagDetailTab
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import kotlin.uuid.Uuid

@Composable
internal fun TagDetailRoutineContent(
    id: Uuid,
    viewModelStoreProvider: ViewModelStoreProvider,
    navigateToRoutineDetail: (Uuid) -> Unit,
    navigateToRoutineFinishedList: () -> Unit,
    scopeState: TagDetailScopeState,
    listState: RoutineListState,
    modifier: Modifier = Modifier,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
) {
    val viewModelStoreOwner = rememberViewModelStoreOwner(key = TagDetailTab.ROUTINE, provider = viewModelStoreProvider)

    CompositionLocalProvider(LocalViewModelStoreOwner provides viewModelStoreOwner) {
        val routineViewModel = koinViewModel<TagDetailRoutineViewModel> { parametersOf(id) }
        val syncViewModel = koinViewModel<SyncRefreshViewModel>()
        val syncUiState by syncViewModel.uiState.collectAsStateWithLifecycle()
        val routinePagingItems = routineViewModel.routinePagingData.collectAsLazyPagingItems()
        val scopeUiState by routineViewModel.scopeUiState.collectAsStateWithLifecycle()

        RoutineListUndoSnackbarEffect(
            onUndo = { effect ->
                when (effect) {
                    is RoutineListEffect.Finished -> routineViewModel.restart(id = effect.id)
                    is RoutineListEffect.Restarted -> routineViewModel.finish(id = effect.id)
                    is RoutineListEffect.Deleted -> routineViewModel.restore(id = effect.id)
                }
            },
            effect = routineViewModel.effect,
            snackbarHostState = snackbarHostState,
        )

        TagDetailScopeEffect(
            onSelect = { scope -> routineViewModel.select(scope = scope) },
            state = scopeState,
        )

        UpdateTagDetailRoutineTodayEffect(
            routineViewModel = routineViewModel,
            listState = listState,
        )

        TagDetailRoutineTab(
            onEvent = { event ->
                when (event) {
                    is TagDetailRoutineContentEvent.Refresh -> syncViewModel.refresh()
                    is TagDetailRoutineContentEvent.ClickFinishedList -> navigateToRoutineFinishedList()
                }
            },
            onRoutineListEvent = { event ->
                when (event) {
                    is RoutineListEvent.ClickRoutine -> navigateToRoutineDetail(event.id)
                    is RoutineListEvent.SwipeFinish -> routineViewModel.finish(id = event.id)
                    is RoutineListEvent.SwipeRestart -> routineViewModel.restart(id = event.id)
                    is RoutineListEvent.SwipeDelete -> routineViewModel.delete(id = event.id)
                }
            },
            modifier = modifier,
            listState = listState,
            routinePagingItems = routinePagingItems,
            isRefreshingProvider = { syncUiState.isRefreshing },
            scopeProvider = { scopeUiState.scope },
        )
    }
}
