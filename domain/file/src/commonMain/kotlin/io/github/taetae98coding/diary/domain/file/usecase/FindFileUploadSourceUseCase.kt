package io.github.taetae98coding.diary.domain.file.usecase

import io.github.taetae98coding.diary.core.model.file.FileUploadSource
import io.github.taetae98coding.diary.core.model.file.FileUri
import io.github.taetae98coding.diary.domain.core.UseCase
import io.github.taetae98coding.diary.domain.file.MAX_FILE_SIZE_BYTES
import io.github.taetae98coding.diary.domain.file.exception.FileTooLargeException
import io.github.taetae98coding.diary.domain.file.repository.FileRepository
import org.koin.core.annotation.Factory

@Factory
public class FindFileUploadSourceUseCase internal constructor(
    private val fileRepository: FileRepository,
) : UseCase<FileUri, FileUploadSource>() {
    override suspend fun execute(parameter: FileUri): FileUploadSource {
        val source = fileRepository.findSource(uri = parameter)

        if (source.size > MAX_FILE_SIZE_BYTES) {
            throw FileTooLargeException(message = "File is too large. size=${source.size}, maxSize=$MAX_FILE_SIZE_BYTES")
        }

        return source
    }
}
