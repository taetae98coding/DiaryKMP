package io.github.taetae98coding.diary.work.fileupload.di

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.BindingContainer
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Provides
import io.github.taetae98coding.diary.work.fileupload.report.AndroidFileUploadNotifier
import io.github.taetae98coding.diary.work.fileupload.report.FileUploadNotifier
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

@BindingContainer
@ContributesTo(AppScope::class)
public object AndroidWorkFileUploadModule {
    @Provides
    @FileUploadTextDispatcher
    internal fun providesFileUploadTextDispatcher(): CoroutineDispatcher = Dispatchers.IO

    @Provides
    internal fun providesFileUploadNotifier(notifier: AndroidFileUploadNotifier): FileUploadNotifier = notifier
}
