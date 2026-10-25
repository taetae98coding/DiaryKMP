package io.github.taetae98coding.diary.core.file.impl

import io.github.taetae98coding.diary.core.file.impl.di.AppFileDirectory
import io.github.taetae98coding.diary.core.file.impl.di.AppFileDirectoryName
import io.github.taetae98coding.diary.core.file.impl.di.FileDownloadDefaultDirectory
import io.github.taetae98coding.diary.library.applicationsupport.applicationSupportDirectory
import io.github.taetae98coding.diary.library.applicationsupport.userHomeDirectory
import org.koin.core.annotation.Configuration
import org.koin.core.annotation.Factory
import org.koin.core.annotation.Module
import java.nio.file.Path

@Module
@Configuration
public class JvmFileModule {
    @Factory
    @AppFileDirectory
    internal fun providesAppFileDirectory(
        @AppFileDirectoryName
        directoryName: String,
    ): String =
        resolveAppFileDirectory(userHome = userHomeDirectory(), directoryName = directoryName)
            .toAbsolutePath()
            .toString()

    @Factory
    @FileDownloadDefaultDirectory
    internal fun providesFileDownloadDefaultDirectory(): String =
        resolveFileDownloadDefaultDirectory(userHome = userHomeDirectory())
            .toAbsolutePath()
            .toString()
}

internal fun resolveAppFileDirectory(
    userHome: Path,
    directoryName: String,
): Path = applicationSupportDirectory(directoryName = directoryName, userHome = userHome)

internal fun resolveFileDownloadDefaultDirectory(userHome: Path): Path = userHome.resolve(DOWNLOADS_DIRECTORY_NAME)

private const val DOWNLOADS_DIRECTORY_NAME = "Downloads"
