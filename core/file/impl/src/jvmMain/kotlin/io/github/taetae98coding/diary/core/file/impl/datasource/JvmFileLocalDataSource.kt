package io.github.taetae98coding.diary.core.file.impl.datasource

import io.github.taetae98coding.diary.core.file.api.datasource.FileLocalDataSource
import io.github.taetae98coding.diary.core.file.impl.di.FileDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import org.koin.core.annotation.Factory

@Factory
internal class JvmFileLocalDataSource(
    @FileDispatcher dispatcher: CoroutineDispatcher,
) : FileLocalDataSource by JavaFileLocalDataSource(dispatcher = dispatcher)
