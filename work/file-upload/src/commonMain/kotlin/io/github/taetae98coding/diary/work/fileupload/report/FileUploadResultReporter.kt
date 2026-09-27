package io.github.taetae98coding.diary.work.fileupload.report

import io.github.taetae98coding.diary.work.fileupload.state.FileHomeViewingHolder
import io.github.taetae98coding.diary.work.fileupload.state.FileUploadEventHolder
import org.koin.core.annotation.Factory

@Factory
internal class FileUploadResultReporter(
    private val fileHomeViewingHolder: FileHomeViewingHolder,
    private val fileUploadEventHolder: FileUploadEventHolder,
    private val fileUploadNotifier: FileUploadNotifier,
) {
    fun report(result: FileUploadResult) {
        if (fileHomeViewingHolder.isViewing) {
            fileUploadEventHolder.send(event = result.toEvent())
        } else {
            fileUploadNotifier.notifyResult(result = result)
        }
    }
}
