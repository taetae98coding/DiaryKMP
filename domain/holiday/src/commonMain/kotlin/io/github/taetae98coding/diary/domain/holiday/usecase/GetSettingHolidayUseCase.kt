package io.github.taetae98coding.diary.domain.holiday.usecase

import io.github.taetae98coding.diary.domain.core.FlowUseCase
import io.github.taetae98coding.diary.domain.holiday.model.HolidaySetting
import io.github.taetae98coding.diary.domain.holiday.repository.HolidayRepository
import io.github.taetae98coding.diary.domain.holiday.repository.HolidaySettingRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import org.koin.core.annotation.Factory

@Factory
public class GetSettingHolidayUseCase internal constructor(
    private val holidayRepository: HolidayRepository,
    private val holidaySettingRepository: HolidaySettingRepository,
) : FlowUseCase<Unit, List<HolidaySetting>>() {
    override fun execute(parameter: Unit): Flow<Result<List<HolidaySetting>>> =
        combine(
            holidayRepository.get(),
            holidaySettingRepository.getHiddenKeySet(),
        ) { holidayList, hiddenKeySet ->
            Result.success(holidayList.toHolidaySettingList(hiddenKeySet = hiddenKeySet))
        }
}
