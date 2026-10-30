package io.github.taetae98coding.diary.app.shared.web

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState

@Composable
internal fun RestoreWebLoginEffect(restore: () -> Unit) {
    val currentRestore by rememberUpdatedState(restore)

    LaunchedEffect(Unit) { currentRestore() }
}
