package io.github.taetae98coding.diary.feature.login.ui

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavBackStack
import io.github.taetae98coding.diary.feature.login.api.LoginHomeNavKey
import io.github.taetae98coding.diary.feature.login.ui.credential.rememberAppleCredentialsManager
import io.github.taetae98coding.diary.feature.login.ui.credential.rememberGoogleCredentialsManager
import io.github.taetae98coding.diary.feature.login.ui.home.LoginHomeScreen
import io.github.taetae98coding.diary.library.navigation3.ScreenNavKey
import org.koin.compose.viewmodel.koinViewModel

public fun EntryProviderScope<ScreenNavKey>.loginEntry(backStack: NavBackStack<ScreenNavKey>) {
    loginHomeEntry(backStack = backStack)
}

private fun EntryProviderScope<ScreenNavKey>.loginHomeEntry(backStack: NavBackStack<ScreenNavKey>) {
    entry<LoginHomeNavKey> {
        LoginHomeScreen(
            navigateUp = backStack::removeLastOrNull,
            googleCredentialsManager = rememberGoogleCredentialsManager(),
            appleCredentialsManager = rememberAppleCredentialsManager(),
            viewModel = koinViewModel(),
        )
    }
}
