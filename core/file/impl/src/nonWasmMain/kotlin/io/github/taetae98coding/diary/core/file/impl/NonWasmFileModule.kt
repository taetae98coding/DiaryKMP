package io.github.taetae98coding.diary.core.file.impl

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.BindingContainer
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Provides
import io.github.taetae98coding.diary.core.file.impl.di.FileDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO

@BindingContainer
@ContributesTo(AppScope::class)
public object NonWasmFileModule {
    @Provides
    @FileDispatcher
    internal fun providesFileDispatcher(): CoroutineDispatcher = Dispatchers.IO
}
