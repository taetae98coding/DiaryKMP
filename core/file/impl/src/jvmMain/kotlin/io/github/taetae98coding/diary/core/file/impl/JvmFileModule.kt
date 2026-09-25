package io.github.taetae98coding.diary.core.file.impl

import io.github.taetae98coding.diary.core.file.impl.di.AppFileDirectory
import io.github.taetae98coding.diary.core.file.impl.di.AppFileDirectoryName
import io.github.taetae98coding.diary.library.applicationsupport.applicationSupportDirectory
import org.koin.core.annotation.Configuration
import org.koin.core.annotation.Factory
import org.koin.core.annotation.Module
import java.nio.file.Path
import java.nio.file.Paths

@Module
@Configuration
public class JvmFileModule {
    @Factory
    @AppFileDirectory
    internal fun providesAppFileDirectory(
        @AppFileDirectoryName
        directoryName: String,
    ): String =
        resolveAppFileDirectory(userHome = Paths.get(System.getProperty("user.home")), directoryName = directoryName)
            .toAbsolutePath()
            .toString()
}

internal fun resolveAppFileDirectory(
    userHome: Path,
    directoryName: String,
): Path = applicationSupportDirectory(directoryName = directoryName, userHome = userHome)
