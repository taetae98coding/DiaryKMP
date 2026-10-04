package io.github.taetae98coding.diary.feature.tag.ui.finished

import io.github.taetae98coding.diary.feature.core.sync.SyncRefreshUiState
import io.github.taetae98coding.diary.feature.core.sync.SyncRefreshViewModel
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow

internal fun screenTestSyncViewModel(uiState: SyncRefreshUiState = SyncRefreshUiState()): SyncRefreshViewModel {
    val viewModel = mockk<SyncRefreshViewModel>(relaxed = true)
    every { viewModel.uiState } returns MutableStateFlow(uiState)
    return viewModel
}
