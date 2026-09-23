@file:OptIn(ExperimentalNativeApi::class)

package io.github.taetae98coding.diary

import io.github.taetae98coding.diary.app.shared.initializer.StartupInitializer
import kotlin.experimental.ExperimentalNativeApi
import kotlin.native.Platform

public data object DiaryStartupInitializer {
    public fun start() {
        StartupInitializer.initialize(isDebug = Platform.isDebugBinary)
    }
}
