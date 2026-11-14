package io.github.taetae98coding.diary.feature.qr.ui

import io.github.taetae98coding.diary.feature.qr.ui.di.QrImageDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Configuration
import org.koin.core.annotation.Factory
import org.koin.core.annotation.Module

@Module
@ComponentScan
@Configuration
public class FeatureQrModule {
    @Factory
    @QrImageDispatcher
    internal fun providesQrImageDispatcher(): CoroutineDispatcher = Dispatchers.Default
}
