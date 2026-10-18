package io.github.taetae98coding.diary.compose.core.input

import androidx.compose.material3.SnackbarHostState
import io.github.taetae98coding.diary.compose.core.snackbar.showImmediate
import io.github.taetae98coding.diary.library.compose.ui.color.randomColor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

// 입력값 초기화는 폼마다 필드가 달라 호출 전에 각 폼이 수행한다.
public fun CoroutineScope.showDiaryFormAdded(
    titleState: DiaryTitleInputState,
    colorState: DiaryColorInputState,
    snackbarHostState: SnackbarHostState,
    message: String,
) {
    titleState.requestFocus()
    launch { colorState.animateTo(color = randomColor()) }
    launch { snackbarHostState.showImmediate(message = message) }
}

public fun CoroutineScope.showDiaryFormTitleBlank(
    titleState: DiaryTitleInputState,
    snackbarHostState: SnackbarHostState,
    message: String,
) {
    titleState.requestFocus()
    launch { snackbarHostState.showImmediate(message = message) }
}
