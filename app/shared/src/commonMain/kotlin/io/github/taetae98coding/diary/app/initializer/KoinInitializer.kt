package io.github.taetae98coding.diary.app.initializer

import io.github.taetae98coding.diary.app.di.DiaryKoinApplication
import org.koin.core.KoinApplication
import org.koin.plugin.module.dsl.startKoin

internal object KoinInitializer {
    fun initialize(configuration: KoinApplication.() -> Unit = {}): KoinApplication =
        startKoin<DiaryKoinApplication> {
            configuration()
        }
}
