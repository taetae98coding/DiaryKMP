package io.github.taetae98coding.diary.app.shared.initializer

import dev.zacsweers.metro.createGraph
import io.github.taetae98coding.diary.app.shared.di.JvmAppGraph

public object StartupInitializer {
    public fun initialize(isDebug: Boolean): JvmAppGraph {
        // 등록 전에 발생한 로그는 남지 않으므로, 로그를 남길 수 있는 어떤 구성 요소보다 기록 수단을 먼저 등록한다.
        LoggerInitializer.initialize(isDebug = isDebug)
        val graph = createGraph<JvmAppGraph>()

        // 지도 화면에 들어갈 때마다 서버를 띄우지 않도록 화면을 그리기 전에 한 번 띄워 두고 앱이 끝날 때까지 쓴다.
        graph.mapHttpServer.start()
        // 연결은 창이 다시 만들어져도 이어져야 하므로 화면 구성과 무관한 앱 시작 시점에 한 번 띄운다.
        graph.mcpServerLauncher.launch()

        return graph
    }
}
