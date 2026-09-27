package io.github.taetae98coding.diary.feature.tag.ui.detail.memo

import androidx.paging.PagingData
import io.github.taetae98coding.diary.compose.memo.list.MemoListItem
import io.github.taetae98coding.diary.compose.memo.list.MemoListUiState
import io.github.taetae98coding.diary.core.model.tag.TagScope
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf

internal fun screenTestMemoSyncViewModel(uiState: MemoListUiState = MemoListUiState()): TagDetailMemoSyncViewModel {
    val viewModel = mockk<TagDetailMemoSyncViewModel>(relaxed = true)
    every { viewModel.uiState } returns MutableStateFlow(uiState)
    return viewModel
}

internal fun screenTestMemoViewModel(memoPagingData: Flow<PagingData<MemoListItem>> = flowOf(PagingData.empty())): TagDetailMemoViewModel {
    val viewModel = mockk<TagDetailMemoViewModel>(relaxed = true)
    every { viewModel.memoPagingData } returns memoPagingData
    every { viewModel.effect } returns emptyFlow()
    every { viewModel.scope } returns MutableStateFlow(TagScope.SELF)
    return viewModel
}
