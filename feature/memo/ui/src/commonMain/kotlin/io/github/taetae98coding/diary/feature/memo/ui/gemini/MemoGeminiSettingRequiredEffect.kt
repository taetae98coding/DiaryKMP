package io.github.taetae98coding.diary.feature.memo.ui.gemini

import androidx.compose.runtime.Composable
import io.github.taetae98coding.diary.compose.core.effect.CollectEffect
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

@Composable
internal fun MemoGeminiSettingRequiredEffect(
    navigateToGeminiSetting: () -> Unit,
    effect: Flow<MemoGeminiEffect> = emptyFlow(),
) {
    CollectEffect(effect) { value ->
        when (value) {
            is MemoGeminiEffect.SettingRequired -> navigateToGeminiSetting()
        }
    }
}
