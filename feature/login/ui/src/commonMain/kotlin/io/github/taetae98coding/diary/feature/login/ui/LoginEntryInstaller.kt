package io.github.taetae98coding.diary.feature.login.ui

import androidx.navigation3.runtime.EntryProviderScope
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesIntoSet
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import dev.zacsweers.metro.binding
import io.github.taetae98coding.diary.compose.credential.apple.AppleCredentialsManagerFactory
import io.github.taetae98coding.diary.compose.credential.google.GoogleCredentialsManagerFactory
import io.github.taetae98coding.diary.core.navigation.ScreenEntryInstaller
import io.github.taetae98coding.diary.core.navigation.ScreenNavKey
import io.github.taetae98coding.diary.core.navigation.ScreenNavigator

@Inject
@SingleIn(AppScope::class)
@ContributesIntoSet(AppScope::class, binding = binding<ScreenEntryInstaller>())
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
