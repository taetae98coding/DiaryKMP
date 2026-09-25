package io.github.taetae98coding.diary.app.shared.integrity

import androidx.compose.runtime.Composable
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect

@Composable
internal fun PlayIntegrityLogEffect(log: () -> Unit) {
    LifecycleEventEffect(Lifecycle.Event.ON_START) {
        log()
    }
}
