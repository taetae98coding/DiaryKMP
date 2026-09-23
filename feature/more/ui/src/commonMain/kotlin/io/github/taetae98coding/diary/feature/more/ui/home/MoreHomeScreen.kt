package io.github.taetae98coding.diary.feature.more.ui.home

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.taetae98coding.diary.feature.more.ui.home.account.MoreHomeAccountViewModel
import io.github.taetae98coding.diary.feature.more.ui.home.menu.MoreHomeMenu
import io.github.taetae98coding.diary.feature.more.ui.home.refresh.MoreHomeRefreshViewModel
import io.github.taetae98coding.diary.feature.more.ui.home.signout.MoreHomeSignOutViewModel

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
    navigateToProfileImageEdit: () -> Unit,
    navigateToQr: () -> Unit,
    navigateToSearch: () -> Unit,
    navigateToSetting: () -> Unit,
    navigateToWeb: () -> Unit,
    accountViewModel: MoreHomeAccountViewModel,
    signOutViewModel: MoreHomeSignOutViewModel,
    refreshViewModel: MoreHomeRefreshViewModel,
    modifier: Modifier = Modifier,
) {
    val accountUiState by accountViewModel.uiState.collectAsStateWithLifecycle()
    val signOutUiState by signOutViewModel.uiState.collectAsStateWithLifecycle()

    RefreshUserDataEffect(refreshViewModel = refreshViewModel)

    MoreHomeScaffold(
        accountUiStateProvider = { accountUiState },
        signOutUiStateProvider = { signOutUiState },
        onEvent = { event ->
            when (event) {
                is MoreHomeScaffoldEvent.ClickProfile -> {
                    navigateToProfileImageEdit()
                }

                is MoreHomeScaffoldEvent.ClickSetting -> {
                    navigateToSetting()
                }

                is MoreHomeScaffoldEvent.ClickSignIn -> {
                    navigateToLogin()
                }

                is MoreHomeScaffoldEvent.ClickSignOut -> {
                    signOutViewModel.signOut()
                }

                is MoreHomeScaffoldEvent.ConfirmSignOut -> {
                    signOutViewModel.confirmSignOut()
                }

                is MoreHomeScaffoldEvent.CancelSignOut -> {
                    signOutViewModel.cancelSignOut()
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

// 화면이 표시될 때마다 다시 확인하므로 진입뿐 아니라 다른 화면에서 돌아올 때도 요청한다.
@Composable
private fun RefreshUserDataEffect(refreshViewModel: MoreHomeRefreshViewModel) {
    LifecycleEventEffect(Lifecycle.Event.ON_START) {
        refreshViewModel.refresh()
    }
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
