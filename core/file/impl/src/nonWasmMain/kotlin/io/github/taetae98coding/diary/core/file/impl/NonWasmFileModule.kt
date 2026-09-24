package io.github.taetae98coding.diary.core.file.impl

import io.github.taetae98coding.diary.core.file.impl.di.FileDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import org.koin.core.annotation.Configuration
import org.koin.core.annotation.Factory
import org.koin.core.annotation.Module

@Module
@Configuration
public class NonWasmFileModule {
    @Factory
    @FileDispatcher
    internal fun providesFileDispatcher(): CoroutineDispatcher = Dispatchers.IO
}
