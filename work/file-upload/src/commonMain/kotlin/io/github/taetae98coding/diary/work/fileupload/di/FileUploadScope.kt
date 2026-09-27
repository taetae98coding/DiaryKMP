package io.github.taetae98coding.diary.work.fileupload.di

import org.koin.core.annotation.Qualifier

@Qualifier
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
internal annotation class FileUploadScope
