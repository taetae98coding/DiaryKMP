package io.github.taetae98coding.diary.domain.holiday.usecase

import io.github.taetae98coding.diary.core.model.holiday.Holiday
import io.github.taetae98coding.diary.domain.holiday.model.HolidaySetting
import io.github.taetae98coding.diary.domain.holiday.model.toHolidayKey

internal fun List<Holiday>.toHolidaySettingList(hiddenKeySet: Set<String>): List<HolidaySetting> =
    groupBy { holiday -> holiday.name.toHolidayKey() }
        .map { (key, holidayList) ->
            val representative =
                holidayList.minWith(
                    compareByDescending<Holiday> { holiday -> holiday.dateRange.start }
                        .thenBy { holiday -> holiday.name },
                )

            HolidaySetting(
                key = key,
                name = representative.name,
                isHoliday = holidayList.any { holiday -> holiday.isHoliday },
                isVisible = key !in hiddenKeySet,
            )
        }.sortedWith(
            compareBy<HolidaySetting> { setting -> setting.name }
                .thenBy { setting -> setting.key },
        )
