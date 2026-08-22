@file:OptIn(ExperimentalMaterial3Api::class)

package io.github.taetae98coding.diary.feature.setting.ui.holiday

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.taetae98coding.diary.compose.core.button.NavigateUpButton
import io.github.taetae98coding.diary.compose.core.preview.ComponentPreview
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.feature.setting.ui.Res
import io.github.taetae98coding.diary.feature.setting.ui.setting_navigate_up_button_content_description
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun SettingHolidayTopBar(
    onEvent: (SettingHolidayScaffoldEvent) -> Unit,
    modifier: Modifier = Modifier,
    state: SettingHolidayScaffoldState = rememberSettingHolidayScaffoldState(),
    componentVisibleProvider: () -> SettingHolidayScaffoldComponentVisible = { SettingHolidayScaffoldComponentVisible() },
) {
    TopAppBar(
        title = { SettingHolidaySearchInputField(modifier = Modifier.fillMaxWidth(), state = state) },
        modifier = modifier,
        navigationIcon = {
            if (componentVisibleProvider().isNavigateUpButtonVisible) {
                NavigateUpButton(
                    onClick = { onEvent(SettingHolidayScaffoldEvent.ClickNavigateUp) },
                    contentDescription = stringResource(Res.string.setting_navigate_up_button_content_description),
                )
            }
        },
    )
}

@ComponentPreview
@Composable
private fun SettingHolidayTopBarPreview() {
    DiaryTheme {
        SettingHolidayTopBar(onEvent = {})
    }
}
