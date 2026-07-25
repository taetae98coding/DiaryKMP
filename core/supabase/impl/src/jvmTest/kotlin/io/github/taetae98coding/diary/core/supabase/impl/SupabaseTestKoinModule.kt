package io.github.taetae98coding.diary.core.supabase.impl

import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Configuration
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single

@Module
@ComponentScan
@Configuration
internal data object SupabaseTestKoinModule {
    @Single
    fun providedSupabaseConfig(): SupabaseConfig =
        SupabaseConfig(
            url = "https://example.supabase.co",
            key = "test-key",
        )
}
