@file:OptIn(ExperimentalMaterial3AdaptiveApi::class)

package io.github.taetae98coding.diary.compose.core.scene

import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfoV2
import androidx.compose.material3.adaptive.layout.PaneExpansionState
import androidx.compose.material3.adaptive.layout.PaneScaffoldDirective
import androidx.compose.material3.adaptive.layout.ThreePaneScaffoldScope
import androidx.compose.material3.adaptive.layout.calculatePaneScaffoldDirective
import androidx.compose.material3.adaptive.navigation.BackNavigationBehavior
import androidx.compose.material3.adaptive.navigation3.ListDetailSceneStrategy
import androidx.compose.material3.adaptive.navigation3.rememberListDetailSceneStrategy
import androidx.compose.runtime.Composable

/**
 * 앱의 모든 목록·상세 배치가 쓰는 [ListDetailSceneStrategy]를 만든다.
 *
 * 라이브러리 기본값인 [BackNavigationBehavior.PopUntilScaffoldValueChange]는 scaffold의 표시 모양이 바뀔 때까지 이력을 건너뛴다.
 * 두 영역을 함께 표시하면 상세 영역에 무엇이 놓여도 모양이 같으므로, 시스템 뒤로가기가 상세 영역을 한 단계 되돌리지 않고 배치 전체를 떠난다.
 * 한 영역만 표시할 때와 같이 back stack의 마지막 entry 하나만 꺼내도록 [BackNavigationBehavior.PopLatest]를 쓴다.
 */
@Composable
public fun <T : Any> rememberDiaryListDetailSceneStrategy(
    directive: PaneScaffoldDirective = calculatePaneScaffoldDirective(currentWindowAdaptiveInfoV2()),
    paneExpansionDragHandle: (@Composable ThreePaneScaffoldScope.(PaneExpansionState) -> Unit)? = null,
): ListDetailSceneStrategy<T> =
    rememberListDetailSceneStrategy(
        backNavigationBehavior = BackNavigationBehavior.PopLatest,
        directive = directive,
        paneExpansionDragHandle = paneExpansionDragHandle,
    )
