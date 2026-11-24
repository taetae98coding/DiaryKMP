package io.github.taetae98coding.diary.app.shared.initializer

import dev.zacsweers.metro.createGraph
import io.github.taetae98coding.diary.app.shared.di.IosAppGraph

public object StartupInitializer {
    public fun initialize(isDebug: Boolean): IosAppGraph {
        // 등록 전에 발생한 로그는 남지 않으므로, 로그를 남길 수 있는 어떤 구성 요소보다 기록 수단을 먼저 등록한다.
        LoggerInitializer.initialize(isDebug = isDebug)
        val graph = createGraph<IosAppGraph>()

        graph.periodicSyncBackgroundTaskRegistrar.register()
        graph.fileUploadWorkInitializer.initialize()
        graph.spotlightWorkLauncher.start()

        return graph
    }
}
