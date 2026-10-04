package io.github.taetae98coding.diary.feature.search.ui.home.tag

import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.ViewModelStoreProvider
import io.github.taetae98coding.diary.compose.tag.list.TagListEffect
import io.github.taetae98coding.diary.compose.tag.list.TagListEvent
import io.github.taetae98coding.diary.compose.tag.list.TagListUndoSnackbarEffect
import io.github.taetae98coding.diary.core.model.tag.Tag
import io.github.taetae98coding.diary.feature.search.api.SearchHomeType
import io.github.taetae98coding.diary.feature.search.ui.home.result.SearchHomeResultContent
import io.github.taetae98coding.diary.feature.search.ui.home.result.SearchHomeResultEvent
import org.koin.compose.viewmodel.koinViewModel
import kotlin.uuid.Uuid

@Composable
internal fun SearchHomeTagContent(
    queryState: TextFieldState,
    viewModelStoreProvider: ViewModelStoreProvider,
    snackbarHostState: SnackbarHostState,
    navigateToDetail: (Uuid) -> Unit,
    modifier: Modifier = Modifier,
) {
    SearchHomeResultContent<Tag, SearchHomeTagViewModel>(
        type = SearchHomeType.TAG,
        queryState = queryState,
        viewModelStoreProvider = viewModelStoreProvider,
        viewModel = { koinViewModel<SearchHomeTagViewModel>() },
    ) { viewModel, state ->
        TagListUndoSnackbarEffect(
            onRestart = { id -> viewModel.undo(effect = TagListEffect.Finished(id = id)) },
            onFinish = { id -> viewModel.undo(effect = TagListEffect.Restarted(id = id)) },
            onRestore = { id -> viewModel.undo(effect = TagListEffect.Deleted(id = id)) },
            effect = viewModel.effect,
            snackbarHostState = snackbarHostState,
        )
        SearchHomeTagList(
            onEvent = { event ->
                when (event) {
                    is SearchHomeResultEvent.ClickSort -> state.sortSheetState.show()
                    is SearchHomeResultEvent.SelectSort -> viewModel.select(sort = event.sort)
                }
            },
            onTagListEvent = { event ->
                when (event) {
                    is TagListEvent.ClickTag -> navigateToDetail(event.id)
                    is TagListEvent.SwipeFinish -> viewModel.finish(id = event.id)
                    is TagListEvent.SwipeRestart -> viewModel.restart(id = event.id)
                    is TagListEvent.SwipeDelete -> viewModel.delete(id = event.id)
                }
            },
            modifier = modifier,
            listState = state.listState,
            sortSheetState = state.sortSheetState,
            tagPagingItems = state.pagingItems,
            query = state.uiState.appliedQuery,
            sortProvider = { state.uiState.sort },
        )
    }
}
