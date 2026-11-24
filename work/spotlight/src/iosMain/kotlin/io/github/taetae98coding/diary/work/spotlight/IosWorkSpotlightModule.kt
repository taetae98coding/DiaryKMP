package io.github.taetae98coding.diary.work.spotlight

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.BindingContainer
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.Dispatchers
import platform.CoreSpotlight.CSSearchableIndex

@BindingContainer
@ContributesTo(AppScope::class)
public object IosWorkSpotlightModule {
    @Provides
    @SingleIn(AppScope::class)
    internal fun providesSpotlightIndex(): SpotlightIndex =
        CoreSpotlightIndex(
            searchableIndex = CSSearchableIndex.defaultSearchableIndex(),
            iconRenderer = SpotlightIconRenderer(),
            dispatcher = Dispatchers.Default,
        )
}
