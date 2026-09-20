package io.github.taetae98coding.diary.domain.sync

import kotlin.time.Duration
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours

internal val SYNC_PERIOD: Duration = 4.hours

internal val SYNC_RESET_THRESHOLD: Duration = 30.days
