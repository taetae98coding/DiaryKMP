@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3AdaptiveApi::class)

package io.github.taetae98coding.diary.app.shared.navigation

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.material3.VerticalDragHandle
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.layout.PaneExpansionState
import androidx.compose.material3.adaptive.layout.ThreePaneScaffoldScope
import androidx.compose.material3.adaptive.navigation3.rememberListDetailSceneStrategy
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.runtime.result.LocalResultEventBus
import androidx.navigation3.runtime.result.ResultEventBus
import androidx.navigation3.ui.NavDisplay
import io.github.taetae98coding.diary.app.shared.AppState
import io.github.taetae98coding.diary.app.shared.rememberAppState
import io.github.taetae98coding.diary.compose.core.animation.DiaryFadeContentTransform
import io.github.taetae98coding.diary.compose.core.scene.BottomSheetSceneStrategy
import io.github.taetae98coding.diary.core.navigation.ScreenNavKey
import io.github.taetae98coding.diary.feature.calendar.ui.calendarEntry
import io.github.taetae98coding.diary.feature.checklist.ui.checklistEntry
import io.github.taetae98coding.diary.feature.contact.ui.contactEntry
import io.github.taetae98coding.diary.feature.dday.ui.dDayEntry
import io.github.taetae98coding.diary.feature.file.ui.fileEntry
import io.github.taetae98coding.diary.feature.holiday.ui.holidayEntry
import io.github.taetae98coding.diary.feature.login.ui.loginEntry
import io.github.taetae98coding.diary.feature.memo.ui.memoEntry
import io.github.taetae98coding.diary.feature.more.ui.moreEntry
import io.github.taetae98coding.diary.feature.place.ui.placeEntry
import io.github.taetae98coding.diary.feature.playlist.ui.playlistEntry
import io.github.taetae98coding.diary.feature.qr.ui.qrEntry
import io.github.taetae98coding.diary.feature.routine.ui.routineEntry
import io.github.taetae98coding.diary.feature.search.ui.searchEntry
import io.github.taetae98coding.diary.feature.setting.ui.settingEntry
import io.github.taetae98coding.diary.feature.tag.ui.tagEntry
import io.github.taetae98coding.diary.feature.web.ui.webEntry

@Composable
internal fun AppNavigation(
    modifier: Modifier = Modifier,
    appState: AppState = rememberAppState(),
) {
    // 목록·상세 배치의 상세 placeholder는 NavEntry가 아니어서 entry decorator가 감싸지 않는다.
    // placeholder에 놓인 화면도 진입점과 같은 버스로 결과를 주고받도록 NavDisplay 위에서 제공한다.
    // FIXME placeholder를 decorate하지 않는 것은 navigation3의 제약이라 라이브러리에서 고쳐져야 한다. 등록된 이슈는 아직 없다.
    // navigation3 1.2.0-alpha05가 rememberResultEventBusNavEntryDecorator에 버스를 넘기는 오버로드를 추가했으므로(b/516995400),
    // 그 버전을 담은 뒤에는 decorator에 이 버스를 넘겨 여기의 제공을 placeholder 몫으로만 남긴다.
    val resultEventBus = remember { ResultEventBus() }

    CompositionLocalProvider(LocalResultEventBus provides resultEventBus) {
        NavDisplay(
            backStack = appState.backStack,
            modifier = modifier,
            sceneStrategies =
                listOf(
                    remember { BottomSheetSceneStrategy<ScreenNavKey>() },
                    rememberListDetailSceneStrategy(
                        directive = appState.paneScaffoldDirective,
                        paneExpansionDragHandle = { state ->
                            AppPaneExpansionDragHandle(state = state)
                        },
                    ),
                ),
            entryDecorators =
                listOf(
                    rememberSaveableStateHolderNavEntryDecorator(),
                    rememberViewModelStoreNavEntryDecorator(),
                    rememberRetainedValuesStoreNavEntryDecorator(),
                ),
            transitionSpec = { DiaryFadeContentTransform },
            popTransitionSpec = { DiaryFadeContentTransform },
            predictivePopTransitionSpec = { DiaryFadeContentTransform },
            entryProvider =
                entryProvider {
                    memoEntry(backStack = appState.backStack)
                    tagEntry(backStack = appState.backStack)
                    calendarEntry(backStack = appState.backStack)
                    routineEntry(backStack = appState.backStack)
                    moreEntry(backStack = appState.backStack)

                    checklistEntry(backStack = appState.backStack)
                    contactEntry(backStack = appState.backStack)
                    dDayEntry(backStack = appState.backStack)
                    fileEntry(backStack = appState.backStack)
                    holidayEntry(backStack = appState.backStack)
                    loginEntry(backStack = appState.backStack)
                    placeEntry(backStack = appState.backStack)
                    playlistEntry(backStack = appState.backStack)
                    qrEntry(backStack = appState.backStack)
                    searchEntry(backStack = appState.backStack)
                    settingEntry(backStack = appState.backStack)
                    webEntry(backStack = appState.backStack)
                },
        )
    }
}

@Composable
private fun ThreePaneScaffoldScope.AppPaneExpansionDragHandle(
    state: PaneExpansionState,
    modifier: Modifier = Modifier,
) {
    val interactionSource = remember { MutableInteractionSource() }

    VerticalDragHandle(
        modifier =
            modifier.paneExpansionDraggable(
                state = state,
                minTouchTargetSize = LocalMinimumInteractiveComponentSize.current,
                interactionSource = interactionSource,
            ),
        interactionSource = interactionSource,
    )
}
