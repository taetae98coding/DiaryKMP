package io.github.taetae98coding.diary

import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.singleWindowApplication
import io.github.taetae98coding.diary.app.shared.App
import io.github.taetae98coding.diary.app.shared.initializer.StartupInitializer
import java.awt.Dimension

// 목록과 상세를 좌우로 함께 표시하는 창 너비로 연다. compact 너비로 열면 목록·상세 배치가 한 영역만 표시한다.
private const val WIDTH = 1080
private const val HEIGHT = 920

// compact 너비까지 줄여 한 영역만 표시하는 배치도 확인할 수 있게 둔다.
private const val MIN_WIDTH = 360
private const val MIN_HEIGHT = 784

internal fun main() {
    // SwingPanel은 기본적으로 Compose 위에 그려져 지도 자리의 패널이 다이얼로그를 가린다.
    // 블렌딩을 켜야 지도 웹뷰를 감춘 동안 그 자리 위에 Compose 레이어가 보인다.
    System.setProperty("compose.interop.blending", "true")
    StartupInitializer.initialize(isDebug = true)

    singleWindowApplication(
        state = WindowState(size = DpSize(width = WIDTH.dp, height = HEIGHT.dp)),
        title = "Diary",
    ) {
        window.minimumSize = Dimension(MIN_WIDTH, MIN_HEIGHT)

        App()
    }
}
