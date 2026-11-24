package io.github.taetae98coding.diary.work.fileupload.report

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject

@Inject
@ContributesBinding(AppScope::class)
internal class NoOpFileUploadNotifier : FileUploadNotifier {
    override fun notifyResult(result: FileUploadResult): Unit = Unit
}
