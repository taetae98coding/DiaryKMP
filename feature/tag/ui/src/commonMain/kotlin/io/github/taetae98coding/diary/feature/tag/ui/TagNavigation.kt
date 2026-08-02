package io.github.taetae98coding.diary.feature.tag.ui

import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import io.github.taetae98coding.diary.core.model.location.Coordinate
import io.github.taetae98coding.diary.feature.place.api.PlaceAddNavKey
import io.github.taetae98coding.diary.feature.tag.api.TagDetailNavKey
import io.github.taetae98coding.diary.feature.tag.api.TagMemoFinishedListNavKey
import io.github.taetae98coding.diary.feature.web.api.WebAddNavKey
import kotlin.uuid.Uuid

internal fun NavBackStack<NavKey>.navigateToTagDetail(id: Uuid) {
    if (lastOrNull() is TagDetailNavKey) {
        removeLastOrNull()
    }

    add(TagDetailNavKey(id))
}

internal fun NavBackStack<NavKey>.navigateToWebAddFromTagDetail(tagId: Uuid) {
    add(WebAddNavKey(initialTagId = tagId))
}

internal fun NavBackStack<NavKey>.navigateToPlaceAddFromTagDetail(
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

internal fun NavBackStack<NavKey>.navigateUpFromTagMemoFinishedList() {
    val index = indexOfLast { key -> key is TagMemoFinishedListNavKey }
    if (index < 0) return

    while (size > index) {
        removeLastOrNull()
    }
}
