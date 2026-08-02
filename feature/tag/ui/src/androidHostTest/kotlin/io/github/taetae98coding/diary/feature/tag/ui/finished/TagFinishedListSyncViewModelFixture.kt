package io.github.taetae98coding.diary.feature.tag.ui.finished

import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow

internal fun screenTestSyncViewModel(uiState: TagFinishedListUiState = TagFinishedListUiState()): TagFinishedListSyncViewModel {
    val viewModel = mockk<TagFinishedListSyncViewModel>(relaxed = true)
    every { viewModel.uiState } returns MutableStateFlow(uiState)
    return viewModel
}
