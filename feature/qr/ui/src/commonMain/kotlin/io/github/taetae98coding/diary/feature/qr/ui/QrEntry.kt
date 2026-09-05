package io.github.taetae98coding.diary.feature.qr.ui

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import io.github.taetae98coding.diary.feature.qr.api.QrHomeNavKey
import io.github.taetae98coding.diary.feature.qr.ui.home.QrHomeScreen

public fun EntryProviderScope<NavKey>.qrEntry(backStack: NavBackStack<NavKey>) {
    qrHomeEntry(backStack = backStack)
}

private fun EntryProviderScope<NavKey>.qrHomeEntry(backStack: NavBackStack<NavKey>) {
    entry<QrHomeNavKey> {
        QrHomeScreen(navigateUp = backStack::removeLastOrNull)
    }
}
