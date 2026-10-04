package io.github.taetae98coding.diary.core.network.impl.file.transport

import io.github.taetae98coding.diary.core.supabase.api.SupabaseFunction
import org.koin.core.annotation.Configuration
import org.koin.core.annotation.Factory
import org.koin.core.annotation.Module

@Module
@Configuration
public class WasmFileUploadTransportModule {
    @Factory
    internal fun providesFileUploadTransport(supabaseFunction: SupabaseFunction): FileUploadTransport = SupabaseFunctionFileUploadTransport(supabaseFunction = supabaseFunction)
}
