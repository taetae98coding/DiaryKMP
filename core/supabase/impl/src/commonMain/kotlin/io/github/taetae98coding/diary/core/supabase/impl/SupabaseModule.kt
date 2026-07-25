package io.github.taetae98coding.diary.core.supabase.impl

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.functions.Functions
import io.github.taetae98coding.diary.core.supabase.impl.di.SupabaseHttpClientEngine
import io.ktor.client.engine.HttpClientEngine
import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Configuration
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single

@Module
@ComponentScan
@Configuration
public class SupabaseModule {
    @Single
    internal fun providesSupabaseClient(
        config: SupabaseConfig,
        @SupabaseHttpClientEngine
        engine: HttpClientEngine,
    ): SupabaseClient =
        createSupabaseClient(
            supabaseUrl = config.url,
            supabaseKey = config.key,
        ) {
            httpEngine = engine
            install(Auth)
            install(Functions)
        }
}
