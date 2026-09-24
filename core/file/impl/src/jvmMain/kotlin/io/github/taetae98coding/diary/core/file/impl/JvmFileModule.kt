package io.github.taetae98coding.diary.core.file.impl

import io.github.taetae98coding.diary.core.file.impl.di.AppFileDirectory
import io.github.taetae98coding.diary.core.file.impl.di.AppFileDirectoryName
import io.github.taetae98coding.diary.library.applicationsupport.applicationSupportDirectory
import org.koin.core.annotation.Configuration
import org.koin.core.annotation.Factory
import org.koin.core.annotation.Module

@Module
@Configuration
public class JvmFileModule {
    @Factory
    @AppFileDirectory
    internal fun providesAppFileDirectory(
        @AppFileDirectoryName
        directoryName: String,
    ): String =
        applicationSupportDirectory(directoryName = directoryName)
            .toAbsolutePath()
            .toString()
}
