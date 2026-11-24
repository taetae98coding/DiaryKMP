package io.github.taetae98coding.diary.core.file.impl

import android.content.Context
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.BindingContainer
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Provides
import io.github.taetae98coding.diary.core.file.impl.di.AppFileDirectory

@BindingContainer
@ContributesTo(AppScope::class)
public object AndroidFileModule {
    @Provides
    @AppFileDirectory
    internal fun providesAppFileDirectory(context: Context): String = context.filesDir.absolutePath
}
