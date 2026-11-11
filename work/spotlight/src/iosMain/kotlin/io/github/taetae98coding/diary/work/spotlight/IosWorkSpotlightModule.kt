package io.github.taetae98coding.diary.work.spotlight

import io.github.taetae98coding.diary.core.database.api.spotlight.datasource.SpotlightLocalDataSource
import io.github.taetae98coding.diary.domain.account.usecase.GetAccountUseCase
import io.github.taetae98coding.diary.library.coroutines.scope.workCoroutineScope
import io.github.taetae98coding.diary.work.spotlight.di.SpotlightScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import org.koin.core.annotation.Configuration
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single
import platform.CoreSpotlight.CSSearchableIndex

@Module
@Configuration
public class IosWorkSpotlightModule {
    @Single
    @SpotlightScope
    internal fun providesSpotlightCoroutineScope(): CoroutineScope = workCoroutineScope(dispatcher = Dispatchers.Default)

    @Single
    internal fun providesSpotlightIndex(): SpotlightIndex =
        CoreSpotlightIndex(
            searchableIndex = CSSearchableIndex.defaultSearchableIndex(),
            iconRenderer = SpotlightIconRenderer(),
            dispatcher = Dispatchers.Default,
        )

    @Single
    internal fun providesSpotlightWorkLauncher(
        getAccountUseCase: GetAccountUseCase,
        spotlightLocalDataSource: SpotlightLocalDataSource,
        spotlightIndex: SpotlightIndex,
        @SpotlightScope scope: CoroutineScope,
    ): SpotlightWorkLauncher =
        SpotlightWorkLauncher(
            work =
                SpotlightWork(
                    getAccountUseCase = getAccountUseCase,
                    spotlightLocalDataSource = spotlightLocalDataSource,
                    spotlightIndex = spotlightIndex,
                ),
            scope = scope,
        )
}
