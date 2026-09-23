package io.github.taetae98coding.diary.compose.core.swipe

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Surface
import androidx.compose.material3.SwipeToDismissBoxState
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import io.github.taetae98coding.diary.compose.core.icon.DeleteIcon
import io.github.taetae98coding.diary.compose.core.icon.FinishIcon
import io.github.taetae98coding.diary.compose.core.icon.RestartIcon
import io.github.taetae98coding.diary.compose.core.preview.ComponentPreview
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme

@Composable
internal fun SwipeToFinishAndDeleteBackground(
    finishContentDescription: String,
    deleteContentDescription: String,
    modifier: Modifier = Modifier,
    state: SwipeToDismissBoxState = rememberSwipeToDismissBoxState(),
    finishAction: SwipeFinishAction = SwipeFinishAction.FINISH,
) {
    Box(
        modifier =
            modifier
                .fillMaxSize()
                .padding(horizontal = SwipeToFinishAndDeleteBoxDefaults.StatusIconHorizontalPadding),
    ) {
        when (state.dismissDirection) {
            SwipeToDismissBoxValue.StartToEnd ->
                SwipeActionIcon(
                    isDismissTargeted = state.currentValue == SwipeToDismissBoxValue.StartToEnd,
                    modifier = Modifier.align(Alignment.CenterStart),
                ) {
                    when (finishAction) {
                        SwipeFinishAction.FINISH ->
                            FinishIcon(
                                modifier = Modifier.size(SwipeToFinishAndDeleteBoxDefaults.StatusIconSize),
                                contentDescription = finishContentDescription,
                            )

                        SwipeFinishAction.RESTART ->
                            RestartIcon(
                                modifier = Modifier.size(SwipeToFinishAndDeleteBoxDefaults.StatusIconSize),
                                contentDescription = finishContentDescription,
                            )
                    }
                }

            SwipeToDismissBoxValue.EndToStart ->
                SwipeActionIcon(
                    isDismissTargeted = state.currentValue == SwipeToDismissBoxValue.EndToStart,
                    modifier = Modifier.align(Alignment.CenterEnd),
                ) {
                    DeleteIcon(
                        modifier = Modifier.size(SwipeToFinishAndDeleteBoxDefaults.StatusIconSize),
                        contentDescription = deleteContentDescription,
                    )
                }

            SwipeToDismissBoxValue.Settled -> Unit
        }
    }
}

@ComponentPreview
@Composable
private fun SwipeToFinishAndDeleteBackgroundPreview() {
    DiaryTheme {
        Surface {
            SwipeToFinishAndDeleteBackground(
                finishContentDescription = "완료",
                deleteContentDescription = "삭제",
            )
        }
    }
}
