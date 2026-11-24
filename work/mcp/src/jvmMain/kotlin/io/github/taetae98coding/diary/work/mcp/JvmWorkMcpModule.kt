package io.github.taetae98coding.diary.work.mcp

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.BindingContainer
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Provides
import io.github.taetae98coding.diary.work.mcp.di.McpServerDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

@BindingContainer
@ContributesTo(AppScope::class)
public object JvmWorkMcpModule {
    @Provides
    @McpServerDispatcher
    internal fun providesMcpServerDispatcher(): CoroutineDispatcher = Dispatchers.IO
}
