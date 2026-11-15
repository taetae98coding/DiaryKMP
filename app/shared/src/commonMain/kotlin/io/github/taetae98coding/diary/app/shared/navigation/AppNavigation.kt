@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3AdaptiveApi::class)

package io.github.taetae98coding.diary.app.shared.navigation

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.material3.VerticalDragHandle
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.layout.PaneExpansionState
import androidx.compose.material3.adaptive.layout.ThreePaneScaffoldScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavEntryDecorator
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.runtime.result.LocalResultEventBus
import androidx.navigation3.runtime.result.ResultEventBus
import androidx.navigation3.runtime.result.rememberResultEventBus
import androidx.navigation3.runtime.result.rememberResultEventBusNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import io.github.taetae98coding.diary.app.shared.AppState
import io.github.taetae98coding.diary.app.shared.rememberAppState
import io.github.taetae98coding.diary.compose.core.animation.DiaryFadeContentTransform
import io.github.taetae98coding.diary.compose.core.appbar.rememberDiaryTopBarAlphaNavEntryDecorator
import io.github.taetae98coding.diary.compose.core.scene.BottomSheetSceneStrategy
import io.github.taetae98coding.diary.compose.core.scene.ListDetailPlaceholderStateHolder
import io.github.taetae98coding.diary.compose.core.scene.LocalListDetailPlaceholderStateHolder
import io.github.taetae98coding.diary.compose.core.scene.rememberDiaryListDetailSceneStrategy
import io.github.taetae98coding.diary.compose.core.scene.rememberListDetailPlaceholderNavEntryDecorator
import io.github.taetae98coding.diary.compose.core.scene.rememberListDetailPlaceholderStateHolder
import io.github.taetae98coding.diary.core.navigation.ScreenEntryInstaller
import io.github.taetae98coding.diary.core.navigation.ScreenNavKey

@Composable
internal fun AppNavigation(
    entryInstallerList: List<ScreenEntryInstaller>,
    modifier: Modifier = Modifier,
    appState: AppState = rememberAppState(),
) {
    // 목록·상세 배치의 상세 placeholder는 NavEntry가 아니어서 entry decorator가 감싸지 않는다.
    // entry는 decorator가 결과 버스를 제공하고, placeholder에 놓인 화면도 같은 버스로 결과를 주고받도록 NavDisplay 위에서 한 번 더 제공한다.
    // 같은 이유로 placeholder의 저장 상태와 ViewModel이 목록 entry의 수명을 따르도록 placeholder 전용 저장소를 제공하고, 목록 entry가 빠질 때 decorator가 비운다.
    // FIXME placeholder를 decorate하지 않는 것은 navigation3의 제약이라 라이브러리에서 고쳐져야 한다. 등록된 이슈는 아직 없다.
    val resultEventBus = rememberResultEventBus()
    val placeholderStateHolder = rememberListDetailPlaceholderStateHolder()

    CompositionLocalProvider(
        LocalResultEventBus provides resultEventBus,
        LocalListDetailPlaceholderStateHolder provides placeholderStateHolder,
    ) {
        NavDisplay(
            backStack = appState.backStack,
            modifier = modifier.navigationHorizontalInsetsPadding(),
            sceneStrategies =
                listOf(
                    remember { BottomSheetSceneStrategy<ScreenNavKey>() },
                    rememberDiaryListDetailSceneStrategy(
                        directive = appState.paneScaffoldDirective,
                        paneExpansionDragHandle = { state ->
                            AppPaneExpansionDragHandle(state = state)
                        },
                    ),
                ),
            entryDecorators = rememberAppNavEntryDecorators(placeholderStateHolder, resultEventBus),
            transitionSpec = { DiaryFadeContentTransform },
            popTransitionSpec = { DiaryFadeContentTransform },
            predictivePopTransitionSpec = { DiaryFadeContentTransform },
            entryProvider =
                entryProvider {
                    entryInstallerList.forEach { installer -> with(installer) { install(navigator = appState) } }
                },
        )
    }
}

@Composable
private fun rememberAppNavEntryDecorators(
    placeholderStateHolder: ListDetailPlaceholderStateHolder,
    resultEventBus: ResultEventBus,
): List<NavEntryDecorator<ScreenNavKey>> =
    listOf(
        rememberSaveableStateHolderNavEntryDecorator(),
        rememberViewModelStoreNavEntryDecorator(),
        rememberRetainedValuesStoreNavEntryDecorator(),
        rememberResultEventBusNavEntryDecorator(resultEventBus),
        rememberListDetailPlaceholderNavEntryDecorator(placeholderStateHolder),
        rememberDiaryTopBarAlphaNavEntryDecorator(),
    )

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
