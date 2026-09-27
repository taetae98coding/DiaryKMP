package io.github.taetae98coding.diary.data.file.paging

import io.github.taetae98coding.diary.core.network.api.file.entity.FileRemoteEntity

internal sealed interface FileFirstPage {
    data object NotFetched : FileFirstPage

    data class Fetched(
        val fileList: List<FileRemoteEntity>,
    ) : FileFirstPage
}
