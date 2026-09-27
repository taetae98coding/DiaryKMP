package io.github.taetae98coding.diary.work.fileupload

import io.github.taetae98coding.diary.work.fileupload.di.FileUploadScope
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Configuration
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single

@Module
@Configuration
@ComponentScan
public class WorkFileUploadModule {
    @Single
    @FileUploadScope
    internal fun providesFileUploadCoroutineScope(): CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default + CoroutineExceptionHandler { _, _ -> })
}
