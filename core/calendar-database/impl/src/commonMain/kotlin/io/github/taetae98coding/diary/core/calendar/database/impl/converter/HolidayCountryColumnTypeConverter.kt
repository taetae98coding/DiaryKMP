package io.github.taetae98coding.diary.core.calendar.database.impl.converter

import androidx.room3.ColumnTypeConverter
import io.github.taetae98coding.diary.core.calendar.database.api.entity.HolidayCountryLocalEntity

internal class HolidayCountryColumnTypeConverter {
    @ColumnTypeConverter
    fun countryToText(country: HolidayCountryLocalEntity): String = country.persistentValue

    @ColumnTypeConverter
    fun textToCountry(value: String): HolidayCountryLocalEntity = requireNotNull(HolidayCountryLocalEntity.fromPersistentValue(value)) { "Unknown holiday country: $value" }
}
