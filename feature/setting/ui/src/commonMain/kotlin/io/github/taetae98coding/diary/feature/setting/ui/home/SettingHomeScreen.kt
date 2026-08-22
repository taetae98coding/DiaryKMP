package io.github.taetae98coding.diary.feature.setting.ui.home

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
internal fun SettingHomeScreen(
    navigateUp: () -> Unit,
    navigateToHoliday: () -> Unit,
    navigateToMap: () -> Unit,
    navigateToGemini: () -> Unit,
    modifier: Modifier = Modifier,
) {
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
                    }
                }
            }
        },
        modifier = modifier,
    )
}
