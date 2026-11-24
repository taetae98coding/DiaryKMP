package io.github.taetae98coding.diary.core.file.impl

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.BindingContainer
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Provides
import io.github.taetae98coding.diary.core.file.impl.di.AppFileDirectory
import io.github.taetae98coding.diary.core.file.impl.di.AppFileDirectoryName
import io.github.taetae98coding.diary.core.file.impl.di.FileDownloadDefaultDirectory
import io.github.taetae98coding.diary.library.applicationsupport.applicationSupportDirectory
import io.github.taetae98coding.diary.library.applicationsupport.userHomeDirectory
import java.nio.file.Path

@BindingContainer
@ContributesTo(AppScope::class)
public object JvmFileModule {
    @Provides
    @AppFileDirectory
    internal fun providesAppFileDirectory(
        @AppFileDirectoryName
        directoryName: String,
    ): String =
        resolveAppFileDirectory(userHome = userHomeDirectory(), directoryName = directoryName)
            .toAbsolutePath()
            .toString()

    @Provides
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
