package io.github.taetae98coding.diary.domain.holiday.usecase

import io.github.taetae98coding.diary.domain.core.UseCase
import io.github.taetae98coding.diary.domain.holiday.model.HolidayCountryOption
import io.github.taetae98coding.diary.domain.holiday.repository.HolidaySettingRepository
import kotlinx.coroutines.flow.first
import org.koin.core.annotation.Factory

@Factory
public class ToggleHolidayCountryOptionUseCase internal constructor(
    private val holidaySettingRepository: HolidaySettingRepository,
) : UseCase<HolidayCountryOption, Unit>() {
    override suspend fun execute(parameter: HolidayCountryOption) {
        if (parameter in holidaySettingRepository.getCountryOptionSet().first()) {
            holidaySettingRepository.removeCountryOption(option = parameter)
        } else {
            holidaySettingRepository.addCountryOption(option = parameter)
        }
    }
}
