package io.github.taetae98coding.diary.feature.tag.ui

import androidx.navigation3.runtime.NavBackStack
import io.github.taetae98coding.diary.core.model.location.Coordinate
import io.github.taetae98coding.diary.feature.place.api.PlaceAddNavKey
import io.github.taetae98coding.diary.feature.tag.api.TagDetailNavKey
import io.github.taetae98coding.diary.feature.tag.api.TagMemoFinishedListNavKey
import io.github.taetae98coding.diary.feature.web.api.WebAddNavKey
import io.github.taetae98coding.diary.library.navigation3.ScreenNavKey
import kotlin.uuid.Uuid

internal fun NavBackStack<ScreenNavKey>.navigateToTagDetail(id: Uuid) {
    if (lastOrNull() is TagDetailNavKey) {
        removeLastOrNull()
    }

    add(TagDetailNavKey(id))
}

internal fun NavBackStack<ScreenNavKey>.navigateToWebAddFromTagDetail(tagId: Uuid) {
    add(WebAddNavKey(initialTagId = tagId))
}

internal fun NavBackStack<ScreenNavKey>.navigateToPlaceAddFromTagDetail(
    tagId: Uuid,
    coordinate: Coordinate?,
) {
    add(
        PlaceAddNavKey(
            latitude = coordinate?.latitude,
            longitude = coordinate?.longitude,
            initialTagId = tagId,
        ),
    )
}

internal fun NavBackStack<ScreenNavKey>.navigateUpFromTagMemoFinishedList() {
    val index = indexOfLast { key -> key is TagMemoFinishedListNavKey }
    if (index < 0) return

    while (size > index) {
        removeLastOrNull()
    }
}
