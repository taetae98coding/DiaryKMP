package io.github.taetae98coding.diary.core.file.impl.datasource

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import io.github.taetae98coding.diary.core.file.api.datasource.FileLocalDataSource
import io.github.taetae98coding.diary.core.file.impl.di.FileDispatcher
import kotlinx.coroutines.CoroutineDispatcher

@Inject
@ContributesBinding(AppScope::class)
internal class JvmFileLocalDataSource(
    @FileDispatcher dispatcher: CoroutineDispatcher,
) : FileLocalDataSource by JavaFileLocalDataSource(dispatcher = dispatcher)
