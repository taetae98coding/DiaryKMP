package io.github.taetae98coding.diary.core.holiday.database.impl

import androidx.room3.Room
import androidx.room3.RoomDatabase
import io.github.taetae98coding.diary.core.holiday.database.impl.di.HolidayDatabaseBuilder
import io.github.taetae98coding.diary.core.holiday.database.impl.di.HolidayDatabaseDirectory
import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Configuration
import org.koin.core.annotation.Factory
import org.koin.core.annotation.Module
import java.nio.file.Files
import java.nio.file.Paths

@Module
@ComponentScan
@Configuration
public class JvmHolidayDatabaseModule {
    @Factory
    @HolidayDatabaseBuilder
    internal fun providesHolidayDatabaseBuilder(
        @HolidayDatabaseDirectory
        databaseDirectory: String,
    ): RoomDatabase.Builder<HolidayDatabase> {
        val databasePath =
            Paths
                .get(System.getProperty("user.home"))
                .resolve("Library/Application Support")
                .resolve(databaseDirectory)
                .resolve(HolidayDatabase.NAME)
        databasePath.parent?.let(Files::createDirectories)

        return Room.databaseBuilder<HolidayDatabase>(name = databasePath.toAbsolutePath().toString())
    }
}
