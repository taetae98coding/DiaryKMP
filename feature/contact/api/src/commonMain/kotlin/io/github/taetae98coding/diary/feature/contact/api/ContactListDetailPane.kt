package io.github.taetae98coding.diary.feature.contact.api

import io.github.taetae98coding.diary.core.navigation.ScreenNavKey
import io.github.taetae98coding.diary.core.navigation.isListDetailPane

public fun List<ScreenNavKey>.isContactListDetailPane(key: ScreenNavKey): Boolean =
    isListDetailPane(
        key = key,
        homeKey = ContactHomeNavKey,
        isDetailPaneKey = ScreenNavKey::isContactDetailPaneKey,
    )

public fun List<ScreenNavKey>.isContactAddOnDetailPane(): Boolean {
    val key = lastOrNull() ?: return false

    return key == ContactHomeNavKey || (key is ContactAddNavKey && isContactListDetailPane(key))
}

// 초기 태그가 있는 ContactAdd는 TagDetail 연락처 탭에서 진입한 것이다.
private fun ScreenNavKey.isContactDetailPaneKey(): Boolean = (this is ContactAddNavKey && initialTagId == null) || (this is ContactDetailNavKey && !isStandalone)
