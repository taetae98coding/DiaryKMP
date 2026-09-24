package io.github.taetae98coding.diary.feature.setting.ui.holiday.list

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import io.github.taetae98coding.diary.compose.core.preview.ScreenPreview
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.domain.holiday.model.HolidayCountrySetting
import io.github.taetae98coding.diary.domain.holiday.model.HolidaySetting
import io.github.taetae98coding.diary.feature.setting.ui.Res
import io.github.taetae98coding.diary.feature.setting.ui.holiday.SettingHolidayScaffoldEvent
import io.github.taetae98coding.diary.feature.setting.ui.holiday.country.SettingHolidayCountrySection
import io.github.taetae98coding.diary.feature.setting.ui.previewHolidayCountrySetting
import io.github.taetae98coding.diary.feature.setting.ui.previewHolidaySettingList
import io.github.taetae98coding.diary.feature.setting.ui.setting_holiday_list_title
import org.jetbrains.compose.resources.stringResource

private const val HOLIDAY_ITEM_KEY_PREFIX = "holiday:"
private const val HEADER_ITEM_KEY = "header"

@Composable
internal fun HolidaySettingList(
    onEvent: (SettingHolidayScaffoldEvent) -> Unit,
    modifier: Modifier = Modifier,
    countrySetting: HolidayCountrySetting? = null,
    holidaySettingList: List<HolidaySetting> = emptyList(),
    bottomPadding: Dp = DiaryTheme.dimens.screenVerticalPadding,
) {
    LazyColumn(
        modifier = modifier,
        contentPadding =
            PaddingValues(
                start = DiaryTheme.dimens.screenHorizontalPadding,
                top = DiaryTheme.dimens.screenVerticalPadding,
                end = DiaryTheme.dimens.screenHorizontalPadding,
                bottom = bottomPadding,
            ),
        verticalArrangement = Arrangement.spacedBy(DiaryTheme.dimens.itemSpacing),
    ) {
        if (countrySetting != null) {
            item(key = HEADER_ITEM_KEY) {
                HolidaySettingListHeader(
                    countrySetting = countrySetting,
                    onEvent = onEvent,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }

        items(
            items = holidaySettingList,
            key = { holidaySetting -> "$HOLIDAY_ITEM_KEY_PREFIX${holidaySetting.key}" },
        ) { holidaySetting ->
            SettingHolidayItemRow(
                holidaySetting = holidaySetting,
                onClick = {
                    onEvent(
                        SettingHolidayScaffoldEvent.ToggleHoliday(
                            key = holidaySetting.key,
                        ),
                    )
                },
                modifier =
                    Modifier
                        .animateItem()
                        .fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun HolidaySettingListHeader(
    countrySetting: HolidayCountrySetting,
    onEvent: (SettingHolidayScaffoldEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        SettingHolidayCountrySection(
            countrySetting = countrySetting,
            onToggle = { option -> onEvent(SettingHolidayScaffoldEvent.ToggleCountryOption(option = option)) },
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(modifier = Modifier.height(DiaryTheme.dimens.screenVerticalPadding))
        Text(
            text = stringResource(Res.string.setting_holiday_list_title),
            style = DiaryTheme.typography.titleMedium,
        )
    }
}

@ScreenPreview
@Composable
private fun HolidaySettingListPreview() {
    DiaryTheme {
        HolidaySettingList(
            onEvent = {},
            modifier = Modifier.fillMaxSize(),
            countrySetting = previewHolidayCountrySetting(),
            holidaySettingList = previewHolidaySettingList(),
        )
    }
}
