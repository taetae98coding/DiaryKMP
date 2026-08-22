package io.github.taetae98coding.diary.feature.setting.ui.holiday

import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.taetae98coding.diary.compose.core.preview.ComponentPreview
import io.github.taetae98coding.diary.compose.core.textfield.DiarySearchInputField
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.feature.setting.ui.Res
import io.github.taetae98coding.diary.feature.setting.ui.setting_holiday_search_placeholder
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun SettingHolidaySearchInputField(
    modifier: Modifier = Modifier,
    state: SettingHolidayScaffoldState = rememberSettingHolidayScaffoldState(),
) {
    DiarySearchInputField(
        placeholder = stringResource(Res.string.setting_holiday_search_placeholder),
        modifier = modifier,
        state = state.queryState,
    )
}

@ComponentPreview
@Composable
private fun SettingHolidaySearchInputFieldPreview() {
    DiaryTheme {
        Surface {
            SettingHolidaySearchInputField(
                state = rememberSettingHolidayScaffoldState(queryState = rememberTextFieldState(initialText = "신정")),
            )
        }
    }
}
