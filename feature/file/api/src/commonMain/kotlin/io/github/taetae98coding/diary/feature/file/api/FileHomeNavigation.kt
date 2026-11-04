package io.github.taetae98coding.diary.feature.file.api

import androidx.navigation3.runtime.NavBackStack
import io.github.taetae98coding.diary.core.navigation.ScreenNavKey

public fun NavBackStack<ScreenNavKey>.openFileHome(navKeyList: List<ScreenNavKey>) {
    if (isFileScreenVisible()) return

    clear()
    addAll(navKeyList)
}

private fun List<ScreenNavKey>.isFileScreenVisible(): Boolean =
    when (lastOrNull()) {
        FileHomeNavKey -> true
        FileAddNavKey -> isFileListDetailPane(key = FileAddNavKey)
        else -> false
    }
