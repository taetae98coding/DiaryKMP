package io.github.taetae98coding.diary.feature.more.ui

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavBackStack
import io.github.taetae98coding.diary.feature.checklist.api.ChecklistHomeNavKey
import io.github.taetae98coding.diary.feature.contact.api.ContactHomeNavKey
import io.github.taetae98coding.diary.feature.dday.api.DDayHomeNavKey
import io.github.taetae98coding.diary.feature.file.api.FileHomeNavKey
import io.github.taetae98coding.diary.feature.holiday.api.HolidayHomeNavKey
import io.github.taetae98coding.diary.feature.login.api.LoginHomeNavKey
import io.github.taetae98coding.diary.feature.more.api.MoreHomeNavKey
import io.github.taetae98coding.diary.feature.more.ui.home.MoreHomeScreen
import io.github.taetae98coding.diary.feature.more.ui.photo.rememberPhotoPicker
import io.github.taetae98coding.diary.feature.place.api.PlaceHomeNavKey
import io.github.taetae98coding.diary.feature.playlist.api.PlaylistHomeNavKey
import io.github.taetae98coding.diary.feature.qr.api.QrHomeNavKey
import io.github.taetae98coding.diary.feature.search.api.SearchHomeNavKey
import io.github.taetae98coding.diary.feature.setting.api.SettingHomeNavKey
import io.github.taetae98coding.diary.feature.web.api.WebHomeNavKey
import io.github.taetae98coding.diary.library.navigation3.ScreenNavKey
import org.koin.compose.viewmodel.koinViewModel

public fun EntryProviderScope<ScreenNavKey>.moreEntry(backStack: NavBackStack<ScreenNavKey>) {
    moreHomeEntry(backStack = backStack)
}

private fun EntryProviderScope<ScreenNavKey>.moreHomeEntry(backStack: NavBackStack<ScreenNavKey>) {
    entry<MoreHomeNavKey> {
        MoreHomeScreen(
            navigateToChecklist = {
                backStack.add(ChecklistHomeNavKey)
            },
            navigateToContact = {
                backStack.add(ContactHomeNavKey)
            },
            navigateToDDay = {
                backStack.add(DDayHomeNavKey)
            },
            navigateToFile = {
                backStack.add(FileHomeNavKey)
            },
            navigateToHoliday = {
                backStack.add(HolidayHomeNavKey)
            },
            navigateToLogin = {
                backStack.add(LoginHomeNavKey)
            },
            navigateToPlace = {
                backStack.add(PlaceHomeNavKey)
            },
            navigateToPlaylist = {
                backStack.add(PlaylistHomeNavKey)
            },
            navigateToQr = {
                backStack.add(QrHomeNavKey)
            },
            navigateToSearch = {
                backStack.add(SearchHomeNavKey())
            },
            navigateToSetting = {
                backStack.add(SettingHomeNavKey)
            },
            navigateToWeb = {
                backStack.add(WebHomeNavKey)
            },
            photoPicker = rememberPhotoPicker(),
            viewModel = koinViewModel(),
        )
    }
}
