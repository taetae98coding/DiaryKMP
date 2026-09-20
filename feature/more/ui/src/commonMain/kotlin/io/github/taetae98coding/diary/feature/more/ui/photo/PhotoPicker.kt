package io.github.taetae98coding.diary.feature.more.ui.photo

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import io.github.taetae98coding.diary.core.model.file.FileUri

@Stable
internal interface PhotoPicker {
    suspend fun open(): FileUri?
}

@Composable
internal expect fun rememberPhotoPicker(): PhotoPicker
