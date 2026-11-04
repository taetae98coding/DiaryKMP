@file:OptIn(SupabaseInternal::class)

package io.github.taetae98coding.diary.core.supabase.impl

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.annotations.SupabaseInternal
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.functions.Functions
import io.github.taetae98coding.diary.core.supabase.impl.di.SupabaseHttpClientEngine
import io.github.taetae98coding.diary.library.ktor.RemoteResponseFailureReport
import io.github.taetae98coding.diary.library.ktor.createPlatformHttpClientEngine
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
    @SupabaseHttpClientEngine
    internal fun providesSupabaseHttpClientEngine(): HttpClientEngine = createPlatformHttpClientEngine()

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
            // supabase-kt는 내부 HTTP 클라이언트에 플러그인을 더하는 공개 수단이 없어 내부 API로 표시된 httpConfig를 쓴다.
            httpConfig { install(RemoteResponseFailureReport) }
            install(Auth)
            install(Functions)
        }
}
