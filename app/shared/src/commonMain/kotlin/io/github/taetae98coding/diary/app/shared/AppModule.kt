package io.github.taetae98coding.diary.app.shared

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.BindingContainer
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Provides
import io.github.taetae98coding.diary.compose.credential.apple.AppleCredentialsConfig
import io.github.taetae98coding.diary.core.google.network.impl.GooglePlacesConfig
import io.github.taetae98coding.diary.core.naver.network.impl.NaverOpenApiConfig
import io.github.taetae98coding.diary.core.supabase.impl.SupabaseConfig
import io.github.taetae98coding.diary.core.weather.network.impl.OpenWeatherApiConfig
import kotlin.time.Clock

@BindingContainer
@ContributesTo(AppScope::class)
internal object AppModule {
    @Provides
    fun providesSupabaseConfig(): SupabaseConfig =
        SupabaseConfig(
            url = BuildKonfig.SUPABASE_URL,
            key = BuildKonfig.SUPABASE_KEY,
        )

    @Provides
    fun providesOpenWeatherApiConfig(): OpenWeatherApiConfig = OpenWeatherApiConfig(appId = BuildKonfig.OPEN_WEATHER_APP_ID)

    @Provides
    fun providesGooglePlacesConfig(): GooglePlacesConfig = GooglePlacesConfig(apiKey = BuildKonfig.GOOGLE_PLACES_API_KEY)

    @Provides
    fun providesNaverOpenApiConfig(): NaverOpenApiConfig =
        NaverOpenApiConfig(
            clientId = BuildKonfig.NAVER_OPEN_API_CLIENT_ID,
            clientSecret = BuildKonfig.NAVER_OPEN_API_CLIENT_SECRET,
        )

    @Provides
    fun providesAppleCredentialsConfig(): AppleCredentialsConfig = appleCredentialsConfig(clientId = BuildKonfig.APPLE_CREDENTIALS_CLIENT_ID)

    @Provides
    fun providesClock(): Clock = Clock.System
}
