package io.github.taetae98coding.diary.feature.file.ui.add

internal sealed interface FileAddEffect {
    data object UploadStarted : FileAddEffect

    data object TitleBlank : FileAddEffect

    data object FileNotSelected : FileAddEffect

    data object FileUnreadable : FileAddEffect

    data object FileTooLarge : FileAddEffect

    data object UploadSucceeded : FileAddEffect

    data object UploadFailed : FileAddEffect
}
