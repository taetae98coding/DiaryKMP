package io.github.taetae98coding.diary.core.supabase.impl

import io.github.taetae98coding.diary.core.supabase.impl.di.SupabaseHttpClientEngine
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.js.Js
import org.koin.core.annotation.Configuration
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single

@Module
@Configuration
public class WasmSupabaseModule {
    @Single
    @SupabaseHttpClientEngine
    internal fun providesSupabaseHttpClientEngine(): HttpClientEngine = Js.create()
}
