package io.github.taetae98coding.diary.feature.setting.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.taetae98coding.diary.domain.browser.usecase.ReadChromeSessionImportSupportUseCase
import io.github.taetae98coding.diary.domain.file.usecase.ReadFileDownloadDirectorySupportUseCase
import io.github.taetae98coding.diary.domain.playlist.usecase.ReadMusicDownloadSupportUseCase
import io.github.taetae98coding.diary.library.coroutines.flow.WhileUiSubscribed
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import org.koin.core.annotation.KoinViewModel

@KoinViewModel
internal class SettingHomeViewModel(
    readChromeSessionImportSupportUseCase: ReadChromeSessionImportSupportUseCase,
    readMusicDownloadSupportUseCase: ReadMusicDownloadSupportUseCase,
    readFileDownloadDirectorySupportUseCase: ReadFileDownloadDirectorySupportUseCase,
) : ViewModel() {
    val uiState: StateFlow<SettingHomeUiState> =
        flow {
            val isChromeSessionImportSupported = readChromeSessionImportSupportUseCase(parameter = Unit).getOrNull()
            val isMusicDownloadSupported = readMusicDownloadSupportUseCase(parameter = Unit).getOrNull()
            val isFileDownloadDirectorySupported = readFileDownloadDirectorySupportUseCase(parameter = Unit).getOrNull()

            if (isChromeSessionImportSupported == null || isMusicDownloadSupported == null || isFileDownloadDirectorySupported == null) {
                emit(SettingHomeUiState.Loading)
            } else {
                emit(
                    SettingHomeUiState.Content(
                        itemList =
                            settingHomeItemList.filter { item ->
                                item.isProvided(
                                    isChromeSessionImportSupported = isChromeSessionImportSupported,
                                    isMusicDownloadSupported = isMusicDownloadSupported,
                                    isFileDownloadDirectorySupported = isFileDownloadDirectorySupported,
                                )
                            },
                    ),
                )
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileUiSubscribed,
            initialValue = SettingHomeUiState.Loading,
        )

    private fun SettingHomeItem.isProvided(
        isChromeSessionImportSupported: Boolean,
        isMusicDownloadSupported: Boolean,
        isFileDownloadDirectorySupported: Boolean,
    ): Boolean =
        when (this) {
            SettingHomeItem.HOLIDAY, SettingHomeItem.MAP, SettingHomeItem.GEMINI -> true
            SettingHomeItem.BROWSER -> isChromeSessionImportSupported
            SettingHomeItem.PLAYLIST -> isMusicDownloadSupported
            SettingHomeItem.FILE -> isFileDownloadDirectorySupported
        }
}
