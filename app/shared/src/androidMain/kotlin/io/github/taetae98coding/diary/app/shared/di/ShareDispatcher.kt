package io.github.taetae98coding.diary.app.shared.di

import dev.zacsweers.metro.Qualifier

@Qualifier
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
internal annotation class ShareDispatcher
