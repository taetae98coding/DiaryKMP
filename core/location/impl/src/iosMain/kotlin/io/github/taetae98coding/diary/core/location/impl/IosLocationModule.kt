package io.github.taetae98coding.diary.core.location.impl

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.BindingContainer
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Provides
import io.github.taetae98coding.diary.core.location.impl.di.LocationDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO

@BindingContainer
@ContributesTo(AppScope::class)
public object IosLocationModule {
    @Provides
    @LocationDispatcher
    internal fun providesLocationDispatcher(): CoroutineDispatcher = Dispatchers.IO
}
