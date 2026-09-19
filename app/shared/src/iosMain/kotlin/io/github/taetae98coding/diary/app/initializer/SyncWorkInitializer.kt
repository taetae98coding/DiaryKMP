package io.github.taetae98coding.diary.app.initializer

import io.github.taetae98coding.diary.core.work.impl.registerPeriodicSyncBackgroundTask

internal actual fun initializeSyncWork() {
    registerPeriodicSyncBackgroundTask()
}
