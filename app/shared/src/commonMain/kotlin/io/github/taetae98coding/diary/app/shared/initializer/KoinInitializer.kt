package io.github.taetae98coding.diary.app.shared.initializer

import io.github.taetae98coding.diary.app.shared.di.DiaryKoinApplication
import org.koin.core.KoinApplication
import org.koin.plugin.module.dsl.startKoin

internal object KoinInitializer {
    fun initialize(configuration: KoinApplication.() -> Unit = {}): KoinApplication =
        startKoin<DiaryKoinApplication> {
            configuration()
        }
}
