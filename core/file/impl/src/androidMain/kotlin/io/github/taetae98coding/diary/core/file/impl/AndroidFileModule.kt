package io.github.taetae98coding.diary.core.file.impl

import android.content.Context
import io.github.taetae98coding.diary.core.file.impl.di.AppFileDirectory
import org.koin.core.annotation.Configuration
import org.koin.core.annotation.Factory
import org.koin.core.annotation.Module

@Module
@Configuration
public class AndroidFileModule {
    @Factory
    @AppFileDirectory
    internal fun providesAppFileDirectory(context: Context): String = context.filesDir.absolutePath
}
