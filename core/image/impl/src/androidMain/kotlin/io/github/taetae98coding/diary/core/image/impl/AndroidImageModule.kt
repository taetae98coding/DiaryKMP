package io.github.taetae98coding.diary.core.image.impl

import android.content.Context
import io.github.taetae98coding.diary.core.image.api.ImageConverter
import org.koin.core.annotation.Configuration
import org.koin.core.annotation.Factory
import org.koin.core.annotation.Module

@Module
@Configuration
public class AndroidImageModule {
    @Factory
    internal fun providesImageConverter(context: Context): ImageConverter = AndroidImageConverter(context = context)
}
