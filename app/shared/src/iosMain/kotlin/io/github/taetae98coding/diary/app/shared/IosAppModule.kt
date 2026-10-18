package io.github.taetae98coding.diary.app.shared

import io.github.taetae98coding.diary.compose.map.di.MapDispatcher
import io.github.taetae98coding.diary.feature.qr.ui.scan.QrScanCameraDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import org.koin.core.annotation.Configuration
import org.koin.core.annotation.Factory
import org.koin.core.annotation.Module

@Module
@Configuration
internal class IosAppModule {
    @Factory
    @MapDispatcher
    fun providesMapDispatcher(): CoroutineDispatcher = Dispatchers.Default

    @Factory
    @QrScanCameraDispatcher
    fun providesQrScanCameraDispatcher(): CoroutineDispatcher = Dispatchers.IO
}
