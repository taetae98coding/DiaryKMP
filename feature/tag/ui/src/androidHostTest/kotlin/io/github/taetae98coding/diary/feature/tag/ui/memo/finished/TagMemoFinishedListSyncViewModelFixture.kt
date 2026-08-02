package io.github.taetae98coding.diary.feature.tag.ui.memo.finished

import io.github.taetae98coding.diary.compose.memo.MemoListUiState
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow

internal fun screenTestSyncViewModel(uiState: MemoListUiState = MemoListUiState()): TagMemoFinishedListSyncViewModel {
    val viewModel = mockk<TagMemoFinishedListSyncViewModel>(relaxed = true)
    every { viewModel.uiState } returns MutableStateFlow(uiState)
    return viewModel
}
