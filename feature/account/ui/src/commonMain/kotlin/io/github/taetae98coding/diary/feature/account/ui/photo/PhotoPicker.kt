package io.github.taetae98coding.diary.feature.account.ui.photo

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import io.github.taetae98coding.diary.core.model.file.FileUri

@Stable
internal interface PhotoPicker {
    suspend fun open(): FileUri?
}

internal interface PhotoPickerFactory {
    @Composable
    fun rememberPhotoPicker(): PhotoPicker
}
