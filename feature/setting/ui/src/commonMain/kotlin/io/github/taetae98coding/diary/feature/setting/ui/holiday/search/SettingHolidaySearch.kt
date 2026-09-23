package io.github.taetae98coding.diary.feature.setting.ui.holiday.search

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import io.github.taetae98coding.diary.domain.holiday.model.HolidaySetting
import io.github.taetae98coding.diary.domain.holiday.model.toHolidayKey

internal fun matchesSettingHolidaySearch(
    query: String,
    displayName: String,
): Boolean {
    val normalizedQuery = query.toHolidayKey()

    return normalizedQuery.isEmpty() || displayName.toHolidayKey().contains(other = normalizedQuery, ignoreCase = true)
}

@Composable
internal fun rememberSettingHolidaySearchResult(
    query: String,
    holidaySettingList: List<HolidaySetting>,
): List<HolidaySetting> =
    remember(query, holidaySettingList) {
        holidaySettingList.filter { holidaySetting ->
            matchesSettingHolidaySearch(
                query = query,
                displayName = holidaySetting.name,
            )
        }
    }
