package io.github.taetae98coding.diary.app.shared

import io.github.taetae98coding.diary.feature.file.ui.picker.FilePickerDispatcher
import io.github.taetae98coding.diary.feature.more.ui.photo.PhotoPickerDispatcher
import io.github.taetae98coding.diary.feature.setting.ui.file.picker.DirectoryPickerDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import org.koin.core.annotation.Configuration
import org.koin.core.annotation.Factory
import org.koin.core.annotation.Module

@Module
@Configuration
internal class JvmPickerModule {
    @Factory
    @FilePickerDispatcher
    fun providesFilePickerDispatcher(): CoroutineDispatcher = Dispatchers.IO

    @Factory
    @DirectoryPickerDispatcher
    fun providesDirectoryPickerDispatcher(): CoroutineDispatcher = Dispatchers.IO

    @Factory
    @PhotoPickerDispatcher
    fun providesPhotoPickerDispatcher(): CoroutineDispatcher = Dispatchers.IO
}
