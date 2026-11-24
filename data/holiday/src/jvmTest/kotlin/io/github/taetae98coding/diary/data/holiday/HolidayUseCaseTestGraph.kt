package io.github.taetae98coding.diary.data.holiday

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.DependencyGraph
import dev.zacsweers.metro.Provides
import io.github.taetae98coding.diary.data.holiday.repository.HolidayRepositoryImpl
import io.github.taetae98coding.diary.data.holiday.repository.HolidaySettingRepositoryImpl
import io.github.taetae98coding.diary.domain.holiday.repository.HolidayRepository
import io.github.taetae98coding.diary.domain.holiday.repository.HolidaySettingRepository
import io.github.taetae98coding.diary.domain.holiday.usecase.GetCalendarHolidayUseCase

@DependencyGraph(scope = AppScope::class, excludes = [HolidayRepositoryImpl::class, HolidaySettingRepositoryImpl::class])
internal interface HolidayUseCaseTestGraph {
    val getCalendarHolidayUseCase: GetCalendarHolidayUseCase

    @DependencyGraph.Factory
    fun interface Factory {
        fun create(
            @Provides holidayRepository: HolidayRepository,
            @Provides holidaySettingRepository: HolidaySettingRepository,
        ): HolidayUseCaseTestGraph
    }
}
