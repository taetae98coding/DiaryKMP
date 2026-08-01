package io.github.taetae98coding.diary.feature.memo.ui.home

import io.github.taetae98coding.diary.compose.memo.MemoListUiState
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow

internal fun screenTestSyncViewModel(uiState: MemoListUiState = MemoListUiState()): MemoHomeSyncViewModel {
    val viewModel = mockk<MemoHomeSyncViewModel>(relaxed = true)
    every { viewModel.uiState } returns MutableStateFlow(uiState)
    return viewModel
}
