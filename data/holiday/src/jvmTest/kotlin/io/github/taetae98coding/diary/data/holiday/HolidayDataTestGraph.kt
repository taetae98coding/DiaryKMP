package io.github.taetae98coding.diary.data.holiday

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.DependencyGraph
import dev.zacsweers.metro.Provides
import io.github.taetae98coding.diary.core.calendar.database.api.datasource.HolidayLocalDataSource
import io.github.taetae98coding.diary.core.calendar.database.api.transaction.HolidayTransaction
import io.github.taetae98coding.diary.core.calendar.network.api.datasource.HolidayRemoteDataSource
import io.github.taetae98coding.diary.core.datastore.api.setting.datasource.HolidaySettingLocalDataSource
import io.github.taetae98coding.diary.domain.holiday.repository.HolidayRepository
import io.github.taetae98coding.diary.domain.holiday.usecase.FetchHolidayUseCase
import io.github.taetae98coding.diary.domain.holiday.usecase.ToggleHolidayCountryOptionUseCase

@DependencyGraph(scope = AppScope::class)
internal interface HolidayDataTestGraph {
    val holidayRepository: HolidayRepository
    val fetchHolidayUseCase: FetchHolidayUseCase
    val toggleHolidayCountryOptionUseCase: ToggleHolidayCountryOptionUseCase

    @DependencyGraph.Factory
    fun interface Factory {
        fun create(
            @Provides holidayRemoteDataSource: HolidayRemoteDataSource,
            @Provides holidayLocalDataSource: HolidayLocalDataSource,
            @Provides holidayTransaction: HolidayTransaction,
            @Provides holidaySettingLocalDataSource: HolidaySettingLocalDataSource,
        ): HolidayDataTestGraph
    }
}
