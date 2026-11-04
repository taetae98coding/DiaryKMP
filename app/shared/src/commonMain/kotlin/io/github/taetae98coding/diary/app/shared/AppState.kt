@file:OptIn(ExperimentalMaterial3AdaptiveApi::class)

package io.github.taetae98coding.diary.app.shared

import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfoV2
import androidx.compose.material3.adaptive.layout.PaneScaffoldDirective
import androidx.compose.material3.adaptive.layout.calculatePaneScaffoldDirectiveWithTwoPanesOnMediumWidth
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffoldState
import androidx.compose.material3.adaptive.navigationsuite.rememberNavigationSuiteScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.deeplink.DeepLinkRequest
import io.github.taetae98coding.diary.app.shared.navigation.AppDeepLinkMatcher
import io.github.taetae98coding.diary.app.shared.navigation.AppNavKeySavedStateConfiguration
import io.github.taetae98coding.diary.app.shared.navigation.TopLevelNavigation
import io.github.taetae98coding.diary.app.shared.navigation.TopLevelReselectEvent
import io.github.taetae98coding.diary.app.shared.navigation.matchOrNull
import io.github.taetae98coding.diary.app.shared.navigation.rememberScreenNavBackStack
import io.github.taetae98coding.diary.core.navigation.ScreenNavKey
import io.github.taetae98coding.diary.feature.calendar.api.CalendarHomeFilterNavKey
import io.github.taetae98coding.diary.feature.file.api.FileAddNavKey
import io.github.taetae98coding.diary.feature.file.api.FileHomeNavKey
import io.github.taetae98coding.diary.feature.file.api.isFileListDetailPane
import io.github.taetae98coding.diary.feature.memo.api.MemoHomeFilterNavKey
import io.github.taetae98coding.diary.feature.memo.api.isMemoListDetailPane
import io.github.taetae98coding.diary.feature.routine.api.isRoutineListDetailPane
import io.github.taetae98coding.diary.feature.tag.api.TagHomeFilterNavKey
import io.github.taetae98coding.diary.feature.tag.api.isTagListDetailPane
import io.github.taetae98coding.diary.feature.timetable.api.CalendarTimetableNavKey
import kotlin.uuid.Uuid

@Stable
internal class AppState(
    val backStack: NavBackStack<ScreenNavKey>,
    val scaffoldState: NavigationSuiteScaffoldState,
    val reselectEvent: TopLevelReselectEvent,
    private val paneScaffoldDirectiveProvider: () -> PaneScaffoldDirective,
) {
    val paneScaffoldDirective: PaneScaffoldDirective
        get() = paneScaffoldDirectiveProvider()

    val currentTopLevelNavigation: TopLevelNavigation? by derivedStateOf {
        backStack
            .asReversed()
            .firstNotNullOfOrNull { key -> TopLevelNavigation.entries.firstOrNull { it.key == key } }
    }

    val currentScreenNavKey: ScreenNavKey? by derivedStateOf {
        backStack.lastOrNull()
    }

    val isNavigationVisible: Boolean by derivedStateOf {
        isTopLevelVisible() || isTopLevelListVisible()
    }

    fun navigateTo(topLevelNavigation: TopLevelNavigation) {
        if (backStack.lastOrNull() == topLevelNavigation.key) {
            reselectEvent.send(topLevelNavigation)

            return
        }

        val navKeyList =
            listOf(TopLevelNavigation.DEFAULT, topLevelNavigation)
                .distinct()
                .map(TopLevelNavigation::key)

        backStack.clear()
        backStack.addAll(navKeyList)
    }

    fun openDeepLink(deepLink: String) {
        val request = DeepLinkRequest(deepLink)

        AppDeepLinkMatcher.fileHome.matchOrNull(request)?.let { result ->
            openFileHome(result.backStack)
            return
        }
        AppDeepLinkMatcher.timetable.matchOrNull(request)?.let { result ->
            openTimetable(result.key)
        }
    }

    private fun openTimetable(navKey: CalendarTimetableNavKey) {
        val openedKey = navKey.copy(id = Uuid.random())

        if (backStack.lastOrNull() is CalendarTimetableNavKey) {
            backStack[backStack.lastIndex] = openedKey
        } else {
            backStack.add(openedKey)
        }
    }

    private fun openFileHome(navKeyList: List<ScreenNavKey>) {
        if (isFileScreenVisible()) return

        backStack.clear()
        backStack.addAll(navKeyList)
    }

    private fun isFileScreenVisible(): Boolean =
        when (backStack.lastOrNull()) {
            FileHomeNavKey -> true
            FileAddNavKey -> backStack.isFileListDetailPane(key = FileAddNavKey)
            else -> false
        }

    private fun isTopLevelVisible(): Boolean {
        val currentKey = currentContentKey()

        return TopLevelNavigation.entries.any { topLevelNavigation -> topLevelNavigation.key == currentKey }
    }

    private fun isTopLevelListVisible(): Boolean {
        if (paneScaffoldDirective.maxHorizontalPartitions <= 1) return false

        val currentKey = currentContentKey()

        return currentKey != null &&
            (
                backStack.isMemoListDetailPane(currentKey) ||
                    backStack.isTagListDetailPane(currentKey) ||
                    backStack.isRoutineListDetailPane(currentKey)
            )
    }

    private fun currentContentKey(): ScreenNavKey? = backStack.lastOrNull { key -> key !in OverlayNavKeySet }
}

private val OverlayNavKeySet: Set<ScreenNavKey> =
    setOf(
        MemoHomeFilterNavKey,
        CalendarHomeFilterNavKey,
        TagHomeFilterNavKey,
    )

@Composable
internal fun rememberAppState(): AppState {
    val backStack =
        rememberScreenNavBackStack(
            configuration = AppNavKeySavedStateConfiguration,
            elements = arrayOf(TopLevelNavigation.DEFAULT.key),
        )
    val scaffoldState = rememberNavigationSuiteScaffoldState()
    val paneScaffoldDirective by rememberUpdatedState(
        calculatePaneScaffoldDirectiveWithTwoPanesOnMediumWidth(currentWindowAdaptiveInfoV2()),
    )

    return remember(backStack, scaffoldState) {
        AppState(
            backStack = backStack,
            scaffoldState = scaffoldState,
            reselectEvent = TopLevelReselectEvent(),
            paneScaffoldDirectiveProvider = { paneScaffoldDirective },
        )
    }
}
