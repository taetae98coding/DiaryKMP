package io.github.taetae98coding.diary.core.work.impl

import androidx.work.Constraints
import androidx.work.NetworkType

internal val SYNC_CONSTRAINTS: Constraints =
    Constraints
        .Builder()
        .setRequiredNetworkType(NetworkType.CONNECTED)
        .build()
