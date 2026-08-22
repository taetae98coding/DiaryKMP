package io.github.taetae98coding.diary.feature.setting.ui.gemini

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.taetae98coding.diary.compose.core.layout.DiaryInputColumn
import io.github.taetae98coding.diary.compose.core.preview.ScreenPreview
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme

@Composable
internal fun SettingGeminiForm(
    onEvent: (SettingGeminiScaffoldEvent) -> Unit,
    modifier: Modifier = Modifier,
    state: SettingGeminiFormState = rememberSettingGeminiFormState(),
    modelUiStateProvider: () -> SettingGeminiModelUiState = { SettingGeminiModelUiState() },
) {
    DiaryInputColumn(modifier = modifier) {
        SettingGeminiApiKeyInput(
            modifier = Modifier.fillMaxWidth(),
            state = state,
        )
        SettingGeminiModelRow(
            onClick = { onEvent(SettingGeminiScaffoldEvent.ClickModel) },
            modifier = Modifier.fillMaxWidth(),
            state = state,
            isMissingFromLoadedListProvider = { modelUiStateProvider().isModelMissing(model = state.model) },
        )
        SettingGeminiSystemPromptInput(
            modifier = Modifier.fillMaxWidth(),
            state = state,
        )
    }
}

private fun SettingGeminiModelUiState.isModelMissing(model: String): Boolean = isLoaded && model.isNotEmpty() && modelList.none { loaded -> loaded.id == model }

@ScreenPreview
@Composable
private fun SettingGeminiFormPreview() {
    DiaryTheme {
        Surface {
            SettingGeminiForm(onEvent = {})
        }
    }
}
