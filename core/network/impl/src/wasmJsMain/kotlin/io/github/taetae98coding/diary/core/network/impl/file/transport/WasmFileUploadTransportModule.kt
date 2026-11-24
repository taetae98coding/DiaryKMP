package io.github.taetae98coding.diary.core.network.impl.file.transport

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.BindingContainer
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Provides
import io.github.taetae98coding.diary.core.supabase.api.SupabaseFunction

@BindingContainer
@ContributesTo(AppScope::class)
public object WasmFileUploadTransportModule {
    @Provides
    internal fun providesFileUploadTransport(supabaseFunction: SupabaseFunction): FileUploadTransport = SupabaseFunctionFileUploadTransport(supabaseFunction = supabaseFunction)
}
