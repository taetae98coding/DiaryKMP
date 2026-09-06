package io.github.taetae98coding.diary.app.initializer

import io.github.taetae98coding.diary.app.di.startKoin

public object KoinInitializer {
    public fun initialize() {
        startKoin(configuration = {})
    }
}
