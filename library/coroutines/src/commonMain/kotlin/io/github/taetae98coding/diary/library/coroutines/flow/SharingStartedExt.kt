package io.github.taetae98coding.diary.library.coroutines.flow

import kotlinx.coroutines.flow.SharingStarted

public const val UI_STOP_TIMEOUT_MILLIS: Long = 5_000L

private val whileUiSubscribed: SharingStarted = SharingStarted.WhileSubscribed(stopTimeoutMillis = UI_STOP_TIMEOUT_MILLIS)

public val SharingStarted.Companion.WhileUiSubscribed: SharingStarted
    get() = whileUiSubscribed
