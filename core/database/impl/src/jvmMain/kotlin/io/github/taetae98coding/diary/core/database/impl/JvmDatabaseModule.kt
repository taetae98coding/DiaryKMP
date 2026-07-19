package io.github.taetae98coding.diary.core.database.impl

import androidx.room3.Room
import androidx.room3.RoomDatabase
import io.github.taetae98coding.diary.core.database.impl.di.DiaryDatabaseBuilder
import io.github.taetae98coding.diary.core.database.impl.di.DiaryDatabaseDirectory
import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Configuration
import org.koin.core.annotation.Factory
import org.koin.core.annotation.Module
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

@Module
@ComponentScan
@Configuration
public class JvmDatabaseModule {
    @Factory
    @DiaryDatabaseBuilder
    internal fun providesDiaryDatabaseBuilder(
        @DiaryDatabaseDirectory
        databaseDirectory: String,
    ): RoomDatabase.Builder<DiaryDatabase> {
        val databasePath =
            resolveDatabasePath(
                userHome = Paths.get(System.getProperty("user.home")),
                databaseDirectory = databaseDirectory,
            )
        databasePath.parent?.let(Files::createDirectories)

        return Room.databaseBuilder<DiaryDatabase>(name = databasePath.toAbsolutePath().toString())
    }
}

internal fun resolveDatabasePath(
    userHome: Path,
    databaseDirectory: String,
): Path =
    userHome
        .resolve("Library/Application Support")
        .resolve(databaseDirectory)
        .resolve(DiaryDatabase.NAME)
