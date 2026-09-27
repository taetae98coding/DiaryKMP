package io.github.taetae98coding.diary.work.fileupload.scheduler

import android.content.Context
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import io.github.taetae98coding.diary.work.fileupload.report.AndroidFileUploadNotifier
import io.github.taetae98coding.diary.work.fileupload.text.FileUploadTextStore
import io.github.taetae98coding.diary.work.fileupload.work.FileUploadWork
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers

internal fun mockFileUploadWorkerFactory(fileUploadWork: FileUploadWork): WorkerFactory =
    mockk {
        every { createWorker(any(), any(), any()) } answers { worker(context = firstArg(), parameters = thirdArg(), fileUploadWork = fileUploadWork) }
        every { createWorkerWithDefaultFallback(any(), any(), any()) } answers { worker(context = firstArg(), parameters = thirdArg(), fileUploadWork = fileUploadWork) }
    }

private fun worker(
    context: Context,
    parameters: WorkerParameters,
    fileUploadWork: FileUploadWork,
): FileUploadWorker =
    FileUploadWorker(
        context = context,
        parameters = parameters,
        fileUploadWork = fileUploadWork,
        androidFileUploadNotifier = AndroidFileUploadNotifier(context = context),
        fileUploadTextStore = fileUploadTextStore(context = context),
    )

internal fun fileUploadTextStore(context: Context): FileUploadTextStore = FileUploadTextStore(context = context, dispatcher = Dispatchers.IO)
