package io.github.taetae98coding.diary.data.holiday.mapper

import io.github.taetae98coding.diary.core.datastore.api.setting.entity.HolidayCountryOptionLocalEntity
import io.github.taetae98coding.diary.domain.holiday.model.HolidayCountryOption

internal fun HolidayCountryOption.toLocal(): HolidayCountryOptionLocalEntity =
    when (this) {
        HolidayCountryOption.DEVICE -> HolidayCountryOptionLocalEntity.DEVICE
        HolidayCountryOption.KOREA -> HolidayCountryOptionLocalEntity.KOREA
        HolidayCountryOption.UNITED_STATES -> HolidayCountryOptionLocalEntity.UNITED_STATES
    }

internal fun HolidayCountryOptionLocalEntity.toDomain(): HolidayCountryOption =
    when (this) {
        HolidayCountryOptionLocalEntity.DEVICE -> HolidayCountryOption.DEVICE
        HolidayCountryOptionLocalEntity.KOREA -> HolidayCountryOption.KOREA
        HolidayCountryOptionLocalEntity.UNITED_STATES -> HolidayCountryOption.UNITED_STATES
    }
