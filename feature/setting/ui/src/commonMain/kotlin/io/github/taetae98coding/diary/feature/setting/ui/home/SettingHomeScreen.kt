package io.github.taetae98coding.diary.feature.setting.ui.home

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
internal fun SettingHomeScreen(
    navigateUp: () -> Unit,
    navigateToHoliday: () -> Unit,
    navigateToMap: () -> Unit,
    navigateToGemini: () -> Unit,
    navigateToBrowser: () -> Unit,
    viewModel: SettingHomeViewModel,
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    SettingHomeScaffold(
        onEvent = { event ->
            when (event) {
                is SettingHomeScaffoldEvent.ClickNavigateUp -> {
                    navigateUp()
                }

                is SettingHomeScaffoldEvent.ClickItem -> {
                    when (event.item) {
                        SettingHomeItem.HOLIDAY -> navigateToHoliday()
                        SettingHomeItem.MAP -> navigateToMap()
                        SettingHomeItem.GEMINI -> navigateToGemini()
                        SettingHomeItem.BROWSER -> navigateToBrowser()
                    }
                }
            }
        },
        modifier = modifier,
        uiStateProvider = { uiState },
    )
}
