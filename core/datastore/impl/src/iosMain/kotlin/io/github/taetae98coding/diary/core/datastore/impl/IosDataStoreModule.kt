package io.github.taetae98coding.diary.core.datastore.impl

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.BindingContainer
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Provides
import io.github.taetae98coding.diary.library.applicationsupport.applicationSupportDirectoryPath

@BindingContainer
@ContributesTo(AppScope::class)
public object IosDataStoreModule {
    @Provides
    internal fun providesSettingPathResolver(): SettingPathResolver {
        val directoryPath = applicationSupportDirectoryPath()

        return SettingPathResolver { name -> "$directoryPath/$name" }
    }
}
