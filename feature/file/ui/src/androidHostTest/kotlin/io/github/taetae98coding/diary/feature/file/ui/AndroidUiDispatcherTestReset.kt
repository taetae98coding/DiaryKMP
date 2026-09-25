package io.github.taetae98coding.diary.feature.file.ui

import androidx.compose.ui.platform.AndroidUiDispatcher
import kotlin.coroutines.ContinuationInterceptor

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
