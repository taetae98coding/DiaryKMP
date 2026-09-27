package io.github.taetae98coding.diary.feature.file.ui.home

internal sealed interface FileHomeRefreshEffect {
    data object RefreshFailed : FileHomeRefreshEffect
}
