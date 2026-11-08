package io.github.taetae98coding.diary.feature.dday.ui.home.filter

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.compose.collectAsLazyPagingItems
import io.github.taetae98coding.diary.compose.tag.filter.TagFilterEvent
import org.koin.compose.viewmodel.koinViewModel

@Composable
internal fun DDayHomeFilterContent(
    navigateToTagAdd: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val viewModel = koinViewModel<DDayHomeFilterViewModel>()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val tagPagingItems = viewModel.tagPagingData.collectAsLazyPagingItems()

    DDayHomeFilterBottomSheetContent(
        onEvent = { event ->
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
