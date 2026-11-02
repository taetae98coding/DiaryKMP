package io.github.taetae98coding.diary.compose.credential

import org.koin.core.annotation.Qualifier

@Qualifier
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
public annotation class GoogleCredentialsClientId
