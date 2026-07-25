package io.github.taetae98coding.diary.compose.core.input

import androidx.compose.runtime.Composable
import io.github.taetae98coding.diary.compose.core.effect.RequestFocusEffect

@Composable
public fun DiaryTitleInputFocusEffect(state: DiaryTitleInputState = rememberDiaryTitleInputState()) {
    RequestFocusEffect(focusRequester = state.focusRequester)
}
