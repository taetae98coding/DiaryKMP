package io.github.taetae98coding.diary.work.fileupload.report

import org.koin.core.annotation.Factory

@Factory
internal class NoOpFileUploadNotifier : FileUploadNotifier {
    override fun notifyResult(result: FileUploadResult): Unit = Unit
}
