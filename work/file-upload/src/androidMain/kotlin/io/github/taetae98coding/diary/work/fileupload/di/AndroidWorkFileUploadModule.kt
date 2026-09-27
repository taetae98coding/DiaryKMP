package io.github.taetae98coding.diary.work.fileupload.di

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import org.koin.core.annotation.Configuration
import org.koin.core.annotation.Factory
import org.koin.core.annotation.Module

@Module
@Configuration
public class AndroidWorkFileUploadModule {
    @Factory
    @FileUploadTextDispatcher
    internal fun providesFileUploadTextDispatcher(): CoroutineDispatcher = Dispatchers.IO
}
