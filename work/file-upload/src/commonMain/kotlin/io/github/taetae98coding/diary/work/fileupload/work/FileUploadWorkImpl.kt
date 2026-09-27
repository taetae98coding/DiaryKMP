package io.github.taetae98coding.diary.work.fileupload.work

import io.github.taetae98coding.diary.core.model.file.FileUploadStep
import io.github.taetae98coding.diary.domain.file.exception.FileTooLargeException
import io.github.taetae98coding.diary.domain.file.exception.FileUnreadableException
import io.github.taetae98coding.diary.domain.file.exception.FileUploadAccountChangedException
import io.github.taetae98coding.diary.domain.file.repository.FileRepository
import io.github.taetae98coding.diary.domain.file.usecase.UploadFileRequest
import io.github.taetae98coding.diary.domain.file.usecase.UploadFileUseCase
import io.github.taetae98coding.diary.work.fileupload.report.FileUploadResult
import io.github.taetae98coding.diary.work.fileupload.report.FileUploadResultReporter
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.withContext
import org.koin.core.annotation.Factory

@Factory
internal class FileUploadWorkImpl(
    private val uploadFileUseCase: UploadFileUseCase,
    private val fileRepository: FileRepository,
    private val fileUploadResultReporter: FileUploadResultReporter,
) : FileUploadWork {
    override suspend fun doWork(
        request: FileUploadRequest,
        onStep: suspend (FileUploadStep) -> Unit,
    ) {
        try {
            upload(request = request, onStep = onStep)
        } catch (exception: CancellationException) {
            // 시스템이 작업을 멈추면 나중에 같은 파일을 처음부터 다시 실행하므로, 그때 다시 읽을 수 있게 남겨 둔다.
            throw exception
        } catch (throwable: Throwable) {
            removeUploadSource(request = request)
            throw throwable
        }

        removeUploadSource(request = request)
    }

    private suspend fun upload(
        request: FileUploadRequest,
        onStep: suspend (FileUploadStep) -> Unit,
    ) {
        var name = ""

        uploadFileUseCase(parameter = UploadFileRequest(uri = request.uri, accountId = request.accountId)).collect { result ->
            val step =
                result.getOrElse { throwable ->
                    throwable.toFileUploadResult(name = name)?.let(fileUploadResultReporter::report)
                    throw throwable
                }

            name = step.source.name
            onStep(step)

            if (step is FileUploadStep.Completed) {
                fileUploadResultReporter.report(result = FileUploadResult.Succeeded(name = step.source.name, fileId = step.file.id))
            }
        }
    }

    private suspend fun removeUploadSource(request: FileUploadRequest) {
        withContext(NonCancellable) { fileRepository.removeUploadSource(uri = request.uri) }
    }

    private fun Throwable.toFileUploadResult(name: String): FileUploadResult? =
        when (this) {
            is FileUploadAccountChangedException -> null
            is FileTooLargeException -> FileUploadResult.TooLarge
            is FileUnreadableException -> FileUploadResult.Failed(name = this.name.ifEmpty { name })
            else -> FileUploadResult.Failed(name = name)
        }
}
