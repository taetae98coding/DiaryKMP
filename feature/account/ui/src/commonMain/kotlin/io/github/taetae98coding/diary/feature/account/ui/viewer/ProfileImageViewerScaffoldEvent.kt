package io.github.taetae98coding.diary.feature.account.ui.viewer

internal sealed interface ProfileImageViewerScaffoldEvent {
    data object ClickNavigateUp : ProfileImageViewerScaffoldEvent
}
