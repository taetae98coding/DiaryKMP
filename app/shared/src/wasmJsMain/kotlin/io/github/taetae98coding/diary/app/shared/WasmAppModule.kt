package io.github.taetae98coding.diary.app.shared

import io.github.taetae98coding.diary.compose.credential.apple.AppleCredentialsConfig
import io.github.taetae98coding.diary.compose.credential.apple.AppleCredentialsManagerFactory
import io.github.taetae98coding.diary.compose.credential.apple.WasmAppleCredentialsManagerFactory
import io.github.taetae98coding.diary.compose.credential.google.GoogleCredentialsManagerFactory
import io.github.taetae98coding.diary.compose.credential.google.WasmGoogleCredentialsManagerFactory
import io.github.taetae98coding.diary.compose.map.DiaryMapContentOverride
import io.github.taetae98coding.diary.compose.map.PlatformDiaryMapContentOverride
import org.koin.core.annotation.Configuration
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single

@Module
@Configuration
internal class WasmAppModule {
    @Single
    fun providesGoogleCredentialsManagerFactory(): GoogleCredentialsManagerFactory = WasmGoogleCredentialsManagerFactory(clientId = BuildKonfig.GOOGLE_CREDENTIALS_CLIENT_ID)

    @Single
    fun providesAppleCredentialsManagerFactory(config: AppleCredentialsConfig): AppleCredentialsManagerFactory = WasmAppleCredentialsManagerFactory(config = config)

    @Single
    fun providesDiaryMapContentOverride(): DiaryMapContentOverride = PlatformDiaryMapContentOverride
}
