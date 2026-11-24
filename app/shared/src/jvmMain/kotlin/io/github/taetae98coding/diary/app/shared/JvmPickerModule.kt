package io.github.taetae98coding.diary.app.shared

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.BindingContainer
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Provides
import io.github.taetae98coding.diary.feature.account.ui.photo.PhotoPickerDispatcher
import io.github.taetae98coding.diary.feature.file.ui.picker.FilePickerDispatcher
import io.github.taetae98coding.diary.feature.setting.ui.file.picker.DirectoryPickerDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

@BindingContainer
@ContributesTo(AppScope::class)
internal object JvmPickerModule {
    @Provides
    @FilePickerDispatcher
    fun providesFilePickerDispatcher(): CoroutineDispatcher = Dispatchers.IO

    @Provides
    @DirectoryPickerDispatcher
    fun providesDirectoryPickerDispatcher(): CoroutineDispatcher = Dispatchers.IO

    @Provides
    @PhotoPickerDispatcher
    fun providesPhotoPickerDispatcher(): CoroutineDispatcher = Dispatchers.IO
}
