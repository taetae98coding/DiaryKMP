package io.github.taetae98coding.diary.work.fileupload

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.BindingContainer
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn
import io.github.taetae98coding.diary.library.coroutines.scope.workCoroutineScope
import io.github.taetae98coding.diary.work.fileupload.di.FileUploadScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

@BindingContainer
@ContributesTo(AppScope::class)
public object WorkFileUploadModule {
    @Provides
    @SingleIn(AppScope::class)
    @FileUploadScope
    internal fun providesFileUploadCoroutineScope(): CoroutineScope = workCoroutineScope(dispatcher = Dispatchers.Default)
}
