package io.github.taetae98coding.diary.app.shared.initializer

import io.github.taetae98coding.diary.work.sync.scheduler.registerPeriodicSyncBackgroundTask

internal actual fun initializeSyncWork() {
    registerPeriodicSyncBackgroundTask()
}
