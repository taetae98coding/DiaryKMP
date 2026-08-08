package io.github.taetae98coding.diary.domain.holiday.usecase

import io.github.taetae98coding.diary.core.model.holiday.Holiday
import io.github.taetae98coding.diary.domain.core.FlowUseCase
import io.github.taetae98coding.diary.domain.holiday.model.toHolidayKey
import io.github.taetae98coding.diary.domain.holiday.repository.HolidayRepository
import io.github.taetae98coding.diary.domain.holiday.repository.HolidaySettingRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import org.koin.core.annotation.Factory

@Factory
public class GetCalendarHolidayUseCase internal constructor(
    private val holidayRepository: HolidayRepository,
    private val holidaySettingRepository: HolidaySettingRepository,
) : FlowUseCase<Int, List<Holiday>>() {
    override fun execute(parameter: Int): Flow<Result<List<Holiday>>> =
        combine(
            holidayRepository.get(year = parameter),
            holidaySettingRepository.getHiddenKeySet(),
        ) { holidayList, hiddenKeySet ->
            Result.success(holidayList.filterNot { holiday -> holiday.name.toHolidayKey() in hiddenKeySet })
        }
}
