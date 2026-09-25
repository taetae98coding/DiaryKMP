package io.github.taetae98coding.diary.feature.qr.ui

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavBackStack
import io.github.taetae98coding.diary.compose.permission.rememberPermissionManager
import io.github.taetae98coding.diary.core.navigation.ScreenNavKey
import io.github.taetae98coding.diary.feature.qr.api.QrHomeNavKey
import io.github.taetae98coding.diary.feature.qr.api.QrScanNavKey
import io.github.taetae98coding.diary.feature.qr.ui.home.QrHomeScreen
import io.github.taetae98coding.diary.feature.qr.ui.scan.QrScanScreen

public fun EntryProviderScope<ScreenNavKey>.qrEntry(backStack: NavBackStack<ScreenNavKey>) {
    qrHomeEntry(backStack = backStack)
    qrScanEntry(backStack = backStack)
}

private fun EntryProviderScope<ScreenNavKey>.qrHomeEntry(backStack: NavBackStack<ScreenNavKey>) {
    entry<QrHomeNavKey> {
        QrHomeScreen(
            navigateUp = backStack::removeLastOrNull,
            navigateToScan = { backStack.add(QrScanNavKey) },
            permissionManager = rememberPermissionManager(),
        )
    }
}

private fun EntryProviderScope<ScreenNavKey>.qrScanEntry(backStack: NavBackStack<ScreenNavKey>) {
    entry<QrScanNavKey> {
        QrScanScreen(navigateUp = backStack::removeLastOrNull)
    }
}
