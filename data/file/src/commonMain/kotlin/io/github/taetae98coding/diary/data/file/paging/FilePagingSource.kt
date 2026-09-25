package io.github.taetae98coding.diary.data.file.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import io.github.taetae98coding.diary.core.model.file.DiaryFile
import io.github.taetae98coding.diary.core.network.api.file.datasource.FileRemoteDataSource
import io.github.taetae98coding.diary.core.network.api.file.entity.FileCursorRemoteEntity
import io.github.taetae98coding.diary.data.file.mapper.toCursor
import io.github.taetae98coding.diary.data.file.mapper.toDomain
import kotlinx.coroutines.CancellationException

internal class FilePagingSource(
    private val fileRemoteDataSource: FileRemoteDataSource,
) : PagingSource<FileCursorRemoteEntity, DiaryFile>() {
    // 다시 불러오면 새로 올라온 파일이 맨 앞에 오므로 보던 자리가 아니라 언제나 처음부터 불러온다.
    override fun getRefreshKey(state: PagingState<FileCursorRemoteEntity, DiaryFile>): FileCursorRemoteEntity? = null

    // Paging은 실패를 LoadResult.Error로 받아야 재시도와 오류 상태를 만들 수 있어 예외를 결과로 바꾼다.
    override suspend fun load(params: LoadParams<FileCursorRemoteEntity>): LoadResult<FileCursorRemoteEntity, DiaryFile> =
        try {
            val fileList = fileRemoteDataSource.fetch(cursor = params.key, size = params.loadSize)

            LoadResult.Page(
                data = fileList.map { file -> file.toDomain() },
                prevKey = null,
                nextKey = if (fileList.size < params.loadSize) null else fileList.last().toCursor(),
            )
        } catch (exception: CancellationException) {
            throw exception
        } catch (throwable: Throwable) {
            LoadResult.Error(throwable)
        }
}
