package io.github.taetae98coding.diary.feature.setting.ui.file

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.taetae98coding.diary.compose.core.effect.CollectEffect
import io.github.taetae98coding.diary.compose.core.snackbar.showImmediate
import io.github.taetae98coding.diary.feature.setting.ui.Res
import io.github.taetae98coding.diary.feature.setting.ui.file.picker.DirectoryPicker
import io.github.taetae98coding.diary.feature.setting.ui.setting_file_change_failed_message
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun SettingFileScreen(
    navigateUp: () -> Unit,
    componentVisibleProvider: () -> SettingFileScaffoldComponentVisible,
    directoryPicker: DirectoryPicker,
    viewModel: SettingFileViewModel,
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    SettingFileChangeFailedEffect(
        effect = viewModel.effect,
        snackbarHostState = snackbarHostState,
    )

    SettingFileScaffold(
        onEvent = { event ->
            when (event) {
                is SettingFileScaffoldEvent.ClickNavigateUp -> navigateUp()
                is SettingFileScaffoldEvent.ClickChooseDirectory -> directoryPicker.open()
                is SettingFileScaffoldEvent.ClickResetDirectory -> viewModel.unsetDirectory()
            }
        },
        modifier = modifier,
        snackbarHostState = snackbarHostState,
        uiStateProvider = { uiState },
        componentVisibleProvider = componentVisibleProvider,
    )
}

@Composable
private fun SettingFileChangeFailedEffect(
    effect: Flow<SettingFileEffect>,
    snackbarHostState: SnackbarHostState,
) {
    val coroutineScope = rememberCoroutineScope()
    val message = stringResource(Res.string.setting_file_change_failed_message)

    CollectEffect(effect) { value ->
        when (value) {
            is SettingFileEffect.ChangeFailed -> coroutineScope.launch { snackbarHostState.showImmediate(message = message) }
        }
    }
}
