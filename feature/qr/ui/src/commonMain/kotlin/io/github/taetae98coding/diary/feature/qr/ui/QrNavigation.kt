package io.github.taetae98coding.diary.feature.qr.ui

import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.result.ResultEventBus
import io.github.taetae98coding.diary.core.navigation.ScreenNavKey
import io.github.taetae98coding.diary.feature.qr.ui.scan.QrScannedResult

internal fun NavBackStack<ScreenNavKey>.navigateUpWithQrScannedValue(
    resultEventBus: ResultEventBus,
    value: String,
) {
    resultEventBus.sendResult(result = QrScannedResult(value = value))
    removeLastOrNull()
}
