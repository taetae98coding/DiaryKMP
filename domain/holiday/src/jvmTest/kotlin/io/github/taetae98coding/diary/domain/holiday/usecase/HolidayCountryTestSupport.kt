package io.github.taetae98coding.diary.domain.holiday.usecase

import io.github.taetae98coding.diary.core.model.holiday.HolidayCountry
import io.github.taetae98coding.diary.domain.holiday.model.HolidayCountryOption
import io.github.taetae98coding.diary.domain.holiday.repository.DeviceCountryRepository
import io.github.taetae98coding.diary.domain.holiday.repository.HolidaySettingRepository
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

internal val KOREA_COUNTRY_SET: Set<HolidayCountry> = setOf(HolidayCountry.KOREA)

internal fun koreaCountrySettingUseCase(): GetHolidayCountrySettingUseCase = countrySettingUseCase(optionSet = setOf(HolidayCountryOption.KOREA))

internal fun countrySettingUseCase(
    optionSet: Set<HolidayCountryOption>,
    deviceCountry: HolidayCountry? = null,
): GetHolidayCountrySettingUseCase = countrySettingUseCase(optionSetFlow = flowOf(optionSet), deviceCountry = deviceCountry)

internal fun countrySettingUseCase(
    optionSetFlow: Flow<Set<HolidayCountryOption>>,
    deviceCountry: HolidayCountry? = null,
): GetHolidayCountrySettingUseCase =
    GetHolidayCountrySettingUseCase(
        holidaySettingRepository =
            mockk<HolidaySettingRepository>().also { repository ->
                every { repository.getCountryOptionSet() } returns optionSetFlow
            },
        deviceCountryRepository =
            mockk<DeviceCountryRepository>().also { repository ->
                every { repository.find() } returns deviceCountry
            },
    )
