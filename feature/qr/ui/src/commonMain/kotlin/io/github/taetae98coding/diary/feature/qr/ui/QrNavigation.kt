package io.github.taetae98coding.diary.feature.qr.ui

import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.result.ResultEventBus
import io.github.taetae98coding.diary.core.navigation.ScreenNavKey
import io.github.taetae98coding.diary.core.navigation.navigateToDetail
import io.github.taetae98coding.diary.core.navigation.navigateUpTo
import io.github.taetae98coding.diary.feature.qr.api.QrAddNavKey
import io.github.taetae98coding.diary.feature.qr.api.QrDetailNavKey
import io.github.taetae98coding.diary.feature.qr.api.QrHomeNavKey
import io.github.taetae98coding.diary.feature.qr.ui.scan.QrScannedResult
import kotlin.uuid.Uuid

internal fun NavBackStack<ScreenNavKey>.navigateToQrDetail(id: Uuid) {
    navigateToDetail(
        detailKey = QrDetailNavKey(id = id),
        isDetailPaneKey = { key -> key is QrDetailNavKey || key == QrAddNavKey },
    )
}

internal fun NavBackStack<ScreenNavKey>.navigateUpFromQrHome() {
    navigateUpTo(homeKey = QrHomeNavKey)
}

internal fun NavBackStack<ScreenNavKey>.navigateUpWithQrScannedValue(
    resultEventBus: ResultEventBus,
    value: String,
) {
    resultEventBus.sendResult(result = QrScannedResult(value = value))
    removeLastOrNull()
}
