@file:OptIn(ExperimentalNativeApi::class)

package io.github.taetae98coding.diary

import io.github.taetae98coding.diary.app.shared.initializer.StartupInitializer
import platform.Foundation.NSLock
import kotlin.experimental.ExperimentalNativeApi
import kotlin.native.Platform

public data object DiaryStartupInitializer {
    private val lock = NSLock()
    private var isStarted = false

    // 앱 시작과 화면 없이 실행된 앱 동작이 모두 부르므로, 어느 쪽이 먼저 불러도 한 번만 시작한다. Koin은 두 번 시작하면 실패한다.
    public fun start() {
        lock.lock()
        try {
            if (!isStarted) {
                StartupInitializer.initialize(isDebug = Platform.isDebugBinary)
                isStarted = true
            }
        } finally {
            lock.unlock()
        }
    }
}
