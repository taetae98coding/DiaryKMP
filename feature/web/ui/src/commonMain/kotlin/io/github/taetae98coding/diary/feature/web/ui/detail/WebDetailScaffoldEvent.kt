package io.github.taetae98coding.diary.feature.web.ui.detail

import io.github.taetae98coding.diary.feature.web.ui.detail.tab.WebDetailTab
import io.github.taetae98coding.diary.feature.web.ui.detail.viewmode.WebDetailViewMode

internal sealed interface WebDetailScaffoldEvent {
    data object ClickNavigateUp : WebDetailScaffoldEvent

    data object ClickRetry : WebDetailScaffoldEvent

    data object ClickUpdate : WebDetailScaffoldEvent

    data object ClickOpenInNew : WebDetailScaffoldEvent

    data object ClickDelete : WebDetailScaffoldEvent

    data object ClickViewMode : WebDetailScaffoldEvent

    data object SessionImportFailed : WebDetailScaffoldEvent

    data class SelectTab(
        val tab: WebDetailTab,
    ) : WebDetailScaffoldEvent

    data class SelectViewMode(
        val viewMode: WebDetailViewMode,
    ) : WebDetailScaffoldEvent
}
