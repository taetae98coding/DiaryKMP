package io.github.taetae98coding.diary.core.datastore.impl

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.BindingContainer
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn
import io.github.taetae98coding.diary.core.datastore.impl.di.DiarySettingDirectory
import io.github.taetae98coding.diary.library.applicationsupport.applicationSupportDirectory
import io.github.taetae98coding.diary.library.applicationsupport.userHomeDirectory
import java.nio.file.Files
import java.nio.file.Path

@BindingContainer
@ContributesTo(AppScope::class)
public object JvmDataStoreModule {
    @Provides
    @SingleIn(AppScope::class)
    internal fun providesSettingPathResolver(
        @DiarySettingDirectory
        settingDirectory: String,
    ): SettingPathResolver {
        val directory =
            resolveSettingDirectory(
                userHome = userHomeDirectory(),
                settingDirectory = settingDirectory,
            )
        Files.createDirectories(directory)

        return SettingPathResolver { name ->
            directory
                .resolve(name)
                .toAbsolutePath()
                .toString()
        }
    }
}

internal fun resolveSettingDirectory(
    userHome: Path,
    settingDirectory: String,
): Path = applicationSupportDirectory(directoryName = settingDirectory, userHome = userHome)
