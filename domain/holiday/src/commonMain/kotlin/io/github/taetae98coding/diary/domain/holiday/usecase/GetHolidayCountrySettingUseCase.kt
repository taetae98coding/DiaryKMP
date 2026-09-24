package io.github.taetae98coding.diary.domain.holiday.usecase

import io.github.taetae98coding.diary.domain.core.FlowUseCase
import io.github.taetae98coding.diary.domain.holiday.model.HolidayCountrySetting
import io.github.taetae98coding.diary.domain.holiday.repository.DeviceCountryRepository
import io.github.taetae98coding.diary.domain.holiday.repository.HolidaySettingRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.koin.core.annotation.Factory

@Factory
public class GetHolidayCountrySettingUseCase internal constructor(
    private val holidaySettingRepository: HolidaySettingRepository,
    private val deviceCountryRepository: DeviceCountryRepository,
) : FlowUseCase<Unit, HolidayCountrySetting>() {
    override fun execute(parameter: Unit): Flow<Result<HolidayCountrySetting>> =
        holidaySettingRepository
            .getCountryOptionSet()
            .map { selectedOptionSet ->
                Result.success(
                    HolidayCountrySetting(
                        selectedOptionSet = selectedOptionSet,
                        deviceCountry = deviceCountryRepository.find(),
                    ),
                )
            }
}
