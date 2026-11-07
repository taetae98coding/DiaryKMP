package io.github.taetae98coding.diary.feature.setting.ui.mcp

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch

@Composable
internal fun SettingMcpScreen(
    navigateUp: () -> Unit,
    componentVisibleProvider: () -> SettingMcpScaffoldComponentVisible,
    viewModel: SettingMcpViewModel,
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val addressCopier = rememberSettingMcpAddressCopier(snackbarHostState = snackbarHostState)
    val coroutineScope = rememberCoroutineScope()

    SettingMcpScaffold(
        onEvent = { event ->
            when (event) {
                is SettingMcpScaffoldEvent.ClickNavigateUp -> navigateUp()
                is SettingMcpScaffoldEvent.ChangeEnabled -> viewModel.changeEnabled(isEnabled = event.isEnabled)
                is SettingMcpScaffoldEvent.ClickCopyAddress -> coroutineScope.launch { addressCopier.copy(address = event.address) }
            }
        },
        modifier = modifier,
        snackbarHostState = snackbarHostState,
        uiStateProvider = { uiState },
        componentVisibleProvider = componentVisibleProvider,
    )
}
