package io.github.taetae98coding.diary.domain.holiday

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.BindingContainer
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Provides
import io.github.taetae98coding.diary.domain.holiday.di.HolidayDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

@BindingContainer
@ContributesTo(AppScope::class)
public object DomainHolidayModule {
    @Provides
    @HolidayDispatcher
    internal fun providesHolidayDispatcher(): CoroutineDispatcher = Dispatchers.Default
}
