package io.github.taetae98coding.diary.core.network.impl.file.transport

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.BindingContainer
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn
import io.github.taetae98coding.diary.core.network.impl.di.FileUploadDispatcher
import io.github.taetae98coding.diary.core.network.impl.di.FileUploadScope
import io.github.taetae98coding.diary.core.supabase.api.SupabaseFunction
import io.github.taetae98coding.diary.library.coroutines.scope.workCoroutineScope
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO

@BindingContainer
@ContributesTo(AppScope::class)
public object IosFileUploadTransportModule {
    @Provides
    @FileUploadDispatcher
    internal fun providesFileUploadDispatcher(): CoroutineDispatcher = Dispatchers.IO

    @Provides
    @SingleIn(AppScope::class)
    @FileUploadScope
    internal fun providesFileUploadScope(
        @FileUploadDispatcher dispatcher: CoroutineDispatcher,
    ): CoroutineScope = workCoroutineScope(dispatcher = dispatcher)

    // 시스템의 백그라운드 전송 세션은 식별자마다 하나만 있어야 이벤트가 한곳으로 모인다.
    @Provides
    @SingleIn(AppScope::class)
    internal fun providesFileUploadTransport(
        supabaseFunction: SupabaseFunction,
        @FileUploadDispatcher dispatcher: CoroutineDispatcher,
        @FileUploadScope scope: CoroutineScope,
    ): FileUploadTransport =
        BackgroundSessionFileUploadTransport(
            supabaseFunction = supabaseFunction,
            dispatcher = dispatcher,
            scope = scope,
        )
}
