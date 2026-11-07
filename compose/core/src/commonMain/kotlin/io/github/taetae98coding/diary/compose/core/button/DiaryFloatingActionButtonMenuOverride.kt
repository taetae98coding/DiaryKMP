package io.github.taetae98coding.diary.compose.core.button

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier

public interface DiaryFloatingActionButtonMenuOverride {
    @Composable
    public fun DiaryFloatingActionButtonMenuOverrideScope.FloatingActionButtonMenu()
}

public class DiaryFloatingActionButtonMenuOverrideScope internal constructor(
    public val expanded: Boolean,
    public val onExpandedChange: (Boolean) -> Unit,
    public val icon: DiaryFloatingActionButtonMenuIcon,
    public val contentDescription: String,
    public val expandedContentDescription: String,
    public val items: List<DiaryFloatingActionButtonMenuItem>,
    public val modifier: Modifier,
)

public object MaterialDiaryFloatingActionButtonMenuOverride : DiaryFloatingActionButtonMenuOverride {
    @Composable
    override fun DiaryFloatingActionButtonMenuOverrideScope.FloatingActionButtonMenu() {
        MaterialDiaryFloatingActionButtonMenu(
            expanded = expanded,
            onExpandedChange = onExpandedChange,
            icon = icon,
            contentDescription = contentDescription,
            expandedContentDescription = expandedContentDescription,
            items = items,
            modifier = modifier,
        )
    }
}

public val LocalDiaryFloatingActionButtonMenuOverride: ProvidableCompositionLocal<DiaryFloatingActionButtonMenuOverride> =
    staticCompositionLocalOf { MaterialDiaryFloatingActionButtonMenuOverride }
