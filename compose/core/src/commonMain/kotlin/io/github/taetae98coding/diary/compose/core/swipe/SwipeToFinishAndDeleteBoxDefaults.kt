package io.github.taetae98coding.diary.compose.core.swipe

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

public object SwipeToFinishAndDeleteBoxDefaults {
    public val StatusIconSize: Dp = 32.dp

    public val CircleIconSize: Dp = 16.dp

    public val StatusIconHorizontalPadding: Dp = 16.dp

    public val DismissedResetDelay: Duration = 1.seconds
}
