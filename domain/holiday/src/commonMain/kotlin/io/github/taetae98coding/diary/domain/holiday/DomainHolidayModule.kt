package io.github.taetae98coding.diary.domain.holiday

import io.github.taetae98coding.diary.domain.holiday.di.HolidayDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Configuration
import org.koin.core.annotation.Factory
import org.koin.core.annotation.Module

@Module
@Configuration
@ComponentScan
public class DomainHolidayModule {
    @Factory
    @HolidayDispatcher
    internal fun providesHolidayDispatcher(): CoroutineDispatcher = Dispatchers.Default
}
