package io.github.taetae98coding.diary.feature.tag.ui.detail

import io.github.taetae98coding.diary.feature.tag.ui.detail.scope.TagDetailScopeState

internal fun handleTagDetailScaffoldEvent(
    event: TagDetailScaffoldEvent,
    viewModel: TagDetailViewModel,
    scopeState: TagDetailScopeState,
    navigateUp: () -> Unit,
) {
    when (event) {
        is TagDetailScaffoldEvent.ClickNavigateUp -> navigateUp()
        is TagDetailScaffoldEvent.ClickFinish -> viewModel.finish()
        is TagDetailScaffoldEvent.ClickRestart -> viewModel.restart()
        is TagDetailScaffoldEvent.ClickDelete -> viewModel.delete()
        is TagDetailScaffoldEvent.ClickScope -> scopeState.sheetState.show()
    }
}
