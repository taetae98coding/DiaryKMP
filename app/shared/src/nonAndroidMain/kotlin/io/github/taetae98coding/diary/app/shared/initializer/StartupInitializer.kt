package io.github.taetae98coding.diary.app.shared.initializer

import io.github.taetae98coding.diary.app.shared.AppDependencies

// Android는 androidx.startup이 같은 순서를 Initializer 의존성으로 표현하므로 이 진입점을 쓰지 않는다.
public object StartupInitializer {
    public fun initialize(isDebug: Boolean): AppDependencies {
        // 등록 전에 발생한 로그는 남지 않으므로, 로그를 남길 수 있는 어떤 구성 요소보다 기록 수단을 먼저 등록한다.
        LoggerInitializer.initialize(isDebug = isDebug)
        val koin = KoinInitializer.initialize().koin
        initializeSyncWork()
        initializeFileUpload()
        initializeMapHttpServer()
        initializeMcpServer()
        initializeSpotlight()

        return koin.get<AppDependencies>()
    }
}
