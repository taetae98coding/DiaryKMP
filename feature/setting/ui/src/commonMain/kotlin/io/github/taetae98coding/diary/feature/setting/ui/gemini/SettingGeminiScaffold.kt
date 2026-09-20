package io.github.taetae98coding.diary.feature.setting.ui.gemini

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import io.github.taetae98coding.diary.compose.core.animation.DiaryScaleVisibility
import io.github.taetae98coding.diary.compose.core.appbar.DiaryNavigateUpTopBar
import io.github.taetae98coding.diary.compose.core.button.FloatingCheckButton
import io.github.taetae98coding.diary.compose.core.preview.ScreenPreview
import io.github.taetae98coding.diary.compose.core.scaffold.DiaryScaffoldDefaults
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.core.model.gemini.GeminiSetting
import io.github.taetae98coding.diary.feature.setting.ui.Res
import io.github.taetae98coding.diary.feature.setting.ui.previewGeminiSetting
import io.github.taetae98coding.diary.feature.setting.ui.setting_gemini_save_button_content_description
import io.github.taetae98coding.diary.feature.setting.ui.setting_gemini_title
import io.github.taetae98coding.diary.feature.setting.ui.setting_navigate_up_button_content_description
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun SettingGeminiScaffold(
    onEvent: (SettingGeminiScaffoldEvent) -> Unit,
    onModelDialogEvent: (SettingGeminiModelDialogEvent) -> Unit,
    modifier: Modifier = Modifier,
    state: SettingGeminiFormState = rememberSettingGeminiFormState(),
    uiStateProvider: () -> SettingGeminiUiState = { SettingGeminiUiState.Loading },
    modelUiStateProvider: () -> SettingGeminiModelUiState = { SettingGeminiModelUiState() },
    componentVisibleProvider: () -> SettingGeminiScaffoldComponentVisible = { SettingGeminiScaffoldComponentVisible() },
) {
    val isChanged by remember(state) {
        derivedStateOf {
            val loaded = uiStateProvider() as? SettingGeminiUiState.Loaded
            loaded != null && state.setting != loaded.setting
        }
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            DiaryNavigateUpTopBar(
                title = stringResource(Res.string.setting_gemini_title),
                onNavigateUp = { onEvent(SettingGeminiScaffoldEvent.ClickNavigateUp) },
                navigateUpContentDescription = stringResource(Res.string.setting_navigate_up_button_content_description),
                isNavigateUpVisibleProvider = { componentVisibleProvider().isNavigateUpButtonVisible },
            )
        },
        snackbarHost = { SnackbarHost(hostState = state.hostState) },
        floatingActionButton = {
            DiaryScaleVisibility(visible = isChanged) {
                FloatingCheckButton(
                    onClick = { onEvent(SettingGeminiScaffoldEvent.ClickSave) },
                    contentDescription = stringResource(Res.string.setting_gemini_save_button_content_description),
                    isInProgressProvider = { (uiStateProvider() as? SettingGeminiUiState.Loaded)?.isInProgress == true },
                )
            }
        },
        contentWindowInsets = DiaryScaffoldDefaults.contentWindowInsets,
    ) { paddingValues ->
        when (uiStateProvider()) {
            is SettingGeminiUiState.Loading -> Unit

            is SettingGeminiUiState.Loaded ->
                SettingGeminiForm(
                    onEvent = onEvent,
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .padding(paddingValues),
                    state = state,
                    modelUiStateProvider = modelUiStateProvider,
                )
        }
    }

    SettingGeminiModelDialogHost(
        dialogState = state.modelDialogState,
        onEvent = onModelDialogEvent,
        selectedModelProvider = { state.model },
        uiStateProvider = modelUiStateProvider,
    )
}

@ScreenPreview
@Composable
private fun SettingGeminiScaffoldPreview(
    @PreviewParameter(SettingGeminiUiStatePreviewParameter::class) uiState: SettingGeminiUiState,
) {
    DiaryTheme {
        SettingGeminiScaffold(
            onEvent = {},
            onModelDialogEvent = {},
            state = rememberSettingGeminiFormState(initialSetting = previewGeminiSetting()),
            uiStateProvider = { uiState },
        )
    }
}

private class SettingGeminiUiStatePreviewParameter : PreviewParameterProvider<SettingGeminiUiState> {
    override val values: Sequence<SettingGeminiUiState> =
        sequenceOf(
            SettingGeminiUiState.Loading,
            SettingGeminiUiState.Loaded(setting = previewGeminiSetting()),
            SettingGeminiUiState.Loaded(setting = GeminiSetting.EMPTY),
            SettingGeminiUiState.Loaded(setting = GeminiSetting.EMPTY, isInProgress = true),
        )
}
