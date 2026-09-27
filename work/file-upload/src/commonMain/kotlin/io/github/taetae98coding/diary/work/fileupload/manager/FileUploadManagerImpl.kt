package io.github.taetae98coding.diary.work.fileupload.manager

import io.github.taetae98coding.diary.core.model.file.FileUploadEvent
import io.github.taetae98coding.diary.core.model.file.FileUploadState
import io.github.taetae98coding.diary.core.model.file.FileUri
import io.github.taetae98coding.diary.domain.file.FileUploadManager
import io.github.taetae98coding.diary.domain.file.repository.FileRepository
import io.github.taetae98coding.diary.work.fileupload.scheduler.FileUploadWorkScheduler
import io.github.taetae98coding.diary.work.fileupload.state.FileHomeViewingHolder
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
    private val fileHomeViewingHolder: FileHomeViewingHolder,
) : FileUploadManager {
    override val state: Flow<FileUploadState>
        get() = fileUploadWorkScheduler.state

    override val event: Flow<FileUploadEvent>
        get() = fileUploadEventHolder.event

    override suspend fun requestUpload(
        uri: FileUri,
        accountId: Uuid,
    ) {
        if (fileUploadWorkScheduler.isUploading()) return

        // 선택 도구가 준 읽기 권한은 이 화면이 살아 있는 동안만 유효하므로, 올리기를 맡기기 전에 붙들어 둔다.
        fileRepository.addUploadSource(uri = uri)
        fileUploadWorkScheduler.upload(request = FileUploadRequest(uri = uri, accountId = accountId))
    }

    // 취소로 끝난 작업은 시스템이 다시 실행할 수도 있어 붙들어 둔 파일을 스스로 놓지 않으므로, 취소를 요청한 여기서 놓는다.
    override suspend fun cancelUpload() {
        fileUploadWorkScheduler.cancel().forEach { uri -> fileRepository.removeUploadSource(uri = uri) }
        fileRepository.deleteContinuedUpload()
    }

    override fun setFileHomeViewing(isViewing: Boolean) {
        fileHomeViewingHolder.isViewing = isViewing
    }
}
