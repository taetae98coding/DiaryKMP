@file:OptIn(ExperimentalNativeApi::class)

package io.github.taetae98coding.diary

import io.github.taetae98coding.diary.app.shared.di.IosAppGraph
import io.github.taetae98coding.diary.app.shared.initializer.StartupInitializer
import platform.Foundation.NSLock
import kotlin.experimental.ExperimentalNativeApi
import kotlin.native.Platform

public data object DiaryStartupInitializer {
    private val lock = NSLock()
    private var appGraph: IosAppGraph? = null

    // Compose 루트와 앱 동작, 딥링크가 쓰는 그래프다. 앱 동작처럼 화면 없이 먼저 불려도 시작이 끝난 뒤 돌려준다.
    internal val graph: IosAppGraph
        get() {
            start()

            return checkNotNull(appGraph)
        }

    // 앱 시작과 화면 없이 실행된 앱 동작이 모두 부르므로, 어느 쪽이 먼저 불러도 한 번만 시작한다. 그래프를 두 번 만들면 싱글턴과 시작 작업이 둘로 나뉜다.
    public fun start() {
        lock.lock()
        try {
            if (appGraph == null) {
                appGraph = StartupInitializer.initialize(isDebug = Platform.isDebugBinary)
            }
        } finally {
            lock.unlock()
        }
    }
}
