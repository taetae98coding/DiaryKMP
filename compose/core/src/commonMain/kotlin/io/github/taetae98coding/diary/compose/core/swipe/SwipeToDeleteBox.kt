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
public fun SwipeToDeleteBox(
    deleteContentDescription: String,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
    key: Any? = null,
    gesturesEnabled: Boolean = true,
    content: @Composable () -> Unit,
) {
    val positionalThreshold = SwipeToDismissBoxDefaults.positionalThreshold
    // rememberSwipeToDismissBoxState는 dismissed 값을 복원해 onDismiss가 다시 불리므로 저장하지 않는 상태를 쓴다.
    val state =
        remember(key) {
            SwipeToDismissBoxState(
                initialValue = SwipeToDismissBoxValue.Settled,
                positionalThreshold = positionalThreshold,
            )
        }

    ResetDismissedSwipeEffect(state = state)

    SwipeToDismissBox(
        state = state,
        backgroundContent = {
            SwipeToFinishAndDeleteBackground(
                state = state,
                finishContentDescription = "",
                deleteContentDescription = deleteContentDescription,
            )
        },
        modifier = modifier,
        enableDismissFromStartToEnd = false,
        gesturesEnabled = gesturesEnabled,
        onDismiss = { value ->
            if (value == SwipeToDismissBoxValue.EndToStart) onDelete()
        },
    ) {
        content()
    }
}

@ComponentPreview
@Composable
private fun SwipeToDeleteBoxPreview() {
    DiaryTheme {
        SwipeToDeleteBox(
            onDelete = {},
            deleteContentDescription = "",
        ) {
            Card {
                Text(
                    text = "SwipeToDeleteBox",
                    modifier = Modifier.padding(16.dp),
                )
            }
        }
    }
}
