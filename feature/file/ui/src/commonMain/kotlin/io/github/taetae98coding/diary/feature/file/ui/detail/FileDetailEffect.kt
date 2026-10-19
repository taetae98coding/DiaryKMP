package io.github.taetae98coding.diary.feature.file.ui.detail

internal sealed interface FileDetailEffect {
    data object TitleBlank : FileDetailEffect

    data object NameBlank : FileDetailEffect

    data object NameTooLong : FileDetailEffect

    data object UpdateSucceeded : FileDetailEffect

    data object UpdateFailed : FileDetailEffect

    data class DownloadSucceeded(
        val name: String,
    ) : FileDetailEffect

    data object DownloadDirectoryUnavailable : FileDetailEffect

    data object DownloadFailed : FileDetailEffect
}
