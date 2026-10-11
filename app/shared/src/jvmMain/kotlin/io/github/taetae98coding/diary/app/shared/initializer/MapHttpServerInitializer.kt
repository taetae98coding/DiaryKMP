package io.github.taetae98coding.diary.app.shared.initializer

import io.github.taetae98coding.diary.compose.map.web.MapHttpServer
import org.koin.mp.KoinPlatform

// 지도 화면에 들어갈 때마다 서버를 띄우지 않도록 화면을 그리기 전에 한 번 띄워 두고 앱이 끝날 때까지 쓴다.
internal actual fun initializeMapHttpServer() {
    KoinPlatform.getKoin().get<MapHttpServer>().start()
}
