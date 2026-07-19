package io.github.taetae98coding.diary.core.datastore.impl

import android.content.Context
import org.koin.core.annotation.Configuration
import org.koin.core.annotation.Factory
import org.koin.core.annotation.Module

@Module
@Configuration
public class AndroidDataStoreModule {
    @Factory
    internal fun providesSettingPathResolver(context: Context): SettingPathResolver = SettingPathResolver { name -> context.filesDir.resolve(name).absolutePath }
}
