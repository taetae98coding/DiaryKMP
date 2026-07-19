@file:OptIn(ExperimentalForeignApi::class)

package io.github.taetae98coding.diary.core.datastore.impl

import kotlinx.cinterop.ExperimentalForeignApi
import org.koin.core.annotation.Configuration
import org.koin.core.annotation.Factory
import org.koin.core.annotation.Module
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSUserDomainMask

@Module
@Configuration
public class IosDataStoreModule {
    @Factory
    internal fun providesSettingPathResolver(): SettingPathResolver {
        val documentDirectory =
            NSFileManager.defaultManager.URLForDirectory(
                directory = NSDocumentDirectory,
                inDomain = NSUserDomainMask,
                appropriateForURL = null,
                create = true,
                error = null,
            )
        val documentPath = requireNotNull(documentDirectory?.path)

        return SettingPathResolver { name -> "$documentPath/$name" }
    }
}
