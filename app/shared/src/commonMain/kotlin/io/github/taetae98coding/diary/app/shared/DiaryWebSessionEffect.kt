package io.github.taetae98coding.diary.app.shared

import androidx.compose.runtime.Composable
import io.github.taetae98coding.diary.compose.core.effect.CollectEffect
import io.github.taetae98coding.diary.compose.web.DiaryWebSession
import io.github.taetae98coding.diary.compose.web.SingletonDiaryWebSession
import kotlinx.coroutines.flow.Flow

@Composable
internal fun DiaryWebSessionEffect(session: Flow<DiaryWebSession>) {
    CollectEffect(effect = session) { value -> SingletonDiaryWebSession.set(value) }
}
