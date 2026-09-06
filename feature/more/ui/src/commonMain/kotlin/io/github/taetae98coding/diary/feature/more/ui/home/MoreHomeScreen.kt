package io.github.taetae98coding.diary.feature.more.ui.home

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.taetae98coding.diary.feature.more.ui.photo.PhotoPicker
import kotlinx.coroutines.launch

@Composable
internal fun MoreHomeScreen(
    navigateToChecklist: () -> Unit,
    navigateToContact: () -> Unit,
    navigateToDDay: () -> Unit,
    navigateToFile: () -> Unit,
    navigateToHoliday: () -> Unit,
    navigateToLogin: () -> Unit,
    navigateToPlace: () -> Unit,
    navigateToPlaylist: () -> Unit,
    navigateToQr: () -> Unit,
    navigateToSearch: () -> Unit,
    navigateToSetting: () -> Unit,
    navigateToWeb: () -> Unit,
    photoPicker: PhotoPicker,
    viewModel: MoreHomeAccountViewModel,
    modifier: Modifier = Modifier,
) {
    val accountUiState by viewModel.uiState.collectAsStateWithLifecycle()
    val coroutineScope = rememberCoroutineScope()

    MoreHomeScaffold(
        accountUiStateProvider = { accountUiState },
        onEvent = { event ->
            when (event) {
                is MoreHomeScaffoldEvent.ClickProfile -> {
                    coroutineScope.launch {
                        photoPicker.open()?.let { uri -> viewModel.changeProfileImage(uri = uri) }
                    }
                }

                is MoreHomeScaffoldEvent.ClickSetting -> {
                    navigateToSetting()
                }

                is MoreHomeScaffoldEvent.ClickSignIn -> {
                    navigateToLogin()
                }

                is MoreHomeScaffoldEvent.ClickSignOut -> {
                    viewModel.signOut()
                }

                is MoreHomeScaffoldEvent.ClickMenu -> {
                    navigateToMenu(
                        menu = event.menu,
                        navigateToChecklist = navigateToChecklist,
                        navigateToContact = navigateToContact,
                        navigateToDDay = navigateToDDay,
                        navigateToFile = navigateToFile,
                        navigateToHoliday = navigateToHoliday,
                        navigateToPlace = navigateToPlace,
                        navigateToPlaylist = navigateToPlaylist,
                        navigateToQr = navigateToQr,
                        navigateToSearch = navigateToSearch,
                        navigateToWeb = navigateToWeb,
                    )
                }
            }
        },
        modifier = modifier,
    )
}

private fun navigateToMenu(
    menu: MoreHomeMenu,
    navigateToChecklist: () -> Unit,
    navigateToContact: () -> Unit,
    navigateToDDay: () -> Unit,
    navigateToFile: () -> Unit,
    navigateToHoliday: () -> Unit,
    navigateToPlace: () -> Unit,
    navigateToPlaylist: () -> Unit,
    navigateToQr: () -> Unit,
    navigateToSearch: () -> Unit,
    navigateToWeb: () -> Unit,
) {
    when (menu) {
        MoreHomeMenu.SEARCH -> navigateToSearch()
        MoreHomeMenu.WEB -> navigateToWeb()
        MoreHomeMenu.PLACE -> navigateToPlace()
        MoreHomeMenu.HOLIDAY -> navigateToHoliday()
        MoreHomeMenu.CONTACT -> navigateToContact()
        MoreHomeMenu.QR -> navigateToQr()
        MoreHomeMenu.D_DAY -> navigateToDDay()
        MoreHomeMenu.CHECKLIST -> navigateToChecklist()
        MoreHomeMenu.FILE -> navigateToFile()
        MoreHomeMenu.PLAYLIST -> navigateToPlaylist()
    }
}
