package io.github.taetae98coding.diary.app.shared

import io.github.taetae98coding.diary.compose.credential.apple.AndroidAppleCredentialsManagerFactory
import io.github.taetae98coding.diary.compose.credential.apple.AppleCredentialsConfig
import io.github.taetae98coding.diary.compose.credential.apple.AppleCredentialsManagerFactory
import io.github.taetae98coding.diary.compose.credential.google.AndroidGoogleCredentialsManagerFactory
import io.github.taetae98coding.diary.compose.credential.google.GoogleCredentialsManagerFactory
import io.github.taetae98coding.diary.compose.map.DiaryMapContentOverride
import io.github.taetae98coding.diary.compose.map.PlatformDiaryMapContentOverride
import org.koin.core.annotation.Configuration
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single

@Module
@Configuration
internal class AndroidAppModule {
    @Single
    fun providesGoogleCredentialsManagerFactory(): GoogleCredentialsManagerFactory = AndroidGoogleCredentialsManagerFactory(serverClientId = BuildKonfig.GOOGLE_CREDENTIALS_SERVER_CLIENT_ID)

    @Single
    fun providesAppleCredentialsManagerFactory(config: AppleCredentialsConfig): AppleCredentialsManagerFactory = AndroidAppleCredentialsManagerFactory(config = config)

    @Single
    fun providesDiaryMapContentOverride(): DiaryMapContentOverride = PlatformDiaryMapContentOverride
}
