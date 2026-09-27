package io.github.taetae98coding.diary.work.fileupload.manager

import io.github.taetae98coding.diary.core.model.file.FileScreen
import io.github.taetae98coding.diary.core.model.file.FileUploadContent
import io.github.taetae98coding.diary.core.model.file.FileUploadEvent
import io.github.taetae98coding.diary.core.model.file.FileUploadState
import io.github.taetae98coding.diary.domain.file.FileUploadManager
import io.github.taetae98coding.diary.domain.file.repository.FileRepository
import io.github.taetae98coding.diary.work.fileupload.scheduler.FileUploadWorkScheduler
import io.github.taetae98coding.diary.work.fileupload.state.FileScreenViewingHolder
import io.github.taetae98coding.diary.work.fileupload.state.FileUploadEventHolder
import io.github.taetae98coding.diary.work.fileupload.work.FileUploadRequest
import kotlinx.coroutines.flow.Flow
import org.koin.core.annotation.Single
import kotlin.uuid.Uuid

@Single
internal class FileUploadManagerImpl(
    private val fileUploadWorkScheduler: FileUploadWorkScheduler,
    private val fileRepository: FileRepository,
    private val fileUploadEventHolder: FileUploadEventHolder,
    private val fileScreenViewingHolder: FileScreenViewingHolder,
) : FileUploadManager {
    override val state: Flow<FileUploadState>
        get() = fileUploadWorkScheduler.state

    override fun getEvent(screen: FileScreen): Flow<FileUploadEvent> = fileUploadEventHolder.getEvent(screen = screen)

    override suspend fun requestUpload(
        content: FileUploadContent,
        accountId: Uuid,
    ) {
        if (fileUploadWorkScheduler.isUploading()) return

        // 선택 도구가 준 읽기 권한은 이 화면이 살아 있는 동안만 유효하므로, 올리기를 맡기기 전에 붙들어 둔다.
        fileRepository.addUploadSource(uri = content.uri)
        fileUploadWorkScheduler.upload(request = FileUploadRequest(content = content, accountId = accountId))
    }

    // 취소로 끝난 작업은 시스템이 다시 실행할 수도 있어 붙들어 둔 파일을 스스로 놓지 않으므로, 취소를 요청한 여기서 놓는다.
    override suspend fun cancelUpload() {
        fileUploadWorkScheduler.cancel().forEach { uri -> fileRepository.removeUploadSource(uri = uri) }
        fileRepository.deleteContinuedUpload()
    }

    override fun startViewing(screen: FileScreen) {
        fileScreenViewingHolder.start(screen = screen)
    }

    override fun stopViewing(screen: FileScreen) {
        fileScreenViewingHolder.stop(screen = screen)
    }
}
