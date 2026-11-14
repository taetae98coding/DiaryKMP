package io.github.taetae98coding.diary.feature.qr.api

import io.github.taetae98coding.diary.core.navigation.ScreenNavKey
import io.github.taetae98coding.diary.core.navigation.isListDetailPane

public fun List<ScreenNavKey>.isQrListDetailPane(key: ScreenNavKey): Boolean =
    isListDetailPane(
        key = key,
        homeKey = QrHomeNavKey,
        isDetailPaneKey = ScreenNavKey::isQrDetailPaneKey,
    )

public fun List<ScreenNavKey>.isQrAddOnDetailPane(): Boolean {
    val key = lastOrNull() ?: return false

    return key == QrHomeNavKey || (key is QrAddNavKey && isQrListDetailPane(key))
}

// 초기 태그가 있는 QrAdd는 TagDetail QR 탭에서 진입한 것이다.
private fun ScreenNavKey.isQrDetailPaneKey(): Boolean = (this is QrAddNavKey && initialTagId == null) || (this is QrDetailNavKey && !isStandalone)
