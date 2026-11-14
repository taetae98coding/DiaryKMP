package io.github.taetae98coding.diary.app.shared

import io.github.taetae98coding.diary.compose.credential.apple.AppleCredentialsManagerFactory
import io.github.taetae98coding.diary.compose.credential.apple.IosAppleCredentialsManagerFactory
import io.github.taetae98coding.diary.compose.credential.google.GoogleCredentialsManagerFactory
import io.github.taetae98coding.diary.compose.credential.google.IosGoogleCredentialsManagerFactory
import io.github.taetae98coding.diary.compose.map.DiaryMapContentOverride
import io.github.taetae98coding.diary.compose.map.UiKitDiaryMapContentOverride
import io.github.taetae98coding.diary.feature.qr.ui.scan.QrScanCameraDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import org.koin.core.annotation.Configuration
import org.koin.core.annotation.Factory
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single

@Module
@Configuration
internal class IosAppModule {
    @Single
    fun providesDiaryMapContentOverride(): DiaryMapContentOverride = UiKitDiaryMapContentOverride(dispatcher = Dispatchers.Default)

    @Single
    fun providesGoogleCredentialsManagerFactory(): GoogleCredentialsManagerFactory = IosGoogleCredentialsManagerFactory

    @Single
    fun providesAppleCredentialsManagerFactory(): AppleCredentialsManagerFactory = IosAppleCredentialsManagerFactory

    @Factory
    @QrScanCameraDispatcher
    fun providesQrScanCameraDispatcher(): CoroutineDispatcher = Dispatchers.IO
}
