package io.github.taetae98coding.diary.feature.memo.ui.gemini

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import io.github.taetae98coding.diary.compose.core.effect.CollectEffect
import io.github.taetae98coding.diary.compose.core.snackbar.showImmediate
import io.github.taetae98coding.diary.feature.memo.ui.Res
import io.github.taetae98coding.diary.feature.memo.ui.memo_gemini_setting_required_message
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun MemoGeminiSettingRequiredEffect(
    hostState: SnackbarHostState,
    effect: Flow<MemoGeminiEffect> = emptyFlow(),
) {
    val coroutineScope = rememberCoroutineScope()
    val settingRequiredMessage = stringResource(Res.string.memo_gemini_setting_required_message)

    CollectEffect(effect) { value ->
        when (value) {
            is MemoGeminiEffect.SettingRequired -> {
                coroutineScope.launch { hostState.showImmediate(message = settingRequiredMessage) }
            }
        }
    }
}
