package io.github.taetae98coding.diary.core.navigation

import androidx.navigation3.runtime.NavBackStack
import kotlinx.coroutines.flow.Flow

public interface ScreenNavigator {
    public val backStack: NavBackStack<ScreenNavKey>

    public fun reselectEventOf(topLevelKey: ScreenNavKey): Flow<Unit>

    public fun topLevelBackStackOf(topLevelKey: ScreenNavKey): List<ScreenNavKey>
}
