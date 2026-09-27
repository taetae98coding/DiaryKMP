package io.github.taetae98coding.diary.work.fileupload.scheduler

import android.content.Context
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import io.github.taetae98coding.diary.core.model.file.FileUploadState
import io.github.taetae98coding.diary.core.model.file.FileUri
import io.github.taetae98coding.diary.work.fileupload.text.FileUploadText
import io.github.taetae98coding.diary.work.fileupload.text.FileUploadTextStore
import io.github.taetae98coding.diary.work.fileupload.work.FileUploadRequest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import org.koin.core.annotation.Factory

@Factory
internal class AndroidFileUploadWorkScheduler(
    private val context: Context,
    private val fileUploadTextStore: FileUploadTextStore,
) : FileUploadWorkScheduler {
    override val state: Flow<FileUploadState>
        get() =
            WorkManager
                .getInstance(context)
                .getWorkInfosForUniqueWorkFlow(FILE_UPLOAD_WORK_NAME)
                .map { workInfoList -> workInfoList.toFileUploadState() }

    override suspend fun isUploading(): Boolean = state.first() is FileUploadState.Uploading

    override suspend fun upload(request: FileUploadRequest) {
        val textId = fileUploadTextStore.write(text = FileUploadText(title = request.content.title, description = request.content.description))

        WorkManager
            .getInstance(context)
            .enqueueUniqueWork(
                FILE_UPLOAD_WORK_NAME,
                ExistingWorkPolicy.KEEP,
                OneTimeWorkRequestBuilder<FileUploadWorker>()
                    .setInputData(request.toData(textId = textId))
                    .addTag(request.content.uri.toFileUploadTag())
                    .addTag(textId.toFileUploadTextTag())
                    .build(),
            )
    }

    // WorkInfo는 입력 값을 보여 주지 않으므로, 취소할 때 놓을 파일과 지울 제목·설명을 태그로 찾는다.
    override suspend fun cancel(): List<FileUri> {
        val workManager = WorkManager.getInstance(context)
        val tagList =
            workManager
                .getWorkInfosForUniqueWorkFlow(FILE_UPLOAD_WORK_NAME)
                .first()
                .filterNot { workInfo -> workInfo.state.isFinished }
                .flatMap { workInfo -> workInfo.tags }

        workManager.cancelUniqueWork(FILE_UPLOAD_WORK_NAME)
        tagList
            .mapNotNull { tag -> tag.toFileUploadTextIdOrNull() }
            .forEach { textId -> fileUploadTextStore.delete(id = textId) }

        return tagList.mapNotNull { tag -> tag.toFileUriOrNull() }
    }

    private fun List<WorkInfo>.toFileUploadState(): FileUploadState {
        val workInfo = firstOrNull { workInfo -> !workInfo.state.isFinished } ?: return FileUploadState.Idle

        return FileUploadState.Uploading(percent = workInfo.progress.toPercent())
    }

    companion object {
        const val FILE_UPLOAD_WORK_NAME: String = "fileUpload"
    }
}

private const val URI_TAG_PREFIX: String = "fileUploadUri:"

internal fun FileUri.toFileUploadTag(): String = "$URI_TAG_PREFIX$value"

private fun String.toFileUriOrNull(): FileUri? = if (startsWith(URI_TAG_PREFIX)) FileUri(removePrefix(URI_TAG_PREFIX)) else null

private const val TEXT_TAG_PREFIX: String = "fileUploadText:"

internal fun String.toFileUploadTextTag(): String = "$TEXT_TAG_PREFIX$this"

private fun String.toFileUploadTextIdOrNull(): String? = if (startsWith(TEXT_TAG_PREFIX)) removePrefix(TEXT_TAG_PREFIX) else null
