package io.github.taetae98coding.diary.app.shared.initializer

import io.github.taetae98coding.diary.compose.map.web.MapHttpServer
import io.github.taetae98coding.diary.work.mcp.launcher.McpServerLauncher
import org.koin.mp.KoinPlatform

internal actual fun initializePlatform() {
    val koin = KoinPlatform.getKoin()

    // 지도 화면에 들어갈 때마다 서버를 띄우지 않도록 화면을 그리기 전에 한 번 띄워 두고 앱이 끝날 때까지 쓴다.
    koin.get<MapHttpServer>().start()
    // 연결은 창이 다시 만들어져도 이어져야 하므로 화면 구성과 무관한 앱 시작 시점에 한 번 띄운다.
    koin.get<McpServerLauncher>().launch()
}
