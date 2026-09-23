package io.github.taetae98coding.diary.feature.setting.ui.holiday.list

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import io.github.taetae98coding.diary.compose.core.preview.ScreenPreview
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.domain.holiday.model.HolidaySetting
import io.github.taetae98coding.diary.feature.setting.ui.holiday.SettingHolidayScaffoldEvent
import io.github.taetae98coding.diary.feature.setting.ui.previewHolidaySettingList

private const val HOLIDAY_ITEM_KEY_PREFIX = "holiday:"

@Composable
internal fun HolidaySettingList(
    onEvent: (SettingHolidayScaffoldEvent) -> Unit,
    modifier: Modifier = Modifier,
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

@ScreenPreview
@Composable
private fun HolidaySettingListPreview() {
    DiaryTheme {
        HolidaySettingList(
            onEvent = {},
            modifier = Modifier.fillMaxSize(),
            holidaySettingList = previewHolidaySettingList(),
        )
    }
}
