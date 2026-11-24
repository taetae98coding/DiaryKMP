package io.github.taetae98coding.diary.work.fileupload.scheduler

import android.content.Context
import androidx.work.ListenableWorker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesIntoSet
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.binding

@Inject
@ContributesIntoSet(AppScope::class, binding = binding<WorkerFactory>())
internal class FileUploadWorkerFactory(
    private val fileUploadWorkerFactory: FileUploadWorker.Factory,
) : WorkerFactory() {
    override fun createWorker(
        appContext: Context,
        workerClassName: String,
        workerParameters: WorkerParameters,
    ): ListenableWorker? =
        if (workerClassName == FileUploadWorker::class.java.name) {
            fileUploadWorkerFactory.create(context = appContext, parameters = workerParameters)
        } else {
            null
        }
}
