package io.github.taetae98coding.diary.compose.core.swipe

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import io.github.taetae98coding.diary.compose.core.animation.DiaryScaleCrossfade
import io.github.taetae98coding.diary.compose.core.icon.CircleIcon
import io.github.taetae98coding.diary.compose.core.icon.DeleteIcon
import io.github.taetae98coding.diary.compose.core.preview.ComponentPreview
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme

internal val SWIPE_ACTION_ICON_SIZE = 32.dp

private val SwipeCircleIconSize = 16.dp

internal const val SWIPE_TO_FINISH_AND_DELETE_CIRCLE_ICON_TEST_TAG: String = "SwipeToFinishAndDeleteCircleIcon"

@Composable
internal fun SwipeActionIcon(
    modifier: Modifier = Modifier,
    isDismissTargeted: Boolean = false,
    icon: @Composable () -> Unit,
) {
    DiaryScaleCrossfade(
        targetState = isDismissTargeted,
        modifier = modifier,
    ) { isTargeted ->
        Box(
            modifier = Modifier.size(SWIPE_ACTION_ICON_SIZE),
            contentAlignment = Alignment.Center,
        ) {
            if (isTargeted) {
                icon()
            } else {
                CircleIcon(
                    modifier =
                        Modifier
                            .testTag(SWIPE_TO_FINISH_AND_DELETE_CIRCLE_ICON_TEST_TAG)
                            .size(SwipeCircleIconSize),
                )
            }
        }
    }
}

@ComponentPreview
@Composable
private fun SwipeActionIconPreview() {
    DiaryTheme {
        Surface {
            SwipeActionIcon(isDismissTargeted = true) {
                DeleteIcon(modifier = Modifier.size(SWIPE_ACTION_ICON_SIZE))
            }
        }
    }
}
