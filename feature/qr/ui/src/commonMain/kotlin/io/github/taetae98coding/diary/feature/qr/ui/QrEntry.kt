package io.github.taetae98coding.diary.feature.qr.ui

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavBackStack
import io.github.taetae98coding.diary.core.navigation.ScreenNavKey
import io.github.taetae98coding.diary.feature.qr.api.QrHomeNavKey
import io.github.taetae98coding.diary.feature.qr.ui.home.QrHomeScreen

public fun EntryProviderScope<ScreenNavKey>.qrEntry(backStack: NavBackStack<ScreenNavKey>) {
    qrHomeEntry(backStack = backStack)
}

private fun EntryProviderScope<ScreenNavKey>.qrHomeEntry(backStack: NavBackStack<ScreenNavKey>) {
    entry<QrHomeNavKey> {
        QrHomeScreen(navigateUp = backStack::removeLastOrNull)
    }
}
