package io.github.taetae98coding.diary.data.file.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import io.github.taetae98coding.diary.core.file.api.datasource.FileLocalDataSource
import io.github.taetae98coding.diary.core.model.file.ContinuedFileUpload
import io.github.taetae98coding.diary.core.model.file.ContinuedFileUploadResult
import io.github.taetae98coding.diary.core.model.file.DiaryFile
import io.github.taetae98coding.diary.core.model.file.FileUploadSource
import io.github.taetae98coding.diary.core.model.file.FileUri
import io.github.taetae98coding.diary.core.network.api.file.datasource.FileRemoteDataSource
import io.github.taetae98coding.diary.core.network.api.file.exception.FileTooLargeRemoteException
import io.github.taetae98coding.diary.data.core.paging.PAGE_SIZE
import io.github.taetae98coding.diary.data.file.mapper.toDomain
import io.github.taetae98coding.diary.data.file.paging.FilePagingSourceHolder
import io.github.taetae98coding.diary.domain.file.exception.FileTooLargeException
import io.github.taetae98coding.diary.domain.file.exception.FileUnreadableException
import io.github.taetae98coding.diary.domain.file.repository.FileRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.koin.core.annotation.Factory

private const val UNKNOWN_MIME_TYPE = "application/octet-stream"

@Factory
internal class FileRepositoryImpl(
    private val fileLocalDataSource: FileLocalDataSource,
    private val fileRemoteDataSource: FileRemoteDataSource,
    private val filePagingSourceHolder: FilePagingSourceHolder,
) : FileRepository {
    override fun page(): Flow<PagingData<DiaryFile>> =
        Pager(
            config =
                PagingConfig(
                    pageSize = PAGE_SIZE,
                    initialLoadSize = PAGE_SIZE,
                    enablePlaceholders = false,
                ),
            pagingSourceFactory = filePagingSourceHolder.createList()::createSource,
        ).flow

    // 받는 동안 다른 계정의 목록으로 바뀌었으면, 받은 첫 페이지는 지금 목록의 것이 아니므로 버린다.
    override suspend fun refresh() {
        val list = filePagingSourceHolder.latestList() ?: return
        val firstPage = fileRemoteDataSource.fetch(cursor = null, size = PAGE_SIZE)

        if (filePagingSourceHolder.isLatest(list = list)) list.invalidate(firstPage = firstPage)
    }

    // 이름을 읽은 뒤 나머지 정보를 읽지 못해도 실패에 이름을 실어, 호출자가 어느 파일이 실패했는지 알 수 있게 한다.
    override suspend fun findSource(uri: FileUri): FileUploadSource {
        val name = readSourceInfo(name = "") { fileLocalDataSource.name(uri = uri) }

        return readSourceInfo(name = name) {
            FileUploadSource(
                uri = uri,
                name = name,
                mimeType = fileLocalDataSource.mimeType(uri = uri).ifEmpty { UNKNOWN_MIME_TYPE },
                size = fileLocalDataSource.size(uri = uri),
            )
        }
    }

    private suspend fun <T> readSourceInfo(
        name: String,
        read: suspend () -> T,
    ): T =
        try {
            read()
        } catch (exception: CancellationException) {
            throw exception
        } catch (throwable: Throwable) {
            throw FileUnreadableException(name = name, cause = throwable)
        }

    override suspend fun create(
        source: FileUploadSource,
        onSent: (sentBytes: Long) -> Unit,
    ): DiaryFile =
        try {
            fileRemoteDataSource
                .upload(
                    name = source.name,
                    mimeType = source.mimeType,
                    contentLength = source.size,
                    openContent = { fileLocalDataSource.openSource(uri = source.uri) },
                    onSent = onSent,
                ).toDomain()
        } catch (exception: FileTooLargeRemoteException) {
            throw FileTooLargeException(message = exception.message, cause = exception)
        }

    override suspend fun addUploadSource(uri: FileUri) {
        fileLocalDataSource.retain(uri = uri)
    }

    override suspend fun removeUploadSource(uri: FileUri) {
        fileLocalDataSource.release(uri = uri)
    }

    override suspend fun deleteLeftoverUploadSources() {
        fileLocalDataSource.deleteLeftoverCopies()
    }

    override fun getContinuedUpload(): Flow<ContinuedFileUpload?> =
        fileRemoteDataSource
            .getContinuedUpload()
            .map { upload -> upload?.toDomain() }

    override fun getContinuedUploadResult(): Flow<ContinuedFileUploadResult> =
        fileRemoteDataSource
            .getContinuedUploadResult()
            .map { result -> result.toDomain() }

    override suspend fun deleteContinuedUpload() {
        fileRemoteDataSource.cancelContinuedUpload()
    }
}
