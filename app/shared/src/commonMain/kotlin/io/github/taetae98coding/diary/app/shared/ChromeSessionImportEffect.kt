package io.github.taetae98coding.diary.app.shared

import androidx.compose.runtime.Composable
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect

@Composable
internal fun ChromeSessionImportEffect(requestImport: () -> Unit) {
    LifecycleEventEffect(Lifecycle.Event.ON_START) {
        requestImport()
    }
}
