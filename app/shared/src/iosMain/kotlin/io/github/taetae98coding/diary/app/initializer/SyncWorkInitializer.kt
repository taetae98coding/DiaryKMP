package io.github.taetae98coding.diary.app.initializer

import io.github.taetae98coding.diary.work.sync.scheduler.registerPeriodicSyncBackgroundTask

internal actual fun initializeSyncWork() {
    registerPeriodicSyncBackgroundTask()
}
