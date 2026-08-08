@file:OptIn(ExperimentalForeignApi::class)

package io.github.taetae98coding.diary.core.holiday.database.impl

import androidx.room3.Room
import androidx.room3.RoomDatabase
import io.github.taetae98coding.diary.core.holiday.database.impl.di.HolidayDatabaseBuilder
import kotlinx.cinterop.ExperimentalForeignApi
import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Configuration
import org.koin.core.annotation.Factory
import org.koin.core.annotation.Module
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSUserDomainMask

@Module
@ComponentScan
@Configuration
public class IosHolidayDatabaseModule {
    @Factory
    @HolidayDatabaseBuilder
    internal fun providesHolidayDatabaseBuilder(): RoomDatabase.Builder<HolidayDatabase> {
        val documentDirectory =
            NSFileManager.defaultManager.URLForDirectory(
                directory = NSDocumentDirectory,
                inDomain = NSUserDomainMask,
                appropriateForURL = null,
                create = true,
                error = null,
            )
        val path = requireNotNull(documentDirectory?.path) + "/" + HolidayDatabase.NAME

        return Room.databaseBuilder<HolidayDatabase>(name = path)
    }
}
