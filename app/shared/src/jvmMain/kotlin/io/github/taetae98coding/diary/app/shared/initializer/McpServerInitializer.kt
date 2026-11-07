package io.github.taetae98coding.diary.app.shared.initializer

import io.github.taetae98coding.diary.work.mcp.launcher.McpServerLauncher
import org.koin.mp.KoinPlatform

// 연결은 창이 다시 만들어져도 이어져야 하므로 화면 구성과 무관한 앱 시작 시점에 한 번 띄운다.
internal actual fun initializeMcpServer() {
    KoinPlatform.getKoin().get<McpServerLauncher>().launch()
}
