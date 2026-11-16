package io.github.taetae98coding.diary.compose.core.swipe

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.taetae98coding.diary.compose.theme.DiaryTheme
import io.github.taetae98coding.diary.compose.theme.preview.ComponentPreview

@Composable
public fun SwipeToDeleteBox(
    deleteContentDescription: String,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
    key: Any? = null,
    gesturesEnabled: Boolean = true,
    content: @Composable () -> Unit,
) {
    SwipeToDismissActionBox(
        finishContentDescription = "",
        deleteContentDescription = deleteContentDescription,
        onFinish = {},
        onDelete = onDelete,
        modifier = modifier,
        key = key,
        gesturesEnabled = gesturesEnabled,
        enableDismissFromStartToEnd = false,
        content = content,
    )
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
