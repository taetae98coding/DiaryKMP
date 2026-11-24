@file:OptIn(ExperimentalMaterial3AdaptiveApi::class)

package io.github.taetae98coding.diary.feature.tag.ui

import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.navigation3.ListDetailSceneStrategy
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavBackStack
import dev.zacsweers.metrox.viewmodel.assistedMetroViewModel
import dev.zacsweers.metrox.viewmodel.metroViewModel
import io.github.taetae98coding.diary.compose.core.scene.LIST_DETAIL_PANE_WIDTH_FRACTION
import io.github.taetae98coding.diary.compose.routine.FinishedRoutineListDetailPlaceholder
import io.github.taetae98coding.diary.core.navigation.ScreenNavKey
import io.github.taetae98coding.diary.feature.routine.api.navigateToRoutineDetail
import io.github.taetae98coding.diary.feature.tag.api.TagRoutineFinishedListNavKey
import io.github.taetae98coding.diary.feature.tag.ui.routine.finished.TagRoutineFinishedListScreen
import io.github.taetae98coding.diary.feature.tag.ui.routine.finished.TagRoutineFinishedListViewModel

internal fun EntryProviderScope<ScreenNavKey>.tagRoutineFinishedListEntry(backStack: NavBackStack<ScreenNavKey>) {
    entry<TagRoutineFinishedListNavKey>(
        metadata = ::tagRoutineFinishedListPaneMetadata,
    ) { key ->
        TagRoutineFinishedListScreen(
            navigateUp = backStack::navigateUpFromTagRoutineFinishedList,
            navigateToRoutineDetail = backStack::navigateToRoutineDetail,
            routineViewModel = assistedMetroViewModel<TagRoutineFinishedListViewModel, TagRoutineFinishedListViewModel.Factory> { create(tagId = key.tagId) },
            syncViewModel = metroViewModel(),
        )
    }
}

internal fun tagRoutineFinishedListPaneMetadata(key: TagRoutineFinishedListNavKey): Map<String, Any> =
    ListDetailSceneStrategy.listPane(
        sceneKey = key,
        detailPlaceholder = { FinishedRoutineListDetailPlaceholder() },
    ) + ListDetailSceneStrategy.preferredPaneSize(width = LIST_DETAIL_PANE_WIDTH_FRACTION)
