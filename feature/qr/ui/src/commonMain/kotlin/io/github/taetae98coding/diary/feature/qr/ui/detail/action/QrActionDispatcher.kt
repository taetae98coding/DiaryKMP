package io.github.taetae98coding.diary.feature.qr.ui.detail.action

import dev.zacsweers.metro.Qualifier

@Qualifier
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
public annotation class QrActionDispatcher
