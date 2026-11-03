package io.github.taetae98coding.diary.compose.core.button

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector

public interface DiaryFloatingActionButtonOverride {
    @Composable
    public fun DiaryFloatingActionButtonOverrideScope.FloatingActionButton()
}

public class DiaryFloatingActionButtonOverrideScope internal constructor(
    public val onClick: () -> Unit,
    public val imageVector: ImageVector,
    public val modifier: Modifier,
    public val contentDescription: String?,
    public val isInProgressProvider: () -> Boolean,
)

public object MaterialDiaryFloatingActionButtonOverride : DiaryFloatingActionButtonOverride {
    @Composable
    override fun DiaryFloatingActionButtonOverrideScope.FloatingActionButton() {
        MaterialDiaryFloatingActionButton(
            onClick = onClick,
            imageVector = imageVector,
            modifier = modifier,
            contentDescription = contentDescription,
            isInProgressProvider = isInProgressProvider,
        )
    }
}

public val LocalDiaryFloatingActionButtonOverride: ProvidableCompositionLocal<DiaryFloatingActionButtonOverride> =
    staticCompositionLocalOf { MaterialDiaryFloatingActionButtonOverride }
