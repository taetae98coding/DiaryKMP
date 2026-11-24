package io.github.taetae98coding.diary.work.mcp.launcher

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import io.github.taetae98coding.diary.domain.core.invoke
import io.github.taetae98coding.diary.domain.mcp.usecase.RunMcpServerUseCase
import io.github.taetae98coding.diary.work.mcp.di.McpScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Inject
@SingleIn(AppScope::class)
public class McpServerLauncher internal constructor(
    @McpScope
    private val scope: CoroutineScope,
    private val runMcpServerUseCase: RunMcpServerUseCase,
) {
    private var isLaunched = false

    public fun launch() {
        if (isLaunched) return
        isLaunched = true

        scope.launch { runMcpServerUseCase() }
    }
}
