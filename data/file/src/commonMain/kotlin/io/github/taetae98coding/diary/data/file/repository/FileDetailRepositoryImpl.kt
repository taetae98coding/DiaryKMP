package io.github.taetae98coding.diary.data.file.repository

import io.github.taetae98coding.diary.core.file.api.datasource.FileDownloadLocalDataSource
import io.github.taetae98coding.diary.core.file.api.exception.FileDownloadDirectoryUnavailableException
import io.github.taetae98coding.diary.core.model.file.DiaryFile
import io.github.taetae98coding.diary.core.model.file.DiaryFileDetail
import io.github.taetae98coding.diary.core.model.file.FileDownloadDirectory
import io.github.taetae98coding.diary.core.model.file.FileDownloadResult
import io.github.taetae98coding.diary.core.network.api.file.datasource.FileRemoteDataSource
import io.github.taetae98coding.diary.data.file.mapper.toDomain
import io.github.taetae98coding.diary.data.file.update.FileUpdateHolder
import io.github.taetae98coding.diary.domain.file.repository.FileDetailRepository
import kotlinx.coroutines.flow.Flow
import org.koin.core.annotation.Factory
import kotlin.uuid.Uuid
import io.github.taetae98coding.diary.domain.file.exception.FileDownloadDirectoryUnavailableException as DomainFileDownloadDirectoryUnavailableException

@Factory
internal class FileDetailRepositoryImpl(
    private val fileRemoteDataSource: FileRemoteDataSource,
    private val fileUpdateHolder: FileUpdateHolder,
    private val fileDownloadLocalDataSource: FileDownloadLocalDataSource,
) : FileDetailRepository {
    override fun getUpdated(): Flow<Map<Uuid, DiaryFile>> = fileUpdateHolder.updated

    override suspend fun fetch(id: Uuid): DiaryFile = fileRemoteDataSource.read(id = id).toDomain()

    override suspend fun updateDetail(
        id: Uuid,
        detail: DiaryFileDetail,
    ): DiaryFile =
        fileRemoteDataSource
            .update(
                id = id,
                name = detail.name,
                title = detail.title,
                description = detail.description,
            ).toDomain()
            .also(fileUpdateHolder::put)

    override suspend fun download(
        id: Uuid,
        directory: FileDownloadDirectory?,
        nameCandidate: (index: Int) -> String,
    ): FileDownloadResult =
        try {
            fileDownloadLocalDataSource.save(
                directory = directory,
                nameCandidate = nameCandidate,
                write = { sink -> fileRemoteDataSource.download(id = id, sink = sink) },
            )
        } catch (exception: FileDownloadDirectoryUnavailableException) {
            throw DomainFileDownloadDirectoryUnavailableException(message = exception.message, cause = exception)
        }
}
