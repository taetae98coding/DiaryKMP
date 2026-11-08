package io.github.taetae98coding.diary.feature.dday.ui

import androidx.navigation3.runtime.NavBackStack
import io.github.taetae98coding.diary.core.navigation.ScreenNavKey
import io.github.taetae98coding.diary.core.navigation.navigateToDetail
import io.github.taetae98coding.diary.core.navigation.navigateUpTo
import io.github.taetae98coding.diary.feature.dday.api.DDayAddNavKey
import io.github.taetae98coding.diary.feature.dday.api.DDayDetailNavKey
import io.github.taetae98coding.diary.feature.dday.api.DDayHomeNavKey
import kotlin.uuid.Uuid

internal fun NavBackStack<ScreenNavKey>.navigateToDDayDetail(id: Uuid) {
    navigateToDetail(
        detailKey = DDayDetailNavKey(id = id),
        isDetailPaneKey = { key -> key is DDayDetailNavKey || key is DDayAddNavKey },
    )
}

internal fun NavBackStack<ScreenNavKey>.navigateUpFromDDayHome() {
    navigateUpTo(homeKey = DDayHomeNavKey)
}

internal fun NavBackStack<ScreenNavKey>.navigateToDDayAddFromHome() {
    add(DDayAddNavKey())
}
