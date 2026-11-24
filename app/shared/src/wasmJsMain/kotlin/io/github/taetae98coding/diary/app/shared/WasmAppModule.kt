package io.github.taetae98coding.diary.app.shared

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.BindingContainer
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn
import io.github.taetae98coding.diary.compose.credential.apple.AppleCredentialsConfig
import io.github.taetae98coding.diary.compose.credential.apple.AppleCredentialsManagerFactory
import io.github.taetae98coding.diary.compose.credential.apple.WasmAppleCredentialsManagerFactory
import io.github.taetae98coding.diary.compose.credential.google.GoogleCredentialsManagerFactory
import io.github.taetae98coding.diary.compose.credential.google.WasmGoogleCredentialsManagerFactory
import io.github.taetae98coding.diary.compose.map.DiaryMapContentOverride
import io.github.taetae98coding.diary.compose.map.PlatformDiaryMapContentOverride

@BindingContainer
@ContributesTo(AppScope::class)
internal object WasmAppModule {
    @Provides
    @SingleIn(AppScope::class)
    fun providesGoogleCredentialsManagerFactory(): GoogleCredentialsManagerFactory = WasmGoogleCredentialsManagerFactory(clientId = BuildKonfig.GOOGLE_CREDENTIALS_CLIENT_ID)

    @Provides
    @SingleIn(AppScope::class)
    fun providesAppleCredentialsManagerFactory(config: AppleCredentialsConfig): AppleCredentialsManagerFactory = WasmAppleCredentialsManagerFactory(config = config)

    @Provides
    @SingleIn(AppScope::class)
    fun providesDiaryMapContentOverride(): DiaryMapContentOverride = PlatformDiaryMapContentOverride
}
