package io.github.taetae98coding.diary.work.fileupload.report

import io.github.taetae98coding.diary.core.model.file.FileScreen
import io.github.taetae98coding.diary.core.model.file.FileUploadEvent
import io.github.taetae98coding.diary.work.fileupload.state.FileScreenViewingHolder
import io.github.taetae98coding.diary.work.fileupload.state.FileUploadEventHolder
import org.koin.core.annotation.Factory

@Factory
internal class FileUploadResultReporter(
    private val fileScreenViewingHolder: FileScreenViewingHolder,
    private val fileUploadEventHolder: FileUploadEventHolder,
    private val fileUploadNotifier: FileUploadNotifier,
) {
    fun report(result: FileUploadResult) {
        when (fileScreenViewingHolder.viewingScreen) {
            FileScreen.HOME -> {
                fileUploadEventHolder.send(screen = FileScreen.HOME, event = result.toEvent())
            }

            FileScreen.ADD -> {
                fileUploadEventHolder.send(screen = FileScreen.ADD, event = result.toEvent())
                if (result is FileUploadResult.Succeeded) {
                    fileUploadEventHolder.send(screen = FileScreen.HOME, event = FileUploadEvent.SucceededOnFileAdd(fileId = result.fileId))
                }
            }

            null -> {
                fileUploadNotifier.notifyResult(result = result)
            }
        }
    }
}
