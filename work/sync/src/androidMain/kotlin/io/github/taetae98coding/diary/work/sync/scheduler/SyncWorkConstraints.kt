package io.github.taetae98coding.diary.work.sync.scheduler

import androidx.work.Constraints
import androidx.work.NetworkType

internal val SYNC_CONSTRAINTS: Constraints =
    Constraints
        .Builder()
        .setRequiredNetworkType(NetworkType.CONNECTED)
        .build()
