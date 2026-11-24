package io.github.taetae98coding.diary.app.shared

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.BindingContainer
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn
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

@BindingContainer
@ContributesTo(AppScope::class)
internal object IosAppModule {
    @Provides
    @SingleIn(AppScope::class)
    fun providesDiaryMapContentOverride(): DiaryMapContentOverride = UiKitDiaryMapContentOverride(dispatcher = Dispatchers.Default)

    @Provides
    @SingleIn(AppScope::class)
    fun providesGoogleCredentialsManagerFactory(): GoogleCredentialsManagerFactory = IosGoogleCredentialsManagerFactory

    @Provides
    @SingleIn(AppScope::class)
    fun providesAppleCredentialsManagerFactory(): AppleCredentialsManagerFactory = IosAppleCredentialsManagerFactory

    @Provides
    @QrScanCameraDispatcher
    fun providesQrScanCameraDispatcher(): CoroutineDispatcher = Dispatchers.IO
}
