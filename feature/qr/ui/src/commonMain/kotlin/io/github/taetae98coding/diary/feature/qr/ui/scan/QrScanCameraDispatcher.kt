package io.github.taetae98coding.diary.feature.qr.ui.scan

import org.koin.core.annotation.Qualifier

@Qualifier
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
public annotation class QrScanCameraDispatcher
