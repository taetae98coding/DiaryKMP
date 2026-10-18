package io.github.taetae98coding.diary.compose.core.swipe

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

internal object SwipeToFinishAndDeleteBoxDefaults {
    val StatusIconSize: Dp = 32.dp

    val CircleIconSize: Dp = 16.dp

    val StatusIconHorizontalPadding: Dp = 16.dp

    val DismissedResetDelay: Duration = 1.seconds
}
