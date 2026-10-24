@file:OptIn(ExperimentalMaterial3AdaptiveApi::class)

package io.github.taetae98coding.diary.feature.tag.ui

import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.navigation3.ListDetailSceneStrategy
import androidx.navigation3.runtime.NavBackStack
import io.github.taetae98coding.diary.compose.core.scene.LIST_DETAIL_PANE_WIDTH_FRACTION
import io.github.taetae98coding.diary.core.navigation.ScreenNavKey
import io.github.taetae98coding.diary.feature.tag.api.TagFinishedListNavKey
import io.github.taetae98coding.diary.feature.tag.ui.finished.TagFinishedListDetailPlaceholder

internal fun tagFinishedListPaneMetadata(): Map<String, Any> =
    ListDetailSceneStrategy.listPane(
        sceneKey = TagFinishedListNavKey,
        detailPlaceholder = { TagFinishedListDetailPlaceholder() },
    ) + ListDetailSceneStrategy.preferredPaneSize(width = LIST_DETAIL_PANE_WIDTH_FRACTION)

internal fun NavBackStack<ScreenNavKey>.navigateUpFromTagFinishedList() {
    val index = indexOfLast { key -> key == TagFinishedListNavKey }
    if (index < 0) return

    while (size > index) {
        removeLastOrNull()
    }
}
