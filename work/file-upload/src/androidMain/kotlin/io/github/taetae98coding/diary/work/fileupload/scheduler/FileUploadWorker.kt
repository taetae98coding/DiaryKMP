package io.github.taetae98coding.diary.work.fileupload.scheduler

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import io.github.taetae98coding.diary.core.model.file.FileUri
import io.github.taetae98coding.diary.work.fileupload.report.AndroidFileUploadNotifier
import io.github.taetae98coding.diary.work.fileupload.work.FileUploadRequest
import io.github.taetae98coding.diary.work.fileupload.work.FileUploadWork
import kotlinx.coroutines.CancellationException
import org.koin.android.annotation.KoinWorker
import kotlin.uuid.Uuid

internal const val FILE_UPLOAD_URI_KEY: String = "uri"
internal const val FILE_UPLOAD_ACCOUNT_ID_KEY: String = "accountId"
internal const val FILE_UPLOAD_PERCENT_KEY: String = "percent"

// WorkManager의 Data는 null을 담을 수 없으므로 백분율이 없는 진행을 이 값으로 둔다.
internal const val NO_PERCENT: Int = -1

@KoinWorker
internal class FileUploadWorker(
    context: Context,
    parameters: WorkerParameters,
    private val fileUploadWork: FileUploadWork,
    private val androidFileUploadNotifier: AndroidFileUploadNotifier,
) : CoroutineWorker(context, parameters) {
    override suspend fun doWork(): Result =
        try {
            var lastPercent: Int? = NO_PERCENT

            fileUploadWork.doWork(request = inputData.toFileUploadRequest()) { step ->
                val percent = step.toFileUploadState().percent

                // 진행 알림이 자주 다시 그려지면 알림 목록이 깜빡이므로 백분율이 바뀔 때만 갱신한다.
                if (percent != lastPercent) {
                    lastPercent = percent
                    setProgress(workDataOf(FILE_UPLOAD_PERCENT_KEY to (percent ?: NO_PERCENT)))
                    updateForeground(name = step.source.name, percent = percent)
                }
            }
            Result.success()
        } catch (exception: CancellationException) {
            throw exception
        } catch (_: Throwable) {
            Result.failure()
        }

    // 시스템이 앱을 정리한 뒤 화면 뒤에서 작업을 다시 시작하면 시스템이 포그라운드 서비스 시작을 막는다.
    private suspend fun updateForeground(
        name: String,
        percent: Int?,
    ) {
        try {
            setForeground(androidFileUploadNotifier.createForegroundInfo(name = name, percent = percent))
        } catch (_: IllegalStateException) {
            Unit
        }
    }
}

internal fun FileUploadRequest.toData(): Data =
    workDataOf(
        FILE_UPLOAD_URI_KEY to uri.value,
        FILE_UPLOAD_ACCOUNT_ID_KEY to accountId.toString(),
    )

private fun Data.toFileUploadRequest(): FileUploadRequest =
    FileUploadRequest(
        uri = FileUri(checkNotNull(getString(FILE_UPLOAD_URI_KEY)) { "File upload uri is missing." }),
        accountId = Uuid.parse(checkNotNull(getString(FILE_UPLOAD_ACCOUNT_ID_KEY)) { "File upload account id is missing." }),
    )

internal fun Data.toPercent(): Int? = getInt(FILE_UPLOAD_PERCENT_KEY, NO_PERCENT).takeIf { percent -> percent != NO_PERCENT }
