package io.github.taetae98coding.diary.feature.setting.ui.gemini.model

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.CircularWavyProgressIndicator
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import io.github.taetae98coding.diary.compose.core.dialog.DiaryPickerDialog
import io.github.taetae98coding.diary.compose.core.icon.RefreshIcon
import io.github.taetae98coding.diary.compose.core.preview.ScreenPreview
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.core.model.gemini.GeminiModel
import io.github.taetae98coding.diary.feature.setting.ui.Res
import io.github.taetae98coding.diary.feature.setting.ui.setting_gemini_model_empty_message
import io.github.taetae98coding.diary.feature.setting.ui.setting_gemini_model_fetch_failed_message
import io.github.taetae98coding.diary.feature.setting.ui.setting_gemini_model_invalid_api_key_message
import io.github.taetae98coding.diary.feature.setting.ui.setting_gemini_model_loading_content_description
import io.github.taetae98coding.diary.feature.setting.ui.setting_gemini_model_picker_title
import io.github.taetae98coding.diary.feature.setting.ui.setting_gemini_model_reload_button_content_description
import io.github.taetae98coding.diary.feature.setting.ui.setting_gemini_model_retry_action
import org.jetbrains.compose.resources.stringResource

private val MessageBoxMinHeight = 120.dp
private val ReloadProgressIndicatorSize = 24.dp

@Composable
internal fun SettingGeminiModelDialog(
    onDismissRequest: () -> Unit,
    onEvent: (SettingGeminiModelDialogEvent) -> Unit,
    modifier: Modifier = Modifier,
    selectedModelProvider: () -> String = { "" },
    uiStateProvider: () -> SettingGeminiModelUiState = { SettingGeminiModelUiState() },
) {
    DiaryPickerDialog(
        title = stringResource(Res.string.setting_gemini_model_picker_title),
        onDismissRequest = onDismissRequest,
        modifier = modifier,
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            ReloadRow(
                onEvent = onEvent,
                modifier = Modifier.fillMaxWidth(),
                uiStateProvider = uiStateProvider,
            )
            Content(
                onEvent = onEvent,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .weight(weight = 1F, fill = false),
                selectedModelProvider = selectedModelProvider,
                uiStateProvider = uiStateProvider,
            )
        }
    }
}

@Composable
private fun ReloadRow(
    onEvent: (SettingGeminiModelDialogEvent) -> Unit,
    modifier: Modifier = Modifier,
    uiStateProvider: () -> SettingGeminiModelUiState = { SettingGeminiModelUiState() },
) {
    val uiState = uiStateProvider()
    val loadingDescription = stringResource(Res.string.setting_gemini_model_loading_content_description)

    Box(
        modifier = modifier,
        contentAlignment = Alignment.CenterEnd,
    ) {
        if (uiState.isInProgress && uiState.isLoaded) {
            CircularWavyProgressIndicator(
                modifier =
                    Modifier
                        .padding(12.dp)
                        .size(ReloadProgressIndicatorSize)
                        .semantics { contentDescription = loadingDescription },
            )
        } else {
            IconButton(onClick = { onEvent(SettingGeminiModelDialogEvent.ClickReload) }) {
                RefreshIcon(contentDescription = stringResource(Res.string.setting_gemini_model_reload_button_content_description))
            }
        }
    }
}

@Composable
private fun Content(
    onEvent: (SettingGeminiModelDialogEvent) -> Unit,
    modifier: Modifier = Modifier,
    selectedModelProvider: () -> String = { "" },
    uiStateProvider: () -> SettingGeminiModelUiState = { SettingGeminiModelUiState() },
) {
    val uiState = uiStateProvider()
    val failure = uiState.failure

    when {
        uiState.isInProgress && !uiState.isLoaded -> LoadingBox(modifier = modifier)

        !uiState.isLoaded && failure != null ->
            FailureBox(
                onEvent = onEvent,
                modifier = modifier,
                failure = failure,
            )

        uiState.isLoaded && uiState.modelList.isEmpty() ->
            MessageBox(
                message = stringResource(Res.string.setting_gemini_model_empty_message),
                modifier = modifier,
            )

        else ->
            ModelList(
                onEvent = onEvent,
                modifier = modifier,
                selectedModelProvider = selectedModelProvider,
                modelList = uiState.modelList,
            )
    }
}

@Composable
private fun LoadingBox(modifier: Modifier = Modifier) {
    val loadingDescription = stringResource(Res.string.setting_gemini_model_loading_content_description)

    Box(
        modifier = modifier.heightIn(min = MessageBoxMinHeight),
        contentAlignment = Alignment.Center,
    ) {
        CircularWavyProgressIndicator(
            modifier = Modifier.semantics { contentDescription = loadingDescription },
        )
    }
}

@Composable
private fun FailureBox(
    onEvent: (SettingGeminiModelDialogEvent) -> Unit,
    modifier: Modifier = Modifier,
    failure: SettingGeminiModelFailure = SettingGeminiModelFailure.UNKNOWN,
) {
    val message =
        when (failure) {
            SettingGeminiModelFailure.INVALID_API_KEY -> stringResource(Res.string.setting_gemini_model_invalid_api_key_message)
            SettingGeminiModelFailure.UNKNOWN -> stringResource(Res.string.setting_gemini_model_fetch_failed_message)
        }

    Box(
        modifier = modifier.heightIn(min = MessageBoxMinHeight),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(DiaryTheme.dimens.itemSpacing),
        ) {
            Text(
                text = message,
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodyMedium,
            )
            TextButton(onClick = { onEvent(SettingGeminiModelDialogEvent.ClickReload) }) {
                Text(text = stringResource(Res.string.setting_gemini_model_retry_action))
            }
        }
    }
}

@Composable
private fun MessageBox(
    message: String,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.heightIn(min = MessageBoxMinHeight),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = message,
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}

@Composable
private fun ModelList(
    onEvent: (SettingGeminiModelDialogEvent) -> Unit,
    modifier: Modifier = Modifier,
    selectedModelProvider: () -> String = { "" },
    modelList: List<GeminiModel> = emptyList(),
) {
    val selectedModel = selectedModelProvider()

    LazyColumn(
        modifier = modifier.selectableGroup(),
        verticalArrangement = Arrangement.spacedBy(DiaryTheme.dimens.itemSpacing),
    ) {
        items(
            items = modelList,
            key = { model -> model.id },
        ) { model ->
            SettingGeminiModelItem(
                model = model,
                onClick = { onEvent(SettingGeminiModelDialogEvent.SelectModel(id = model.id)) },
                modifier = Modifier.fillMaxWidth(),
                selected = model.id == selectedModel,
            )
        }
    }
}

@ScreenPreview
@Composable
private fun SettingGeminiModelDialogPreview() {
    DiaryTheme {
        SettingGeminiModelDialog(
            onDismissRequest = {},
            onEvent = {},
            selectedModelProvider = { "models/gemini-3.6-flash" },
            uiStateProvider = {
                SettingGeminiModelUiState(
                    isLoaded = true,
                    modelList =
                        listOf(
                            GeminiModel(id = "models/gemini-3.6-flash", displayName = "Gemini 3.6 Flash", description = "빠른 범용 모델"),
                            GeminiModel(id = "models/gemini-3.6-pro", displayName = "Gemini 3.6 Pro", description = ""),
                        ),
                )
            },
        )
    }
}
