@file:OptIn(ExperimentalCoroutinesApi::class)

package io.github.taetae98coding.diary.domain.holiday.usecase

import io.github.taetae98coding.diary.core.model.holiday.Holiday
import io.github.taetae98coding.diary.domain.core.FlowUseCase
import io.github.taetae98coding.diary.domain.holiday.model.toHolidayKey
import io.github.taetae98coding.diary.domain.holiday.repository.HolidayRepository
import io.github.taetae98coding.diary.domain.holiday.repository.HolidaySettingRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import org.koin.core.annotation.Factory

@Factory
public class GetCalendarHolidayUseCase internal constructor(
    private val getHolidayCountrySettingUseCase: GetHolidayCountrySettingUseCase,
    private val holidayRepository: HolidayRepository,
    private val holidaySettingRepository: HolidaySettingRepository,
) : FlowUseCase<Int, List<Holiday>>() {
    override fun execute(parameter: Int): Flow<Result<List<Holiday>>> =
        combine(
            getHolidayCountrySettingUseCase
                .countrySetFlow()
                .flatMapLatest { countrySet -> holidayRepository.get(countrySet = countrySet, year = parameter) },
            holidaySettingRepository.getHiddenKeySet(),
        ) { holidayList, hiddenKeySet ->
            Result.success(holidayList.filterNot { holiday -> holiday.name.toHolidayKey() in hiddenKeySet })
        }
}
