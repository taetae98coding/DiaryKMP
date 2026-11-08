package io.github.taetae98coding.diary.feature.tag.ui

import androidx.navigation3.runtime.NavBackStack
import io.github.taetae98coding.diary.core.model.location.Coordinate
import io.github.taetae98coding.diary.core.navigation.ScreenNavKey
import io.github.taetae98coding.diary.feature.checklist.api.ChecklistAddNavKey
import io.github.taetae98coding.diary.feature.contact.api.ContactAddNavKey
import io.github.taetae98coding.diary.feature.dday.api.DDayAddNavKey
import io.github.taetae98coding.diary.feature.place.api.PlaceAddNavKey
import io.github.taetae98coding.diary.feature.playlist.api.MusicAddNavKey
import io.github.taetae98coding.diary.feature.qr.api.QrAddNavKey
import io.github.taetae98coding.diary.feature.web.api.WebAddNavKey
import kotlin.uuid.Uuid

internal fun NavBackStack<ScreenNavKey>.navigateToWebAddFromTagDetail(tagId: Uuid) {
    add(WebAddNavKey(initialTagId = tagId))
}

internal fun NavBackStack<ScreenNavKey>.navigateToContactAddFromTagDetail(tagId: Uuid) {
    add(ContactAddNavKey(initialTagId = tagId))
}

internal fun NavBackStack<ScreenNavKey>.navigateToChecklistAddFromTagDetail(tagId: Uuid) {
    add(ChecklistAddNavKey(initialTagId = tagId))
}

internal fun NavBackStack<ScreenNavKey>.navigateToMusicAddFromTagDetail(tagId: Uuid) {
    add(MusicAddNavKey(initialTagId = tagId))
}

internal fun NavBackStack<ScreenNavKey>.navigateToQrAddFromTagDetail(tagId: Uuid) {
    add(QrAddNavKey(initialTagId = tagId))
}

internal fun NavBackStack<ScreenNavKey>.navigateToDDayAddFromTagDetail(tagId: Uuid) {
    add(DDayAddNavKey(initialTagId = tagId))
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
