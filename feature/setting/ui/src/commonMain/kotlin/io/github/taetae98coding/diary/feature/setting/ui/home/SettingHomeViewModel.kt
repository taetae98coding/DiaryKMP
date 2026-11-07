package io.github.taetae98coding.diary.feature.setting.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.taetae98coding.diary.domain.file.usecase.ReadFileDownloadDirectorySupportUseCase
import io.github.taetae98coding.diary.domain.mcp.usecase.ReadMcpServerSupportUseCase
import io.github.taetae98coding.diary.library.coroutines.flow.WhileUiSubscribed
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import org.koin.core.annotation.KoinViewModel

@KoinViewModel
internal class SettingHomeViewModel(
    readFileDownloadDirectorySupportUseCase: ReadFileDownloadDirectorySupportUseCase,
    readMcpServerSupportUseCase: ReadMcpServerSupportUseCase,
) : ViewModel() {
    val uiState: StateFlow<SettingHomeUiState> =
        flow {
            val isFileDownloadDirectorySupported = readFileDownloadDirectorySupportUseCase(parameter = Unit).getOrNull()
            val isMcpServerSupported = readMcpServerSupportUseCase(parameter = Unit).getOrNull()

            if (isFileDownloadDirectorySupported == null || isMcpServerSupported == null) {
                emit(SettingHomeUiState.Loading)
            } else {
                emit(
                    SettingHomeUiState.Content(
                        itemList =
                            settingHomeItemList.filter { item ->
                                item.isProvided(
                                    isFileDownloadDirectorySupported = isFileDownloadDirectorySupported,
                                    isMcpServerSupported = isMcpServerSupported,
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
        isFileDownloadDirectorySupported: Boolean,
        isMcpServerSupported: Boolean,
    ): Boolean =
        when (this) {
            SettingHomeItem.HOLIDAY, SettingHomeItem.MAP, SettingHomeItem.GEMINI -> true
            SettingHomeItem.FILE -> isFileDownloadDirectorySupported
            SettingHomeItem.MCP -> isMcpServerSupported
        }
}
