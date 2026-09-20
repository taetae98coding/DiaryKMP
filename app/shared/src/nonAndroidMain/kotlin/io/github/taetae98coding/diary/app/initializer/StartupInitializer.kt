package io.github.taetae98coding.diary.app.initializer

// Android는 androidx.startup이 같은 순서를 Initializer 의존성으로 표현하므로 이 진입점을 쓰지 않는다.
public object StartupInitializer {
    public fun initialize(isDebug: Boolean) {
        // 등록 전에 발생한 로그는 남지 않으므로, 로그를 남길 수 있는 어떤 구성 요소보다 기록 수단을 먼저 등록한다.
        LoggerInitializer.initialize(isDebug = isDebug)
        KoinInitializer.initialize()
        initializeSyncWork()
    }
}
