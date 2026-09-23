package io.github.taetae98coding.diary.feature.setting.ui.holiday.search

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import io.github.taetae98coding.diary.compose.core.preview.ScreenPreview
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.feature.setting.ui.Res
import io.github.taetae98coding.diary.feature.setting.ui.setting_holiday_search_empty
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun SettingHolidaySearchEmpty(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center,
    ) {
        Text(text = stringResource(Res.string.setting_holiday_search_empty))
    }
}

@ScreenPreview
@Composable
private fun SettingHolidaySearchEmptyPreview() {
    DiaryTheme {
        Surface {
            SettingHolidaySearchEmpty(modifier = Modifier.fillMaxSize())
        }
    }
}
