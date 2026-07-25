package io.github.taetae98coding.diary.core.supabase.impl

import io.github.taetae98coding.diary.core.supabase.impl.di.SupabaseHttpClientEngine
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.darwin.Darwin
import org.koin.core.annotation.Configuration
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single

@Module
@Configuration
public class IosSupabaseModule {
    @Single
    @SupabaseHttpClientEngine
    internal fun providesSupabaseHttpClientEngine(): HttpClientEngine = Darwin.create()
}
