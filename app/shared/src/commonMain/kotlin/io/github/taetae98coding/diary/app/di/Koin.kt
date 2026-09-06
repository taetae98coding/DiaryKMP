package io.github.taetae98coding.diary.app.di

import org.koin.core.KoinApplication
import org.koin.plugin.module.dsl.startKoin

internal fun startKoin(configuration: KoinApplication.() -> Unit): KoinApplication =
    startKoin<DiaryKoinApplication> {
        configuration()
    }
