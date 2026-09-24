package io.github.taetae98coding.diary.domain.holiday.model

import io.github.taetae98coding.diary.core.model.holiday.HolidayCountry

public data class HolidayCountrySetting(
    val selectedOptionSet: Set<HolidayCountryOption>,
    val deviceCountry: HolidayCountry?,
) {
    val countrySet: Set<HolidayCountry> =
        selectedOptionSet.mapNotNullTo(mutableSetOf()) { option ->
            when (option) {
                HolidayCountryOption.DEVICE -> deviceCountry
                HolidayCountryOption.KOREA -> HolidayCountry.KOREA
                HolidayCountryOption.UNITED_STATES -> HolidayCountry.UNITED_STATES
            }
        }
}
