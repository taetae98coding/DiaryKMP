package io.github.taetae98coding.diary.work.fileupload.report

import dev.zacsweers.metro.Inject
import io.github.taetae98coding.diary.work.fileupload.state.FileUploadEventHolder

@Inject
internal class FileUploadResultReporter(
    private val fileUploadEventHolder: FileUploadEventHolder,
    private val fileUploadNotifier: FileUploadNotifier,
) {
    fun report(result: FileUploadResult) {
        fileUploadNotifier.notifyResult(result = result)
        fileUploadEventHolder.send(event = result.toEvent())
    }
}
