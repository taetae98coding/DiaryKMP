package io.github.taetae98coding.diary.domain.file.usecase

import io.github.taetae98coding.diary.core.model.file.FileUploadStep
import io.github.taetae98coding.diary.core.model.file.FileUri
import io.github.taetae98coding.diary.domain.core.FlowUseCase
import io.github.taetae98coding.diary.domain.file.exception.FileTooLargeException
import io.github.taetae98coding.diary.domain.file.exception.FileUploadAccountChangedException
import io.github.taetae98coding.diary.domain.file.repository.FileRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.launch
import org.koin.core.annotation.Factory
import kotlin.uuid.Uuid

private const val MAX_FILE_SIZE_BYTES = 50L * 1024 * 1024

public data class UploadFileRequest(
    val uri: FileUri,
    val accountId: Uuid,
)

@Factory
public class UploadFileUseCase internal constructor(
    private val awaitFileUploadAccountChangeUseCase: AwaitFileUploadAccountChangeUseCase,
    private val fileRepository: FileRepository,
) : FlowUseCase<UploadFileRequest, FileUploadStep>() {
    override fun execute(parameter: UploadFileRequest): Flow<Result<FileUploadStep>> =
        channelFlow {
            val accountChange =
                launch {
                    awaitFileUploadAccountChangeUseCase(parameter = parameter.accountId).getOrThrow()

                    throw FileUploadAccountChangedException(message = "Account changed while uploading. accountId=${parameter.accountId}")
                }

            val source = fileRepository.findSource(uri = parameter.uri)

            if (source.size > MAX_FILE_SIZE_BYTES) {
                throw FileTooLargeException(message = "File is too large. size=${source.size}, maxSize=$MAX_FILE_SIZE_BYTES")
            }

            send(Result.success(FileUploadStep.Started(source = source)))

            // 보낸 양은 전송 수단이 코루틴 밖에서 알려 주므로 기다리지 않고 넣는다. 밀린 진행은 버려도 다음 진행이 대신한다.
            val file =
                fileRepository.create(source = source) { sentBytes ->
                    trySend(Result.success(FileUploadStep.Sent(source = source, sentBytes = sentBytes)))
                }

            send(Result.success(FileUploadStep.Completed(source = source, file = file)))
            accountChange.cancel()
        }
}
