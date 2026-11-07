package io.github.taetae98coding.diary.app.shared

import io.github.taetae98coding.diary.compose.credential.google.GoogleCredentialsServerClientId
import org.koin.core.annotation.Configuration
import org.koin.core.annotation.Factory
import org.koin.core.annotation.Module

@Module
@Configuration
internal class AndroidAppModule {
    @Factory
    @GoogleCredentialsServerClientId
    fun providesGoogleCredentialsServerClientId(): String = BuildKonfig.GOOGLE_CREDENTIALS_SERVER_CLIENT_ID
}
