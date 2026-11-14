package io.github.taetae98coding.diary.feature.login.ui

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavBackStack
import io.github.taetae98coding.diary.compose.credential.apple.AppleCredentialsManagerFactory
import io.github.taetae98coding.diary.compose.credential.google.GoogleCredentialsManagerFactory
import io.github.taetae98coding.diary.core.navigation.ScreenNavKey
import io.github.taetae98coding.diary.feature.login.api.LoginHomeNavKey
import io.github.taetae98coding.diary.feature.login.ui.home.LoginHomeScreen
import org.koin.compose.viewmodel.koinViewModel

internal fun EntryProviderScope<ScreenNavKey>.loginEntry(
    backStack: NavBackStack<ScreenNavKey>,
    googleCredentialsManagerFactory: GoogleCredentialsManagerFactory,
    appleCredentialsManagerFactory: AppleCredentialsManagerFactory,
) {
    loginHomeEntry(
        backStack = backStack,
        googleCredentialsManagerFactory = googleCredentialsManagerFactory,
        appleCredentialsManagerFactory = appleCredentialsManagerFactory,
    )
}

private fun EntryProviderScope<ScreenNavKey>.loginHomeEntry(
    backStack: NavBackStack<ScreenNavKey>,
    googleCredentialsManagerFactory: GoogleCredentialsManagerFactory,
    appleCredentialsManagerFactory: AppleCredentialsManagerFactory,
) {
    entry<LoginHomeNavKey> {
        LoginHomeScreen(
            navigateUp = backStack::removeLastOrNull,
            googleCredentialsManager = googleCredentialsManagerFactory.rememberGoogleCredentialsManager(),
            appleCredentialsManager = appleCredentialsManagerFactory.rememberAppleCredentialsManager(),
            viewModel = koinViewModel(),
        )
    }
}
