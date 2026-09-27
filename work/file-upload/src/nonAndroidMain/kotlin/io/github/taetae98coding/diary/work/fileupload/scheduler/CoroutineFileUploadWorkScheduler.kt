package io.github.taetae98coding.diary.work.fileupload.scheduler

import io.github.taetae98coding.diary.core.model.file.FileUploadState
import io.github.taetae98coding.diary.core.model.file.FileUri
import io.github.taetae98coding.diary.domain.file.repository.FileRepository
import io.github.taetae98coding.diary.work.fileupload.di.FileUploadScope
import io.github.taetae98coding.diary.work.fileupload.report.FileUploadResultReporter
import io.github.taetae98coding.diary.work.fileupload.report.toFileUploadResult
import io.github.taetae98coding.diary.work.fileupload.work.FileUploadRequest
import io.github.taetae98coding.diary.work.fileupload.work.FileUploadWork
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.koin.core.annotation.Single

@Single
internal class CoroutineFileUploadWorkScheduler(
    private val fileUploadWork: FileUploadWork,
    private val fileRepository: FileRepository,
    private val fileUploadResultReporter: FileUploadResultReporter,
    @param:FileUploadScope private val scope: CoroutineScope,
) : FileUploadWorkScheduler {
    private val requestedState = MutableStateFlow<FileUploadState>(FileUploadState.Idle)

    private val continuedState: StateFlow<FileUploadState.Uploading?> =
        fileRepository
            .getContinuedUpload()
            .map { upload -> upload?.toFileUploadState() }
            .stateIn(scope = scope, started = SharingStarted.Eagerly, initialValue = null)

    override val state: Flow<FileUploadState> =
        combine(requestedState, continuedState) { requested, continued ->
            if (requested is FileUploadState.Uploading) requested else continued ?: FileUploadState.Idle
        }

    private var job: Job? = null
    private var request: FileUploadRequest? = null
    private var generation: Int = 0

    init {
        // 이 예약기는 파일을 고르기 전에 만들어지므로(iOS는 앱 시작 시점), 이때 남아 있는 사본은 앞선 실행이 둔 것이다.
        scope.launch { fileRepository.deleteLeftoverUploadSources() }
        scope.launch {
            fileRepository.getContinuedUploadResult().collect { result -> fileUploadResultReporter.report(result = result.toFileUploadResult()) }
        }
    }

    override suspend fun isUploading(): Boolean = job?.isActive == true || continuedState.value != null

    override suspend fun upload(request: FileUploadRequest) {
        if (isUploading()) return

        val currentGeneration = ++generation

        requestedState.value = FileUploadState.Uploading(percent = null)
        this.request = request
        job =
            scope.launch {
                try {
                    fileUploadWork.doWork(request = request) { step -> requestedState.value = step.toFileUploadState() }
                } finally {
                    if (currentGeneration == generation) {
                        requestedState.value = FileUploadState.Idle
                    }
                }
            }
    }

    override suspend fun cancel(): List<FileUri> {
        val activeRequest = request.takeIf { job?.isActive == true }

        job?.cancel()

        return listOfNotNull(activeRequest?.content?.uri)
    }
}
