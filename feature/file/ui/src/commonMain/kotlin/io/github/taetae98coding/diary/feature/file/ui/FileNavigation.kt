package io.github.taetae98coding.diary.feature.file.ui

import androidx.navigation3.runtime.NavBackStack
import io.github.taetae98coding.diary.core.navigation.ScreenNavKey
import io.github.taetae98coding.diary.core.navigation.navigateToDetail
import io.github.taetae98coding.diary.core.navigation.navigateUpTo
import io.github.taetae98coding.diary.feature.file.api.FileDetailNavKey
import io.github.taetae98coding.diary.feature.file.api.FileHomeNavKey
import io.github.taetae98coding.diary.feature.file.api.isFileDetailPaneKey
import kotlin.uuid.Uuid

internal fun NavBackStack<ScreenNavKey>.navigateToFileDetail(id: Uuid) {
    navigateToDetail(
        detailKey = FileDetailNavKey(id = id),
        isDetailPaneKey = ScreenNavKey::isFileDetailPaneKey,
    )
}

internal fun NavBackStack<ScreenNavKey>.navigateUpFromFileHome() {
    navigateUpTo(homeKey = FileHomeNavKey)
}
