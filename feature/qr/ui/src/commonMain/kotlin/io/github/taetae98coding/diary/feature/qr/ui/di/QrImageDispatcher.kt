package io.github.taetae98coding.diary.feature.qr.ui.di

import dev.zacsweers.metro.Qualifier

@Qualifier
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
internal annotation class QrImageDispatcher
