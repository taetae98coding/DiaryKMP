@file:OptIn(ExperimentalCoroutinesApi::class)

package io.github.taetae98coding.diary.domain.holiday.usecase

import io.github.taetae98coding.diary.domain.core.FlowUseCase
import io.github.taetae98coding.diary.domain.holiday.model.HolidaySetting
import io.github.taetae98coding.diary.domain.holiday.repository.HolidayRepository
import io.github.taetae98coding.diary.domain.holiday.repository.HolidaySettingRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import org.koin.core.annotation.Factory

@Factory
public class GetSettingHolidayUseCase internal constructor(
    private val getHolidayCountrySettingUseCase: GetHolidayCountrySettingUseCase,
    private val holidayRepository: HolidayRepository,
    private val holidaySettingRepository: HolidaySettingRepository,
) : FlowUseCase<Unit, List<HolidaySetting>>() {
    override fun execute(parameter: Unit): Flow<Result<List<HolidaySetting>>> =
        combine(
            getHolidayCountrySettingUseCase
                .countrySetFlow()
                .flatMapLatest { countrySet -> holidayRepository.get(countrySet = countrySet) },
            holidaySettingRepository.getHiddenKeySet(),
        ) { holidayList, hiddenKeySet ->
            Result.success(holidayList.toHolidaySettingList(hiddenKeySet = hiddenKeySet))
        }
}
