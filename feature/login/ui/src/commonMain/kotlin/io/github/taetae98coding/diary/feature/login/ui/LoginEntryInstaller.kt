package io.github.taetae98coding.diary.feature.login.ui

import androidx.navigation3.runtime.EntryProviderScope
import io.github.taetae98coding.diary.compose.credential.apple.AppleCredentialsManagerFactory
import io.github.taetae98coding.diary.compose.credential.google.GoogleCredentialsManagerFactory
import io.github.taetae98coding.diary.core.navigation.ScreenEntryInstaller
import io.github.taetae98coding.diary.core.navigation.ScreenNavKey
import io.github.taetae98coding.diary.core.navigation.ScreenNavigator
import org.koin.core.annotation.Single

@Single(binds = [ScreenEntryInstaller::class])
internal class LoginEntryInstaller(
    private val googleCredentialsManagerFactory: GoogleCredentialsManagerFactory,
    private val appleCredentialsManagerFactory: AppleCredentialsManagerFactory,
) : ScreenEntryInstaller {
    override fun EntryProviderScope<ScreenNavKey>.install(navigator: ScreenNavigator) {
        loginEntry(
            backStack = navigator.backStack,
            googleCredentialsManagerFactory = googleCredentialsManagerFactory,
            appleCredentialsManagerFactory = appleCredentialsManagerFactory,
        )
    }
}
