package io.github.taetae98coding.diary.app.shared.di

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.DependencyGraph
import io.github.taetae98coding.diary.compose.map.web.MapHttpServer
import io.github.taetae98coding.diary.domain.web.usecase.SaveWebLoginUseCase
import io.github.taetae98coding.diary.work.mcp.launcher.McpServerLauncher

@DependencyGraph(AppScope::class)
public interface JvmAppGraph : AppGraph {
    public val mapHttpServer: MapHttpServer

    public val mcpServerLauncher: McpServerLauncher

    public val saveWebLoginUseCase: SaveWebLoginUseCase
}
