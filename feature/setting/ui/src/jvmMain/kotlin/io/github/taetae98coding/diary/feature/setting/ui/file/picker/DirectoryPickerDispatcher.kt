package io.github.taetae98coding.diary.feature.setting.ui.file.picker

import org.koin.core.annotation.Qualifier

@Qualifier
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
public annotation class DirectoryPickerDispatcher
