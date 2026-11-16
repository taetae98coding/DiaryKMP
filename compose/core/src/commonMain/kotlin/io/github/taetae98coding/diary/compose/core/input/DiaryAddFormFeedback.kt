package io.github.taetae98coding.diary.compose.core.input

import androidx.compose.material3.SnackbarHostState
import io.github.taetae98coding.diary.compose.core.snackbar.showImmediate
import io.github.taetae98coding.diary.library.compose.ui.color.randomColor
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

// 입력값 초기화는 폼마다 필드가 달라 호출 전에 각 폼이 수행한다.
public suspend fun showDiaryFormAdded(
    colorState: DiaryColorInputState,
    snackbarHostState: SnackbarHostState,
    message: String,
) {
    coroutineScope {
        launch { colorState.animateTo(color = randomColor()) }
        snackbarHostState.showImmediate(message = message)
    }
}
