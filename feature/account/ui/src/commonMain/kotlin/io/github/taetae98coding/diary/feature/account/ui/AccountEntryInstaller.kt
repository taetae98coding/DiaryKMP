package io.github.taetae98coding.diary.feature.account.ui

import androidx.navigation3.runtime.EntryProviderScope
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesIntoSet
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import dev.zacsweers.metro.binding
import io.github.taetae98coding.diary.compose.credential.apple.AppleCredentialsManagerFactory
import io.github.taetae98coding.diary.core.navigation.ScreenEntryInstaller
import io.github.taetae98coding.diary.core.navigation.ScreenNavKey
import io.github.taetae98coding.diary.core.navigation.ScreenNavigator
import io.github.taetae98coding.diary.feature.account.ui.photo.PhotoPickerFactory

@Inject
@SingleIn(AppScope::class)
@ContributesIntoSet(AppScope::class, binding = binding<ScreenEntryInstaller>())
internal class AccountEntryInstaller(
    private val appleCredentialsManagerFactory: AppleCredentialsManagerFactory,
    private val photoPickerFactory: PhotoPickerFactory,
) : ScreenEntryInstaller {
    override fun EntryProviderScope<ScreenNavKey>.install(navigator: ScreenNavigator) {
        accountEntry(
            backStack = navigator.backStack,
            appleCredentialsManagerFactory = appleCredentialsManagerFactory,
            photoPickerFactory = photoPickerFactory,
        )
    }
}
