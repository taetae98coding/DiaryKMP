package io.github.taetae98coding.diary.feature.memo.ui.gemini

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect

@Composable
internal fun MemoGeminiCloseEffect(geminiViewModel: MemoGeminiViewModel) {
    LaunchedEffect(geminiViewModel) {
        geminiViewModel.close()
    }
}
