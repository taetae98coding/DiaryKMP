package io.github.taetae98coding.diary.app.shared.web

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.taetae98coding.diary.compose.web.DiaryWebSession
import io.github.taetae98coding.diary.compose.web.LocalDiaryWebSession
import kotlinx.coroutines.flow.StateFlow

@Composable
internal fun ProvideDiaryWebSession(
    session: StateFlow<DiaryWebSession>,
    content: @Composable () -> Unit,
) {
    val value by session.collectAsStateWithLifecycle()

    CompositionLocalProvider(
        LocalDiaryWebSession provides value,
        content = content,
    )
}
