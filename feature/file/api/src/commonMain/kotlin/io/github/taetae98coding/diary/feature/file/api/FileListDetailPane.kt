package io.github.taetae98coding.diary.feature.file.api

import io.github.taetae98coding.diary.core.navigation.ScreenNavKey
import io.github.taetae98coding.diary.core.navigation.isListDetailPane

public fun List<ScreenNavKey>.isFileListDetailPane(key: ScreenNavKey): Boolean =
    isListDetailPane(
        key = key,
        homeKey = FileHomeNavKey,
        isDetailPaneKey = ScreenNavKey::isFileDetailPaneKey,
    )

public fun List<ScreenNavKey>.isFileAddOnDetailPane(): Boolean {
    val key = lastOrNull() ?: return false

    return key == FileHomeNavKey || (key == FileAddNavKey && isFileListDetailPane(key))
}

public fun ScreenNavKey.isFileDetailPaneKey(): Boolean = this is FileAddNavKey || this is FileDetailNavKey
