package io.github.taetae98coding.diary.core.datastore.impl

import io.github.taetae98coding.diary.core.datastore.impl.di.DiarySettingDirectory
import org.koin.core.annotation.Configuration
import org.koin.core.annotation.Factory
import org.koin.core.annotation.Module
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

@Module
@Configuration
public class JvmDataStoreModule {
    @Factory
    internal fun providesSettingPathResolver(
        @DiarySettingDirectory
        settingDirectory: String,
    ): SettingPathResolver {
        val directory =
            resolveSettingDirectory(
                userHome = Paths.get(System.getProperty("user.home")),
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
): Path =
    userHome
        .resolve("Library/Application Support")
        .resolve(settingDirectory)
