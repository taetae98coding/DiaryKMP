package io.github.taetae98coding.diary.feature.routine.ui.home.filter

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.compose.collectAsLazyPagingItems
import io.github.taetae98coding.diary.compose.tag.filter.TagFilterEvent
import org.koin.compose.viewmodel.koinViewModel

@Composable
internal fun RoutineHomeFilterContent(
    navigateToTagAdd: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val viewModel = koinViewModel<RoutineHomeFilterViewModel>()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val tagPagingItems = viewModel.tagPagingData.collectAsLazyPagingItems()

    RoutineHomeFilterBottomSheetContent(
        onEvent = { event ->
            when (event) {
                is RoutineHomeFilterBottomSheetEvent.SetViewScope -> viewModel.setViewScope(viewScope = event.viewScope)
            }
        },
        onTagFilterEvent = { event ->
            when (event) {
                is TagFilterEvent.Select -> viewModel.selectTag(id = event.id)
                is TagFilterEvent.Unselect -> viewModel.unselectTag(id = event.id)
                is TagFilterEvent.UnselectAll -> viewModel.unselectAllTag()
                is TagFilterEvent.ClickAdd -> navigateToTagAdd()
            }
        },
        modifier = modifier,
        tagPagingItems = tagPagingItems,
        uiStateProvider = { uiState },
    )
}
