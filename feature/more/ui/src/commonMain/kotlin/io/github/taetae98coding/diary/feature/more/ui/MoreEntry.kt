package io.github.taetae98coding.diary.feature.more.ui

import androidx.lifecycle.compose.dropUnlessResumed
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavBackStack
import dev.zacsweers.metrox.viewmodel.assistedMetroViewModel
import dev.zacsweers.metrox.viewmodel.metroViewModel
import io.github.taetae98coding.diary.compose.core.lifecycle.dropUnlessResumedWithArg
import io.github.taetae98coding.diary.core.navigation.ScreenNavKey
import io.github.taetae98coding.diary.feature.account.api.ProfileImageViewerNavKey
import io.github.taetae98coding.diary.feature.account.api.ProfileNavKey
import io.github.taetae98coding.diary.feature.checklist.api.ChecklistHomeNavKey
import io.github.taetae98coding.diary.feature.contact.api.ContactHomeNavKey
import io.github.taetae98coding.diary.feature.dday.api.DDayHomeNavKey
import io.github.taetae98coding.diary.feature.file.api.FileHomeNavKey
import io.github.taetae98coding.diary.feature.holiday.api.HolidayHomeNavKey
import io.github.taetae98coding.diary.feature.login.api.LoginHomeNavKey
import io.github.taetae98coding.diary.feature.more.api.MoreHomeNavKey
import io.github.taetae98coding.diary.feature.more.ui.home.MoreHomeScreen
import io.github.taetae98coding.diary.feature.more.ui.home.signout.MoreHomeSignOutViewModel
import io.github.taetae98coding.diary.feature.place.api.PlaceHomeNavKey
import io.github.taetae98coding.diary.feature.playlist.api.PlaylistHomeNavKey
import io.github.taetae98coding.diary.feature.qr.api.QrHomeNavKey
import io.github.taetae98coding.diary.feature.search.api.SearchHomeNavKey
import io.github.taetae98coding.diary.feature.setting.api.SettingHomeNavKey
import io.github.taetae98coding.diary.feature.web.api.WebHomeNavKey

internal fun EntryProviderScope<ScreenNavKey>.moreEntry(backStack: NavBackStack<ScreenNavKey>) {
    moreHomeEntry(backStack = backStack)
}

private fun EntryProviderScope<ScreenNavKey>.moreHomeEntry(backStack: NavBackStack<ScreenNavKey>) {
    entry<MoreHomeNavKey> {
        MoreHomeScreen(
            navigateToChecklist = dropUnlessResumed { backStack.add(ChecklistHomeNavKey) },
            navigateToContact = dropUnlessResumed { backStack.add(ContactHomeNavKey) },
            navigateToDDay = dropUnlessResumed { backStack.add(DDayHomeNavKey) },
            navigateToFile = dropUnlessResumed { backStack.add(FileHomeNavKey) },
            navigateToHoliday = dropUnlessResumed { backStack.add(HolidayHomeNavKey) },
            navigateToLogin = dropUnlessResumed { backStack.add(LoginHomeNavKey) },
            navigateToPlace = dropUnlessResumed { backStack.add(PlaceHomeNavKey) },
            navigateToPlaylist = dropUnlessResumed { backStack.add(PlaylistHomeNavKey) },
            navigateToProfile = dropUnlessResumed { backStack.add(ProfileNavKey) },
            navigateToProfileImageViewer = dropUnlessResumedWithArg { imageUrl -> backStack.add(ProfileImageViewerNavKey(imageUrl = imageUrl)) },
            navigateToQr = dropUnlessResumed { backStack.add(QrHomeNavKey) },
            navigateToSearch = dropUnlessResumed { backStack.add(SearchHomeNavKey()) },
            navigateToSetting = dropUnlessResumed { backStack.add(SettingHomeNavKey) },
            navigateToWeb = dropUnlessResumed { backStack.add(WebHomeNavKey) },
            accountViewModel = metroViewModel(),
            signOutViewModel = assistedMetroViewModel<MoreHomeSignOutViewModel>(),
            refreshViewModel = metroViewModel(),
        )
    }
}
