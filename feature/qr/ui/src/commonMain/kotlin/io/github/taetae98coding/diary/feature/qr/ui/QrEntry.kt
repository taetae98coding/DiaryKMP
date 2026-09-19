package io.github.taetae98coding.diary.feature.qr.ui

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavBackStack
import io.github.taetae98coding.diary.feature.qr.api.QrHomeNavKey
import io.github.taetae98coding.diary.feature.qr.ui.home.QrHomeScreen
import io.github.taetae98coding.diary.library.navigation3.ScreenNavKey

public fun EntryProviderScope<ScreenNavKey>.qrEntry(backStack: NavBackStack<ScreenNavKey>) {
    qrHomeEntry(backStack = backStack)
}

private fun EntryProviderScope<ScreenNavKey>.qrHomeEntry(backStack: NavBackStack<ScreenNavKey>) {
    entry<QrHomeNavKey> {
        QrHomeScreen(navigateUp = backStack::removeLastOrNull)
    }
}
