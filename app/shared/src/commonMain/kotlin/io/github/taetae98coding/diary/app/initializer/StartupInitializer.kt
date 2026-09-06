package io.github.taetae98coding.diary.app.initializer

public object StartupInitializer {
    public fun initialize(isDebug: Boolean) {
        KoinInitializer.initialize()
        LoggerInitializer.initialize(isDebug = isDebug)
    }
}
