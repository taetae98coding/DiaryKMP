package io.github.taetae98coding.diary.core.datastore.impl

import io.github.taetae98coding.diary.library.applicationsupport.applicationSupportDirectoryPath
import org.koin.core.annotation.Configuration
import org.koin.core.annotation.Factory
import org.koin.core.annotation.Module

@Module
@Configuration
public class IosDataStoreModule {
    @Factory
    internal fun providesSettingPathResolver(): SettingPathResolver {
        val directoryPath = applicationSupportDirectoryPath()

        return SettingPathResolver { name -> "$directoryPath/$name" }
    }
}
