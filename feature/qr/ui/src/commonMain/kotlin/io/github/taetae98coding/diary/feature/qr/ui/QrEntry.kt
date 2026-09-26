package io.github.taetae98coding.diary.feature.qr.ui

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.result.LocalResultEventBus
import androidx.navigation3.runtime.result.ResultEventBus
import io.github.taetae98coding.diary.compose.permission.rememberPermissionManager
import io.github.taetae98coding.diary.core.navigation.ScreenNavKey
import io.github.taetae98coding.diary.feature.qr.api.QrAddNavKey
import io.github.taetae98coding.diary.feature.qr.api.QrHomeNavKey
import io.github.taetae98coding.diary.feature.qr.api.QrScanNavKey
import io.github.taetae98coding.diary.feature.qr.ui.add.QrAddScreen
import io.github.taetae98coding.diary.feature.qr.ui.home.QrHomeScreen
import io.github.taetae98coding.diary.feature.qr.ui.scan.QrScanScreen
import io.github.taetae98coding.diary.feature.qr.ui.scan.QrScannedResult

public fun EntryProviderScope<ScreenNavKey>.qrEntry(backStack: NavBackStack<ScreenNavKey>) {
    qrHomeEntry(backStack = backStack)
    qrAddEntry(backStack = backStack)
    qrScanEntry(backStack = backStack)
}

private fun EntryProviderScope<ScreenNavKey>.qrHomeEntry(backStack: NavBackStack<ScreenNavKey>) {
    entry<QrHomeNavKey> {
        QrHomeScreen(
            navigateUp = backStack::removeLastOrNull,
            navigateToAdd = { backStack.add(QrAddNavKey) },
        )
    }
}

private fun EntryProviderScope<ScreenNavKey>.qrAddEntry(backStack: NavBackStack<ScreenNavKey>) {
    entry<QrAddNavKey> {
        QrAddScreen(
            navigateUp = backStack::removeLastOrNull,
            navigateToScan = { backStack.add(QrScanNavKey) },
            permissionManager = rememberPermissionManager(),
            resultEventBus = LocalResultEventBus.current,
        )
    }
}

private fun EntryProviderScope<ScreenNavKey>.qrScanEntry(backStack: NavBackStack<ScreenNavKey>) {
    entry<QrScanNavKey> {
        val resultEventBus = LocalResultEventBus.current

        QrScanScreen(
            navigateUp = backStack::removeLastOrNull,
            navigateUpWithValue = { value ->
                backStack.navigateUpWithQrScannedValue(
                    resultEventBus = resultEventBus,
                    value = value,
                )
            },
        )
    }
}

internal fun NavBackStack<ScreenNavKey>.navigateUpWithQrScannedValue(
    resultEventBus: ResultEventBus,
    value: String,
) {
    resultEventBus.sendResult(result = QrScannedResult(value = value))
    removeLastOrNull()
}
