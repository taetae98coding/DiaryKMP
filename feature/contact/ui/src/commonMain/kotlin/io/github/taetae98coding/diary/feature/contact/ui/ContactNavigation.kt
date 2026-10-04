package io.github.taetae98coding.diary.feature.contact.ui

import androidx.navigation3.runtime.NavBackStack
import io.github.taetae98coding.diary.core.navigation.ScreenNavKey
import io.github.taetae98coding.diary.core.navigation.navigateToDetail
import io.github.taetae98coding.diary.core.navigation.navigateUpTo
import io.github.taetae98coding.diary.feature.contact.api.ContactAddNavKey
import io.github.taetae98coding.diary.feature.contact.api.ContactDetailNavKey
import io.github.taetae98coding.diary.feature.contact.api.ContactHomeNavKey
import kotlin.uuid.Uuid

internal fun NavBackStack<ScreenNavKey>.navigateToContactDetail(id: Uuid) {
    navigateToDetail(
        detailKey = ContactDetailNavKey(id = id),
        isDetailPaneKey = { key -> key is ContactDetailNavKey || key is ContactAddNavKey },
    )
}

internal fun NavBackStack<ScreenNavKey>.navigateUpFromContactHome() {
    navigateUpTo(homeKey = ContactHomeNavKey)
}
