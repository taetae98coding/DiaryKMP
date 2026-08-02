package io.github.taetae98coding.diary.feature.tag.ui.detail

import androidx.paging.PagingData
import io.github.taetae98coding.diary.compose.memo.MemoListItem
import io.github.taetae98coding.diary.compose.memo.MemoListUiState
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
    return viewModel
}
