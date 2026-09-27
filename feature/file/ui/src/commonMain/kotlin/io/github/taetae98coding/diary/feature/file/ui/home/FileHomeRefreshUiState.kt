package io.github.taetae98coding.diary.feature.file.ui.home

import kotlin.uuid.Uuid

internal data class FileHomeRefreshUiState(
    val isRefreshing: Boolean = false,
    val scrollToTop: FileHomeScrollToTop = FileHomeScrollToTop.None,
)

internal sealed interface FileHomeScrollToTop {
    data object None : FileHomeScrollToTop

    // 새 목록이 나타나기 전에 처음으로 옮기면 LazyList가 앞선 첫 항목의 키를 따라 그 자리로 되돌아가므로, 첫 파일이 바뀐 뒤에 옮긴다.
    data class AfterFirstFileChanges(
        val firstFileIdBefore: Uuid,
    ) : FileHomeScrollToTop
}
