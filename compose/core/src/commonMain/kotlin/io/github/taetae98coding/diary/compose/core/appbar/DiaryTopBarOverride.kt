package io.github.taetae98coding.diary.compose.core.appbar

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier

public interface DiaryTopBarOverride {
    @Composable
    public fun DiaryTopBarOverrideScope.TopBar()
}

public class DiaryTopBarOverrideScope internal constructor(
    public val title: @Composable DiaryTopBarTitleScope.() -> Unit,
    public val modifier: Modifier,
    public val isTitleCentered: Boolean,
    public val navigationIcon: @Composable DiaryTopBarNavigationIconScope.() -> Unit,
    public val actions: @Composable DiaryTopBarActionScope.() -> Unit,
)

public object MaterialDiaryTopBarOverride : DiaryTopBarOverride {
    @Composable
    override fun DiaryTopBarOverrideScope.TopBar() {
        MaterialDiaryTopBar(
            title = title,
            modifier = modifier,
            isTitleCentered = isTitleCentered,
            navigationIcon = navigationIcon,
            actions = actions,
        )
    }
}

public val LocalDiaryTopBarOverride: ProvidableCompositionLocal<DiaryTopBarOverride> = staticCompositionLocalOf { MaterialDiaryTopBarOverride }
