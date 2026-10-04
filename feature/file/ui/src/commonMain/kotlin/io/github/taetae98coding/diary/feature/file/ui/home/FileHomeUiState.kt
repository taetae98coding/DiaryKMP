package io.github.taetae98coding.diary.feature.file.ui.home

import kotlin.uuid.Uuid

internal sealed interface FileHomeUiState {
    data object Loading : FileHomeUiState

    data object Guest : FileHomeUiState

    data class User(
        val accountId: Uuid,
        // Paging은 새 계정의 첫 페이지가 올 때까지 앞선 계정의 파일을 그대로 들고 있다.
        val isAccountChanging: Boolean = false,
    ) : FileHomeUiState
}
