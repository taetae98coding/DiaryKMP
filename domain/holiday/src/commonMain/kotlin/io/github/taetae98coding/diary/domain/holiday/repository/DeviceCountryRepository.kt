package io.github.taetae98coding.diary.domain.holiday.repository

import io.github.taetae98coding.diary.core.model.holiday.HolidayCountry

public interface DeviceCountryRepository {
    public fun find(): HolidayCountry?
}
