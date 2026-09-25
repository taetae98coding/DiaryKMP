package io.github.taetae98coding.diary.domain.file.usecase

import io.github.taetae98coding.diary.core.model.file.DiaryFile
import io.github.taetae98coding.diary.core.model.file.FileUri
import io.github.taetae98coding.diary.domain.account.usecase.GetAccountUseCase
import io.github.taetae98coding.diary.domain.core.UseCase
import io.github.taetae98coding.diary.domain.file.exception.FileUploadAccountChangedException
import io.github.taetae98coding.diary.domain.file.repository.FileRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.selects.select
import org.koin.core.annotation.Factory

private const val MAX_FILE_SIZE_BYTES = 50L * 1024 * 1024

@Factory
public class UploadFileUseCase internal constructor(
    private val getAccountUseCase: GetAccountUseCase,
    private val fileRepository: FileRepository,
) : UseCase<FileUri, DiaryFile>() {
    override suspend fun execute(parameter: FileUri): DiaryFile =
        coroutineScope {
            val accountId = getAccountUseCase(parameter = Unit).first().getOrThrow().id
            val upload = async { fileRepository.create(uri = parameter, maxSize = MAX_FILE_SIZE_BYTES) }
            val accountChange =
                async {
                    getAccountUseCase(parameter = Unit).first { result -> result.isSuccess && result.getOrNull()?.id != accountId }
                }

            select {
                upload.onAwait { file ->
                    accountChange.cancel()
                    file
                }
                accountChange.onAwait {
                    upload.cancel()
                    throw FileUploadAccountChangedException(message = "Account changed while uploading. accountId=$accountId")
                }
            }
        }
}
