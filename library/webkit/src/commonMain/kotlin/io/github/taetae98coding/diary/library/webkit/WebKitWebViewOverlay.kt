@file:OptIn(ComposeToolingApi::class, ExperimentalCoroutinesApi::class)

package io.github.taetae98coding.diary.library.webkit

import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.tooling.ComposeToolingApi
import androidx.compose.ui.ComposeDesktopEntryPoint
import androidx.compose.ui.semantics.SemanticsNode
import androidx.compose.ui.semantics.SemanticsOwner
import androidx.compose.ui.semantics.SemanticsPropertyKey
import androidx.compose.ui.semantics.SemanticsPropertyReceiver
import androidx.compose.ui.semantics.getOrNull
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import java.awt.event.HierarchyEvent
import java.awt.event.HierarchyListener
import javax.swing.SwingUtilities
import kotlin.uuid.Uuid

private val WebKitWebViewOverlayIdKey = SemanticsPropertyKey<Uuid>(name = "WebKitWebViewOverlayId")

public var SemanticsPropertyReceiver.webKitWebViewOverlayId: Uuid by WebKitWebViewOverlayIdKey

/**
 * 창의 Compose 레이어(Dialog·Popup)마다 semantics 루트가 하나씩 생기고, 나중에 열린 레이어가 뒤에 온다.
 * 맨 위 레이어의 semantics 트리에 [overlayId]를 단 노드가 없으면 웹뷰보다 위에 레이어가 열린 것이므로 웹뷰를 감춘다.
 * 웹뷰를 두는 SwingPanel의 semantics에 [webKitWebViewOverlayId]로 같은 값을 달아야 한다.
 */
public suspend fun WebKitWebViewPanel.hideWhileCoveredByOverlay(overlayId: Uuid) {
    composeDesktopEntryPointFlow()
        .flatMapLatest { entryPoint ->
            if (entryPoint == null) {
                flowOf(false)
            } else {
                snapshotFlow { entryPoint.semanticsOwners.lastOrNull() }
                    .map { topmost -> topmost != null && !topmost.contains(overlayId) }
            }
        }.collect { isCovered -> isCoveredByOverlay = isCovered }
}

// SwingPanel의 컴포넌트는 배치된 뒤에야 창에 붙으므로, 창 소속이 바뀔 때마다 다시 찾는다.
private fun WebKitWebViewPanel.composeDesktopEntryPointFlow(): Flow<ComposeDesktopEntryPoint?> =
    callbackFlow {
        trySend(SwingUtilities.getWindowAncestor(this@composeDesktopEntryPointFlow) as? ComposeDesktopEntryPoint)

        val listener =
            HierarchyListener { event ->
                if (event.changeFlags and HierarchyEvent.PARENT_CHANGED.toLong() != 0L) {
                    trySend(SwingUtilities.getWindowAncestor(this@composeDesktopEntryPointFlow) as? ComposeDesktopEntryPoint)
                }
            }

        addHierarchyListener(listener)

        awaitClose { removeHierarchyListener(listener) }
    }

private fun SemanticsOwner.contains(overlayId: Uuid): Boolean = unmergedRootSemanticsNode.contains(overlayId)

private fun SemanticsNode.contains(overlayId: Uuid): Boolean = config.getOrNull(WebKitWebViewOverlayIdKey) == overlayId || children.any { child -> child.contains(overlayId) }
