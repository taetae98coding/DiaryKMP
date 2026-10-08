package io.github.taetae98coding.diary.work.fileupload.scheduler

import android.content.Context
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import io.github.taetae98coding.diary.work.fileupload.report.AndroidFileUploadNotifier
import io.github.taetae98coding.diary.work.fileupload.text.FileUploadTextStore
import io.github.taetae98coding.diary.work.fileupload.work.FileUploadWork
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.CoroutineDispatcher

internal fun mockFileUploadWorkerFactory(
    fileUploadWork: FileUploadWork,
    dispatcher: CoroutineDispatcher,
): WorkerFactory =
    mockk {
        every { createWorker(any(), any(), any()) } answers { worker(context = firstArg(), parameters = thirdArg(), fileUploadWork = fileUploadWork, dispatcher = dispatcher) }
        every { createWorkerWithDefaultFallback(any(), any(), any()) } answers { worker(context = firstArg(), parameters = thirdArg(), fileUploadWork = fileUploadWork, dispatcher = dispatcher) }
    }

private fun worker(
    context: Context,
    parameters: WorkerParameters,
    fileUploadWork: FileUploadWork,
    dispatcher: CoroutineDispatcher,
): FileUploadWorker =
    FileUploadWorker(
        context = context,
        parameters = parameters,
        fileUploadWork = fileUploadWork,
        androidFileUploadNotifier = AndroidFileUploadNotifier(context = context),
        fileUploadTextStore = fileUploadTextStore(context = context, dispatcher = dispatcher),
    )

internal fun fileUploadTextStore(
    context: Context,
    dispatcher: CoroutineDispatcher,
): FileUploadTextStore = FileUploadTextStore(context = context, dispatcher = dispatcher)
