package io.github.taetae98coding.diary.work.mcp

import io.github.taetae98coding.diary.work.mcp.di.McpServerDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import org.koin.core.annotation.Configuration
import org.koin.core.annotation.Factory
import org.koin.core.annotation.Module

@Module
@Configuration
public class JvmWorkMcpModule {
    @Factory
    @McpServerDispatcher
    internal fun providesMcpServerDispatcher(): CoroutineDispatcher = Dispatchers.IO
}
