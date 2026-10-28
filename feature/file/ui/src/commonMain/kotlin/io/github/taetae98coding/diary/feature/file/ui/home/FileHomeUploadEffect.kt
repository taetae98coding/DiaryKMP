package io.github.taetae98coding.diary.feature.file.ui.home

internal sealed interface FileHomeUploadEffect {
    data object UploadSucceeded : FileHomeUploadEffect

    data object UploadTooLarge : FileHomeUploadEffect

    data object UploadFailed : FileHomeUploadEffect

    data object NavigateToAdd : FileHomeUploadEffect
}
