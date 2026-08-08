package io.github.taetae98coding.diary.domain.holiday.usecase

import io.github.taetae98coding.diary.domain.core.UseCase
import io.github.taetae98coding.diary.domain.holiday.model.toHolidayKey
import io.github.taetae98coding.diary.domain.holiday.repository.HolidayRepository
import io.github.taetae98coding.diary.domain.holiday.repository.HolidaySettingRepository
import kotlinx.coroutines.flow.first
import org.koin.core.annotation.Factory

@Factory
public class SelectDaysOffHolidayUseCase internal constructor(
    private val holidayRepository: HolidayRepository,
    private val holidaySettingRepository: HolidaySettingRepository,
) : UseCase<Unit, Unit>() {
    override suspend fun execute(parameter: Unit) {
        val hiddenKeySet =
            holidayRepository
                .get()
                .first()
                .groupBy { holiday -> holiday.name.toHolidayKey() }
                .filterValues { holidayList -> holidayList.none { holiday -> holiday.isHoliday } }
                .keys

        holidaySettingRepository.submitHiddenKeySet(
            hiddenKeySet = hiddenKeySet,
        )
    }
}
