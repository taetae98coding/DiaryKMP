package io.github.taetae98coding.diary.work.spotlight

import kotlinx.coroutines.Dispatchers
import org.koin.core.annotation.Configuration
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single
import platform.CoreSpotlight.CSSearchableIndex

@Module
@Configuration
public class IosWorkSpotlightModule {
    @Single
    internal fun providesSpotlightIndex(): SpotlightIndex =
        CoreSpotlightIndex(
            searchableIndex = CSSearchableIndex.defaultSearchableIndex(),
            iconRenderer = SpotlightIconRenderer(),
            dispatcher = Dispatchers.Default,
        )
}
