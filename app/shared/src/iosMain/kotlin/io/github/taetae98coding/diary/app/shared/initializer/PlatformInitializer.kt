package io.github.taetae98coding.diary.app.shared.initializer

import io.github.taetae98coding.diary.work.fileupload.scheduler.initializeFileUploadWork
import io.github.taetae98coding.diary.work.spotlight.initializeSpotlightWork
import io.github.taetae98coding.diary.work.sync.scheduler.registerPeriodicSyncBackgroundTask

internal actual fun initializePlatform() {
    registerPeriodicSyncBackgroundTask()
    initializeFileUploadWork()
    initializeSpotlightWork()
}
