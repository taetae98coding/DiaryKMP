package io.github.taetae98coding.diary.core.database.impl

import androidx.room3.Room
import androidx.room3.RoomDatabase
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.BindingContainer
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Provides
import io.github.taetae98coding.diary.core.database.impl.di.DiaryDatabaseBuilder
import io.github.taetae98coding.diary.core.database.impl.di.DiaryDatabaseDirectory
import io.github.taetae98coding.diary.library.applicationsupport.applicationSupportDirectory
import io.github.taetae98coding.diary.library.applicationsupport.userHomeDirectory
import java.nio.file.Files
import java.nio.file.Path

@BindingContainer
@ContributesTo(AppScope::class)
public object JvmDatabaseModule {
    @Provides
    @DiaryDatabaseBuilder
    internal fun providesDiaryDatabaseBuilder(
        @DiaryDatabaseDirectory
        databaseDirectory: String,
    ): RoomDatabase.Builder<DiaryDatabase> {
        val databasePath =
            resolveDatabasePath(
                userHome = userHomeDirectory(),
                databaseDirectory = databaseDirectory,
            )
        databasePath.parent?.let(Files::createDirectories)

        return Room.databaseBuilder<DiaryDatabase>(name = databasePath.toAbsolutePath().toString())
    }
}

internal fun resolveDatabasePath(
    userHome: Path,
    databaseDirectory: String,
): Path = applicationSupportDirectory(directoryName = databaseDirectory, userHome = userHome).resolve(DiaryDatabase.NAME)
