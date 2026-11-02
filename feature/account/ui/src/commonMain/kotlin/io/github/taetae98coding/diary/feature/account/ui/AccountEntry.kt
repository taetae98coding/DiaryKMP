package io.github.taetae98coding.diary.feature.account.ui

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavBackStack
import io.github.taetae98coding.diary.compose.credential.rememberAppleCredentialsManager
import io.github.taetae98coding.diary.core.navigation.ScreenNavKey
import io.github.taetae98coding.diary.feature.account.api.ProfileImageEditNavKey
import io.github.taetae98coding.diary.feature.account.api.ProfileImageViewerNavKey
import io.github.taetae98coding.diary.feature.account.api.ProfileNavKey
import io.github.taetae98coding.diary.feature.account.ui.photo.rememberPhotoPicker
import io.github.taetae98coding.diary.feature.account.ui.profile.ProfileScreen
import io.github.taetae98coding.diary.feature.account.ui.profileimage.ProfileImageEditScreen
import io.github.taetae98coding.diary.feature.account.ui.viewer.ProfileImageViewerScreen
import org.koin.compose.viewmodel.koinViewModel

public fun EntryProviderScope<ScreenNavKey>.accountEntry(backStack: NavBackStack<ScreenNavKey>) {
    profileEntry(backStack = backStack)
    profileImageEditEntry(backStack = backStack)
    profileImageViewerEntry(backStack = backStack)
}

private fun EntryProviderScope<ScreenNavKey>.profileEntry(backStack: NavBackStack<ScreenNavKey>) {
    entry<ProfileNavKey> {
        ProfileScreen(
            navigateUp = backStack::removeLastOrNull,
            navigateToProfileImageEdit = {
                backStack.add(ProfileImageEditNavKey)
            },
            appleCredentialsManager = rememberAppleCredentialsManager(),
            accountViewModel = koinViewModel(),
            deleteViewModel = koinViewModel(),
        )
    }
}

private fun EntryProviderScope<ScreenNavKey>.profileImageEditEntry(backStack: NavBackStack<ScreenNavKey>) {
    entry<ProfileImageEditNavKey> {
        ProfileImageEditScreen(
            navigateUp = backStack::removeLastOrNull,
            photoPicker = rememberPhotoPicker(),
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
