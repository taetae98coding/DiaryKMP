package io.github.taetae98coding.diary.data.file.paging

import io.github.taetae98coding.diary.core.network.api.file.datasource.FileRemoteDataSource
import io.github.taetae98coding.diary.core.network.api.file.entity.FileRemoteEntity
import org.koin.core.annotation.Single

// Paging은 새 세대의 첫 불러오기가 실패해도 이전 세대를 버리므로, 다시 불러올 첫 페이지를 먼저 받아 둔 뒤에만 세대를 바꾼다.
// 목록을 만드는 쪽과 다시 불러오는 쪽이 같은 목록을 보도록 앱에 하나만 두고, 목록마다 받아 둔 첫 페이지를 따로 든다.
// 계정이 바뀌면 새 목록이 만들어지므로, 앞선 목록에서 받은 첫 페이지가 새 목록에 들어가지 않는다.
@Single
internal class FilePagingSourceHolder(
    private val fileRemoteDataSource: FileRemoteDataSource,
) {
    private var latestList: FilePagingList? = null

    fun createList(): FilePagingList = FilePagingList(fileRemoteDataSource = fileRemoteDataSource).also { list -> latestList = list }

    fun latestList(): FilePagingList? = latestList

    fun isLatest(list: FilePagingList): Boolean = latestList === list
}

internal class FilePagingList(
    private val fileRemoteDataSource: FileRemoteDataSource,
) {
    private var latestSource: FilePagingSource? = null
    private var firstPage: FileFirstPage = FileFirstPage.NotFetched

    fun createSource(): FilePagingSource =
        FilePagingSource(fileRemoteDataSource = fileRemoteDataSource, firstPage = firstPage)
            .also { source ->
                firstPage = FileFirstPage.NotFetched
                latestSource = source
            }

    fun invalidate(firstPage: List<FileRemoteEntity>) {
        this.firstPage = FileFirstPage.Fetched(fileList = firstPage)
        latestSource?.invalidate()
    }
}
