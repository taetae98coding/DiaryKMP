package io.github.taetae98coding.diary.domain.holiday.usecase

import io.github.taetae98coding.diary.core.model.holiday.Holiday
import io.github.taetae98coding.diary.domain.holiday.model.HolidaySetting

internal fun List<Holiday>.toHolidaySettingList(hiddenKeySet: Set<String>): List<HolidaySetting> =
    groupBy { holiday -> holiday.name }
        .map { (name, holidayList) ->
            HolidaySetting(
                name = name,
                isHoliday = holidayList.any { holiday -> holiday.isHoliday },
                isVisible = name !in hiddenKeySet,
            )
        }.sortedBy { setting -> setting.name }
