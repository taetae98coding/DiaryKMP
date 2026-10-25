package io.github.taetae98coding.diary.feature.setting.ui.holiday

import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import io.github.taetae98coding.diary.compose.core.appbar.DiaryTopBar
import io.github.taetae98coding.diary.compose.core.preview.ComponentPreview
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.feature.setting.ui.Res
import io.github.taetae98coding.diary.feature.setting.ui.setting_holiday_search_placeholder
import io.github.taetae98coding.diary.feature.setting.ui.setting_navigate_up_button_content_description
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun SettingHolidayTopBar(
    onEvent: (SettingHolidayScaffoldEvent) -> Unit,
    modifier: Modifier = Modifier,
    state: SettingHolidayScaffoldState = rememberSettingHolidayScaffoldState(),
    componentVisibleProvider: () -> SettingHolidayScaffoldComponentVisible = { SettingHolidayScaffoldComponentVisible() },
) {
    val placeholder = stringResource(Res.string.setting_holiday_search_placeholder)
    val navigateUpContentDescription = stringResource(Res.string.setting_navigate_up_button_content_description)
    val focusRequester = remember { FocusRequester() }

    DiaryTopBar(
        title = {
            SearchTitle(
                state = state.queryState,
                placeholder = placeholder,
                focusRequester = focusRequester,
            )
        },
        modifier = modifier,
        navigationIcon = {
            if (componentVisibleProvider().isNavigateUpButtonVisible) {
                NavigateUp(
                    onClick = { onEvent(SettingHolidayScaffoldEvent.ClickNavigateUp) },
                    contentDescription = navigateUpContentDescription,
                )
            }
        },
    )
}

@ComponentPreview
@Composable
private fun SettingHolidayTopBarPreview() {
    DiaryTheme {
        SettingHolidayTopBar(
            onEvent = {},
            state = rememberSettingHolidayScaffoldState(queryState = rememberTextFieldState(initialText = "신정")),
        )
    }
}
