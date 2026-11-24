package io.github.taetae98coding.diary.core.supabase.impl

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.DependencyGraph
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.createGraphFactory
import io.github.jan.supabase.SupabaseClient
import io.github.taetae98coding.diary.core.supabase.impl.di.SupabaseHttpClientEngine
import io.ktor.client.engine.HttpClientEngine

@DependencyGraph(scope = AppScope::class, excludes = [SupabaseHttpClientEngineModule::class])
internal interface SupabaseTestGraph {
    val supabaseClient: SupabaseClient

    @DependencyGraph.Factory
    fun interface Factory {
        fun create(
            @Provides config: SupabaseConfig,
            @Provides @SupabaseHttpClientEngine engine: HttpClientEngine,
        ): SupabaseTestGraph
    }
}

internal fun createTestSupabaseClient(engine: HttpClientEngine): SupabaseClient =
    createGraphFactory<SupabaseTestGraph.Factory>()
        .create(
            config =
                SupabaseConfig(
                    url = "https://example.supabase.co",
                    key = "test-key",
                ),
            engine = engine,
        ).supabaseClient
