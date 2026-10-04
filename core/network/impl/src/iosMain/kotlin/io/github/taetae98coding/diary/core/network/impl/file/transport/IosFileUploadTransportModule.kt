package io.github.taetae98coding.diary.core.network.impl.file.transport

import io.github.taetae98coding.diary.core.network.impl.di.FileUploadDispatcher
import io.github.taetae98coding.diary.core.supabase.api.SupabaseFunction
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import org.koin.core.annotation.Configuration
import org.koin.core.annotation.Factory
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single

@Module
@Configuration
public class IosFileUploadTransportModule {
    @Factory
    @FileUploadDispatcher
    internal fun providesFileUploadDispatcher(): CoroutineDispatcher = Dispatchers.IO

    // 시스템의 백그라운드 전송 세션은 식별자마다 하나만 있어야 이벤트가 한곳으로 모인다.
    @Single
    internal fun providesFileUploadTransport(
        supabaseFunction: SupabaseFunction,
        @FileUploadDispatcher dispatcher: CoroutineDispatcher,
    ): FileUploadTransport =
        BackgroundSessionFileUploadTransport(
            supabaseFunction = supabaseFunction,
            dispatcher = dispatcher,
        )
}
