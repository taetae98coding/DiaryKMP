package io.github.taetae98coding.diary.feature.place.ui

import androidx.compose.ui.platform.AndroidUiDispatcher
import kotlin.coroutines.ContinuationInterceptor

/**
 * Robolectric은 테스트가 끝날 때 메인 스레드 대기열을 비우지만, Compose 화면 디스패처는 "이미 예약했다"는 표시와
 * 앞선 테스트가 남긴 작업을 그대로 들고 있어 뒤이은 테스트의 작업을 다시 예약하지 않는다.
 * 그러면 나누어 불러오는 목록의 결과가 화면에 전달되지 않으므로, 이런 목록을 그리는 테스트는 시작 전에 예약 상태를 비운다.
 */
internal fun resetAndroidUiDispatcher() {
    val dispatcher = AndroidUiDispatcher.Main[ContinuationInterceptor] as AndroidUiDispatcher
    val type = AndroidUiDispatcher::class.java
    val lock = type.getDeclaredField("lock").apply { isAccessible = true }.get(dispatcher)

    synchronized(lock) {
        (type.getDeclaredField("toRunTrampolined").apply { isAccessible = true }.get(dispatcher) as MutableCollection<*>).clear()
        (type.getDeclaredField("toRunOnFrame").apply { isAccessible = true }.get(dispatcher) as MutableCollection<*>).clear()
        type.getDeclaredField("scheduledTrampolineDispatch").apply { isAccessible = true }.setBoolean(dispatcher, false)
        type.getDeclaredField("scheduledFrameDispatch").apply { isAccessible = true }.setBoolean(dispatcher, false)
    }
}
