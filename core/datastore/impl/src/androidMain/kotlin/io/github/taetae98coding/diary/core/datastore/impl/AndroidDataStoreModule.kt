package io.github.taetae98coding.diary.core.datastore.impl

import android.content.Context
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.BindingContainer
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Provides

@BindingContainer
@ContributesTo(AppScope::class)
public object AndroidDataStoreModule {
    @Provides
    internal fun providesSettingPathResolver(context: Context): SettingPathResolver = SettingPathResolver { name -> context.filesDir.resolve(name).absolutePath }
}
