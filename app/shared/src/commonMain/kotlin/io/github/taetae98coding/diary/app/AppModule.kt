package io.github.taetae98coding.diary.app

import io.github.taetae98coding.diary.core.google.network.impl.GooglePlacesConfig
import io.github.taetae98coding.diary.core.naver.network.impl.NaverOpenApiConfig
import io.github.taetae98coding.diary.core.supabase.impl.SupabaseConfig
import io.github.taetae98coding.diary.core.weather.network.impl.OpenWeatherApiConfig
import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Configuration
import org.koin.core.annotation.Factory
import org.koin.core.annotation.Module
import kotlin.time.Clock

@Module
@ComponentScan
@Configuration
internal class AppModule {
    @Factory
    fun providesSupabaseConfig(): SupabaseConfig =
        SupabaseConfig(
            url = BuildKonfig.SUPABASE_URL,
            key = BuildKonfig.SUPABASE_KEY,
        )

    @Factory
    fun providesOpenWeatherApiConfig(): OpenWeatherApiConfig = OpenWeatherApiConfig(appId = BuildKonfig.OPEN_WEATHER_APP_ID)

    @Factory
    fun providesGooglePlacesConfig(): GooglePlacesConfig = GooglePlacesConfig(apiKey = BuildKonfig.GOOGLE_PLACES_API_KEY)

    @Factory
    fun providesNaverOpenApiConfig(): NaverOpenApiConfig =
        NaverOpenApiConfig(
            clientId = BuildKonfig.NAVER_OPEN_API_CLIENT_ID,
            clientSecret = BuildKonfig.NAVER_OPEN_API_CLIENT_SECRET,
        )

    @Factory
    fun providesClock(): Clock = Clock.System
}
