package io.github.taetae98coding.diary.feature.setting.ui.browser

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
internal fun SettingBrowserScreen(
    navigateUp: () -> Unit,
    componentVisibleProvider: () -> SettingBrowserScaffoldComponentVisible,
    viewModel: SettingBrowserViewModel,
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    SettingBrowserScaffold(
        onEvent = { event ->
            when (event) {
                is SettingBrowserScaffoldEvent.ClickNavigateUp -> {
                    navigateUp()
                }

                is SettingBrowserScaffoldEvent.SelectProfile -> {
                    if (event.directory.isEmpty()) {
                        viewModel.unselectProfile()
                    } else {
                        viewModel.selectProfile(directory = event.directory)
                    }
                }
            }
        },
        modifier = modifier,
        uiStateProvider = { uiState },
        componentVisibleProvider = componentVisibleProvider,
    )
}
