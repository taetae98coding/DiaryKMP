package io.github.taetae98coding.diary.feature.account.ui

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavBackStack
import io.github.taetae98coding.diary.compose.credential.apple.AppleCredentialsManagerFactory
import io.github.taetae98coding.diary.core.navigation.ScreenNavKey
import io.github.taetae98coding.diary.feature.account.api.ProfileImageEditNavKey
import io.github.taetae98coding.diary.feature.account.api.ProfileImageViewerNavKey
import io.github.taetae98coding.diary.feature.account.api.ProfileNavKey
import io.github.taetae98coding.diary.feature.account.ui.photo.PhotoPickerFactory
import io.github.taetae98coding.diary.feature.account.ui.profile.ProfileScreen
import io.github.taetae98coding.diary.feature.account.ui.profileimage.ProfileImageEditScreen
import io.github.taetae98coding.diary.feature.account.ui.viewer.ProfileImageViewerScreen
import org.koin.compose.viewmodel.koinViewModel

internal fun EntryProviderScope<ScreenNavKey>.accountEntry(
    backStack: NavBackStack<ScreenNavKey>,
    appleCredentialsManagerFactory: AppleCredentialsManagerFactory,
    photoPickerFactory: PhotoPickerFactory,
) {
    profileEntry(backStack = backStack, appleCredentialsManagerFactory = appleCredentialsManagerFactory)
    profileImageEditEntry(backStack = backStack, photoPickerFactory = photoPickerFactory)
    profileImageViewerEntry(backStack = backStack)
}

private fun EntryProviderScope<ScreenNavKey>.profileEntry(
    backStack: NavBackStack<ScreenNavKey>,
    appleCredentialsManagerFactory: AppleCredentialsManagerFactory,
) {
    entry<ProfileNavKey> {
        ProfileScreen(
            navigateUp = backStack::removeLastOrNull,
            navigateToProfileImageEdit = {
                backStack.add(ProfileImageEditNavKey)
            },
            appleCredentialsManager = appleCredentialsManagerFactory.rememberAppleCredentialsManager(),
            accountViewModel = koinViewModel(),
            deleteViewModel = koinViewModel(),
        )
    }
}

private fun EntryProviderScope<ScreenNavKey>.profileImageEditEntry(
    backStack: NavBackStack<ScreenNavKey>,
    photoPickerFactory: PhotoPickerFactory,
) {
    entry<ProfileImageEditNavKey> {
        ProfileImageEditScreen(
            navigateUp = backStack::removeLastOrNull,
            photoPicker = photoPickerFactory.rememberPhotoPicker(),
            viewModel = koinViewModel(),
        )
    }
}

private fun EntryProviderScope<ScreenNavKey>.profileImageViewerEntry(backStack: NavBackStack<ScreenNavKey>) {
    entry<ProfileImageViewerNavKey> { key ->
        ProfileImageViewerScreen(
            navigateUp = backStack::removeLastOrNull,
            imageUrl = key.imageUrl,
        )
    }
}
