package io.github.taetae98coding.diary.work.mcp

import io.github.taetae98coding.diary.library.coroutines.scope.workCoroutineScope
import io.github.taetae98coding.diary.work.mcp.di.McpScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Configuration
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single

@Module
@Configuration
@ComponentScan
public class WorkMcpModule {
    @Single
    @McpScope
    internal fun providesMcpCoroutineScope(): CoroutineScope = workCoroutineScope(dispatcher = Dispatchers.Default)
}
