package io.github.taetae98coding.diary.domain.holiday.usecase

import io.github.taetae98coding.diary.domain.core.UseCase
import io.github.taetae98coding.diary.domain.holiday.model.toHolidayKey
import io.github.taetae98coding.diary.domain.holiday.repository.HolidayRepository
import io.github.taetae98coding.diary.domain.holiday.repository.HolidaySettingRepository
import kotlinx.coroutines.flow.first
import org.koin.core.annotation.Factory

@Factory
public class DeselectAllHolidayUseCase internal constructor(
    private val getHolidayCountrySettingUseCase: GetHolidayCountrySettingUseCase,
    private val holidayRepository: HolidayRepository,
    private val holidaySettingRepository: HolidaySettingRepository,
) : UseCase<Unit, Unit>() {
    override suspend fun execute(parameter: Unit) {
        val targetKeySet =
            holidayRepository
                .get(countrySet = getHolidayCountrySettingUseCase.countrySetFlow().first())
                .first()
                .mapTo(mutableSetOf()) { holiday -> holiday.name.toHolidayKey() }

        holidaySettingRepository.submitHiddenKeySet(
            hiddenKeySet = targetKeySet,
        )
    }
}
