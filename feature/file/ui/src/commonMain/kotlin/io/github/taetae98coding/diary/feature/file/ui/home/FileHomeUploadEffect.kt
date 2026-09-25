package io.github.taetae98coding.diary.feature.file.ui.home

import kotlin.uuid.Uuid

internal sealed interface FileHomeUploadEffect {
    data class UploadSucceeded(
        val id: Uuid,
    ) : FileHomeUploadEffect

    data object UploadTooLarge : FileHomeUploadEffect

    data object UploadFailed : FileHomeUploadEffect
}
