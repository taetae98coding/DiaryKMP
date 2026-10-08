package io.github.taetae98coding.diary.core.location.impl

import io.github.taetae98coding.diary.core.location.impl.di.LocationDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Configuration
import org.koin.core.annotation.Factory
import org.koin.core.annotation.Module

@Module
@ComponentScan
@Configuration
public class IosLocationModule {
    @Factory
    @LocationDispatcher
    internal fun providesLocationDispatcher(): CoroutineDispatcher = Dispatchers.IO
}
