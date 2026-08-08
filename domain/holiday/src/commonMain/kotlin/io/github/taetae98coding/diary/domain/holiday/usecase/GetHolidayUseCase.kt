package io.github.taetae98coding.diary.domain.holiday.usecase

import io.github.taetae98coding.diary.core.model.holiday.Holiday
import io.github.taetae98coding.diary.domain.core.FlowUseCase
import io.github.taetae98coding.diary.domain.holiday.repository.HolidayRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.koin.core.annotation.Factory

@Factory
public class GetHolidayUseCase internal constructor(
    private val holidayRepository: HolidayRepository,
) : FlowUseCase<Int, List<Holiday>>() {
    override fun execute(parameter: Int): Flow<Result<List<Holiday>>> =
        holidayRepository
            .get(year = parameter)
            .map { holidayList -> Result.success(holidayList) }
}
