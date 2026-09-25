package io.github.taetae98coding.diary.data.file.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import io.github.taetae98coding.diary.core.file.api.datasource.FileLocalDataSource
import io.github.taetae98coding.diary.core.model.file.DiaryFile
import io.github.taetae98coding.diary.core.model.file.FileUri
import io.github.taetae98coding.diary.core.network.api.file.datasource.FileRemoteDataSource
import io.github.taetae98coding.diary.core.network.api.file.exception.FileTooLargeRemoteException
import io.github.taetae98coding.diary.data.core.paging.PAGE_SIZE
import io.github.taetae98coding.diary.data.file.mapper.toDomain
import io.github.taetae98coding.diary.data.file.paging.FilePagingSource
import io.github.taetae98coding.diary.domain.file.exception.FileTooLargeException
import io.github.taetae98coding.diary.domain.file.repository.FileRepository
import kotlinx.coroutines.flow.Flow
import org.koin.core.annotation.Factory

private const val UNKNOWN_MIME_TYPE = "application/octet-stream"

@Factory
internal class FileRepositoryImpl(
    private val fileLocalDataSource: FileLocalDataSource,
    private val fileRemoteDataSource: FileRemoteDataSource,
) : FileRepository {
    override fun page(): Flow<PagingData<DiaryFile>> =
        Pager(
            config =
                PagingConfig(
                    pageSize = PAGE_SIZE,
                    initialLoadSize = PAGE_SIZE,
                    enablePlaceholders = false,
                ),
            pagingSourceFactory = { FilePagingSource(fileRemoteDataSource = fileRemoteDataSource) },
        ).flow

    override suspend fun create(
        uri: FileUri,
        maxSize: Long,
    ): DiaryFile {
        val name = fileLocalDataSource.name(uri = uri)
        val size = fileLocalDataSource.size(uri = uri)

        if (size > maxSize) throw FileTooLargeException(message = "File is too large. size=$size, maxSize=$maxSize")

        val mimeType = fileLocalDataSource.mimeType(uri = uri).ifEmpty { UNKNOWN_MIME_TYPE }

        return try {
            fileRemoteDataSource
                .upload(
                    name = name,
                    mimeType = mimeType,
                    contentLength = size,
                    openContent = { fileLocalDataSource.openSource(uri = uri) },
                ).toDomain()
        } catch (exception: FileTooLargeRemoteException) {
            throw FileTooLargeException(message = exception.message, cause = exception)
        }
    }
}
