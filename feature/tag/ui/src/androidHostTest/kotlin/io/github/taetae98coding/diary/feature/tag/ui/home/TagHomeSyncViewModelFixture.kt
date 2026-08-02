package io.github.taetae98coding.diary.feature.tag.ui.home

import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow

internal fun screenTestSyncViewModel(uiState: TagHomeUiState = TagHomeUiState()): TagHomeSyncViewModel {
    val viewModel = mockk<TagHomeSyncViewModel>(relaxed = true)
    every { viewModel.uiState } returns MutableStateFlow(uiState)
    return viewModel
}
