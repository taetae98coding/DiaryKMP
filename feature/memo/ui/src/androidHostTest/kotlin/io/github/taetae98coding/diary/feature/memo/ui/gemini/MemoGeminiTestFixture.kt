package io.github.taetae98coding.diary.feature.memo.ui.gemini

import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.emptyFlow

internal fun screenTestGeminiViewModel(uiState: StateFlow<MemoGeminiUiState> = MutableStateFlow(MemoGeminiUiState())): MemoGeminiViewModel {
    val viewModel = mockk<MemoGeminiViewModel>()

    every { viewModel.uiState } returns uiState
    every { viewModel.effect } returns emptyFlow()
    every { viewModel.open() } returns Unit
    every { viewModel.close() } returns Unit
    every { viewModel.cancel() } returns Unit
    every { viewModel.generate(any()) } returns Unit
    every { viewModel.markApplied(any()) } returns Unit

    return viewModel
}
