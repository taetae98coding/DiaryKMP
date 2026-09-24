package io.github.taetae98coding.diary.feature.setting.ui.holiday

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.compose.viewmodel.koinViewModel

@Composable
internal fun SettingHolidayScreen(
    navigateUp: () -> Unit,
    componentVisibleProvider: () -> SettingHolidayScaffoldComponentVisible,
    viewModel: SettingHolidayViewModel,
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    SettingHolidayScaffold(
        uiStateProvider = { uiState },
        onEvent = { event ->
            when (event) {
                is SettingHolidayScaffoldEvent.ClickNavigateUp -> navigateUp()
                is SettingHolidayScaffoldEvent.ClickSelectAll -> viewModel.selectAll()
                is SettingHolidayScaffoldEvent.ClickDeselectAll -> viewModel.deselectAll()
                is SettingHolidayScaffoldEvent.ClickSelectDaysOff -> viewModel.selectDaysOff()
                is SettingHolidayScaffoldEvent.ToggleHoliday -> viewModel.toggleHoliday(name = event.name)
                is SettingHolidayScaffoldEvent.ToggleCountryOption -> viewModel.toggleCountryOption(option = event.option)
            }
        },
        modifier = modifier,
        componentVisibleProvider = componentVisibleProvider,
    )
}
