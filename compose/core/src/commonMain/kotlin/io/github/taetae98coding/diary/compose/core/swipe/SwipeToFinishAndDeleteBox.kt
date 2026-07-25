package io.github.taetae98coding.diary.compose.core.swipe

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxDefaults
import androidx.compose.material3.SwipeToDismissBoxState
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.taetae98coding.diary.compose.core.preview.ComponentPreview
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme

@Composable
public fun SwipeToFinishAndDeleteBox(
    finishContentDescription: String,
    deleteContentDescription: String,
    onFinish: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
    key: Any? = null,
    gesturesEnabled: Boolean = true,
    finishAction: SwipeFinishAction = SwipeFinishAction.FINISH,
    content: @Composable () -> Unit,
) {
    val positionalThreshold = SwipeToDismissBoxDefaults.positionalThreshold
    // rememberSwipeToDismissBoxState는 rememberSaveable로 currentValue를 복원해
    // 실행 취소로 돌아온 카드가 dismissed 상태로 복원되고 onDismiss가 재발화된다.
    val state =
        remember(key) {
            SwipeToDismissBoxState(
                initialValue = SwipeToDismissBoxValue.Settled,
                positionalThreshold = positionalThreshold,
            )
        }

    SwipeToDismissBox(
        state = state,
        backgroundContent = {
            SwipeToFinishAndDeleteBackground(
                state = state,
                finishAction = finishAction,
                finishContentDescription = finishContentDescription,
                deleteContentDescription = deleteContentDescription,
            )
        },
        modifier = modifier,
        gesturesEnabled = gesturesEnabled,
        onDismiss = { value ->
            when (value) {
                SwipeToDismissBoxValue.StartToEnd -> onFinish()
                SwipeToDismissBoxValue.EndToStart -> onDelete()
                SwipeToDismissBoxValue.Settled -> Unit
            }
        },
    ) {
        content()
    }
}

@ComponentPreview
@Composable
private fun SwipeToFinishAndDeleteBoxPreview() {
    DiaryTheme {
        SwipeToFinishAndDeleteBox(
            onFinish = {},
            onDelete = {},
            finishContentDescription = "",
            deleteContentDescription = "",
        ) {
            Card {
                Text(
                    text = "SwipeToFinishAndDeleteBox",
                    modifier = Modifier.padding(16.dp),
                )
            }
        }
    }
}
