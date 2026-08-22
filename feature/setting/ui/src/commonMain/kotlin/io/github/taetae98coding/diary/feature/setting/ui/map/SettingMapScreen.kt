package io.github.taetae98coding.diary.feature.setting.ui.map

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.compose.viewmodel.koinViewModel

@Composable
internal fun SettingMapScreen(
    navigateUp: () -> Unit,
    componentVisibleProvider: () -> SettingMapScaffoldComponentVisible,
    viewModel: SettingMapViewModel,
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    SettingMapScaffold(
        uiStateProvider = { uiState },
        onEvent = { event ->
            when (event) {
                is SettingMapScaffoldEvent.ClickNavigateUp -> {
                    navigateUp()
                }

                is SettingMapScaffoldEvent.SelectDefaultProvider -> {
                    viewModel.selectDefaultProvider(provider = event.provider)
                }
            }
        },
        modifier = modifier,
        componentVisibleProvider = componentVisibleProvider,
    )
}
