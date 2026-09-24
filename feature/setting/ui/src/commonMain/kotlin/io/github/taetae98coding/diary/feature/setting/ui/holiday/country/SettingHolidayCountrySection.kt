package io.github.taetae98coding.diary.feature.setting.ui.holiday.country

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.taetae98coding.diary.compose.core.listitem.DiaryCheckableSegmentedListItem
import io.github.taetae98coding.diary.compose.core.listitem.DiarySegmentedListItemDefaults
import io.github.taetae98coding.diary.compose.core.preview.ComponentPreview
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.core.model.holiday.HolidayCountry
import io.github.taetae98coding.diary.domain.holiday.model.HolidayCountryOption
import io.github.taetae98coding.diary.domain.holiday.model.HolidayCountrySetting
import io.github.taetae98coding.diary.feature.setting.ui.Res
import io.github.taetae98coding.diary.feature.setting.ui.previewHolidayCountrySetting
import io.github.taetae98coding.diary.feature.setting.ui.setting_holiday_country_device
import io.github.taetae98coding.diary.feature.setting.ui.setting_holiday_country_korea
import io.github.taetae98coding.diary.feature.setting.ui.setting_holiday_country_title
import io.github.taetae98coding.diary.feature.setting.ui.setting_holiday_country_united_states
import io.github.taetae98coding.diary.feature.setting.ui.setting_holiday_country_unsupported_region
import org.jetbrains.compose.resources.stringResource

internal val settingHolidayCountryOptionList: List<HolidayCountryOption> =
    listOf(
        HolidayCountryOption.DEVICE,
        HolidayCountryOption.KOREA,
        HolidayCountryOption.UNITED_STATES,
    )

@Composable
internal fun SettingHolidayCountrySection(
    countrySetting: HolidayCountrySetting,
    onToggle: (HolidayCountryOption) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(DiaryTheme.dimens.itemSpacing),
    ) {
        Text(
            text = stringResource(Res.string.setting_holiday_country_title),
            style = DiaryTheme.typography.titleMedium,
        )

        Column(verticalArrangement = Arrangement.spacedBy(DiarySegmentedListItemDefaults.Gap)) {
            settingHolidayCountryOptionList.forEachIndexed { index, option ->
                DiaryCheckableSegmentedListItem(
                    onCheckedChange = { onToggle(option) },
                    checked = option in countrySetting.selectedOptionSet,
                    index = index,
                    count = settingHolidayCountryOptionList.size,
                    supportingContent =
                        if (option == HolidayCountryOption.DEVICE) {
                            { Text(text = countrySetting.deviceCountry.deviceLabel()) }
                        } else {
                            null
                        },
                ) {
                    Text(text = option.label())
                }
            }
        }
    }
}

@Composable
private fun HolidayCountryOption.label(): String =
    when (this) {
        HolidayCountryOption.DEVICE -> stringResource(Res.string.setting_holiday_country_device)
        HolidayCountryOption.KOREA -> stringResource(Res.string.setting_holiday_country_korea)
        HolidayCountryOption.UNITED_STATES -> stringResource(Res.string.setting_holiday_country_united_states)
    }

@Composable
private fun HolidayCountry?.deviceLabel(): String =
    when (this) {
        HolidayCountry.KOREA -> stringResource(Res.string.setting_holiday_country_korea)
        HolidayCountry.UNITED_STATES -> stringResource(Res.string.setting_holiday_country_united_states)
        null -> stringResource(Res.string.setting_holiday_country_unsupported_region)
    }

@ComponentPreview
@Composable
private fun SettingHolidayCountrySectionPreview() {
    DiaryTheme {
        SettingHolidayCountrySection(
            countrySetting = previewHolidayCountrySetting(),
            onToggle = {},
        )
    }
}
