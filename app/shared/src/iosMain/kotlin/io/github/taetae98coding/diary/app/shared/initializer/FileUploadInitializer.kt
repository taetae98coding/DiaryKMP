package io.github.taetae98coding.diary.app.shared.initializer

import io.github.taetae98coding.diary.work.fileupload.scheduler.initializeFileUploadWork

internal actual fun initializeFileUpload() {
    initializeFileUploadWork()
}
