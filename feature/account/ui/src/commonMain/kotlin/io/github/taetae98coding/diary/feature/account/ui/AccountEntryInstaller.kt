package io.github.taetae98coding.diary.feature.account.ui

import androidx.navigation3.runtime.EntryProviderScope
import io.github.taetae98coding.diary.compose.credential.apple.AppleCredentialsManagerFactory
import io.github.taetae98coding.diary.core.navigation.ScreenEntryInstaller
import io.github.taetae98coding.diary.core.navigation.ScreenNavKey
import io.github.taetae98coding.diary.core.navigation.ScreenNavigator
import io.github.taetae98coding.diary.feature.account.ui.photo.PhotoPickerFactory
import org.koin.core.annotation.Single

@Single(binds = [ScreenEntryInstaller::class])
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
