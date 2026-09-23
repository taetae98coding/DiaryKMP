package io.github.taetae98coding.diary.feature.memo.ui.home.filter

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.compose.collectAsLazyPagingItems
import io.github.taetae98coding.diary.compose.tag.filter.TagFilterEvent
import org.koin.compose.viewmodel.koinViewModel

@Composable
internal fun MemoHomeFilterContent(modifier: Modifier = Modifier) {
    val viewModel = koinViewModel<MemoHomeFilterViewModel>()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val tagPagingItems = viewModel.tagPagingData.collectAsLazyPagingItems()

    MemoHomeFilterBottomSheetContent(
        tagPagingItems = tagPagingItems,
        uiStateProvider = { uiState },
        onEvent = { event ->
            when (event) {
                is MemoHomeFilterBottomSheetEvent.SetDateExistence -> viewModel.setDateExistence(existence = event.existence)
                is MemoHomeFilterBottomSheetEvent.SetTagExistence -> viewModel.setTagExistence(existence = event.existence)
                is MemoHomeFilterBottomSheetEvent.SetPlaceExistence -> viewModel.setPlaceExistence(existence = event.existence)
            }
        },
        onTagFilterEvent = { event ->
            when (event) {
                is TagFilterEvent.Select -> viewModel.selectTag(id = event.id)
                is TagFilterEvent.Unselect -> viewModel.unselectTag(id = event.id)
                is TagFilterEvent.UnselectAll -> viewModel.unselectAllTag()
            }
        },
        modifier = modifier,
    )
}
