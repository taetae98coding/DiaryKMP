@file:OptIn(ExperimentalMaterial3AdaptiveApi::class)

package io.github.taetae98coding.diary.feature.memo.ui

import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.navigation3.ListDetailSceneStrategy
import io.github.taetae98coding.diary.compose.core.scene.LIST_DETAIL_PANE_WIDTH_FRACTION
import io.github.taetae98coding.diary.feature.core.memo.finished.EntityMemoFinishedListDetailPlaceholder
import io.github.taetae98coding.diary.feature.memo.api.MemoFinishedListNavKey

internal fun memoFinishedListPaneMetadata(): Map<String, Any> =
    ListDetailSceneStrategy.listPane(
        sceneKey = MemoFinishedListNavKey,
        detailPlaceholder = { EntityMemoFinishedListDetailPlaceholder() },
    ) + ListDetailSceneStrategy.preferredPaneSize(width = LIST_DETAIL_PANE_WIDTH_FRACTION)
