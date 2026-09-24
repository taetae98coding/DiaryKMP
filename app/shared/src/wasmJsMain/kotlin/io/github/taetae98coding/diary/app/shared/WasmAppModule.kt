package io.github.taetae98coding.diary.app.shared

import io.github.taetae98coding.diary.feature.login.ui.credential.AppleCredentialsConfig
import io.github.taetae98coding.diary.feature.login.ui.credential.GoogleCredentialsClientId
import org.koin.core.annotation.Configuration
import org.koin.core.annotation.Factory
import org.koin.core.annotation.Module

@Module
@Configuration
internal class WasmAppModule {
    @Factory
    @GoogleCredentialsClientId
    fun providesGoogleCredentialsClientId(): String = BuildKonfig.GOOGLE_CREDENTIALS_CLIENT_ID

    @Factory
    fun providesAppleCredentialsConfig(): AppleCredentialsConfig = appleCredentialsConfig(clientId = BuildKonfig.APPLE_CREDENTIALS_CLIENT_ID)
}
