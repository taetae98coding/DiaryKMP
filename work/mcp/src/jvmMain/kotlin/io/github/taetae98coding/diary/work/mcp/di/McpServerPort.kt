package io.github.taetae98coding.diary.work.mcp.di

import dev.zacsweers.metro.Qualifier

@Qualifier
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
public annotation class McpServerPort
