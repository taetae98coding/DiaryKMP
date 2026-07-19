package io.github.taetae98coding.diary.core.file.impl

import android.content.Context
import io.github.taetae98coding.diary.core.file.api.FileReader
import org.koin.core.annotation.Configuration
import org.koin.core.annotation.Factory
import org.koin.core.annotation.Module

@Module
@Configuration
public class AndroidFileModule {
    @Factory
    internal fun providesFileReader(context: Context): FileReader = AndroidFileReader(context = context)
}
