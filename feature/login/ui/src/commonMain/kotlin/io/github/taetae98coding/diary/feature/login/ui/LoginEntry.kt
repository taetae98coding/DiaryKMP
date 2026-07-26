package io.github.taetae98coding.diary.feature.login.ui

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import io.github.taetae98coding.diary.feature.login.api.LoginHomeNavKey
import io.github.taetae98coding.diary.feature.login.ui.credential.rememberAppleCredentialsManager
import io.github.taetae98coding.diary.feature.login.ui.credential.rememberGoogleCredentialsManager
import io.github.taetae98coding.diary.feature.login.ui.home.LoginHomeScreen
import org.koin.compose.viewmodel.koinViewModel

public fun EntryProviderScope<NavKey>.loginEntry(backStack: NavBackStack<NavKey>) {
    loginHomeEntry(backStack = backStack)
}

private fun EntryProviderScope<NavKey>.loginHomeEntry(backStack: NavBackStack<NavKey>) {
    entry<LoginHomeNavKey> {
        LoginHomeScreen(
            navigateUp = backStack::removeLastOrNull,
            googleCredentialsManager = rememberGoogleCredentialsManager(),
            appleCredentialsManager = rememberAppleCredentialsManager(),
            viewModel = koinViewModel(),
        )
    }
}
