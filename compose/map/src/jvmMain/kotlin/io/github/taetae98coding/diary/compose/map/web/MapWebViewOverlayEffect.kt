@file:OptIn(ComposeToolingApi::class)

package io.github.taetae98coding.diary.compose.map.web

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.tooling.ComposeToolingApi
import androidx.compose.ui.ComposeDesktopEntryPoint
import androidx.compose.ui.semantics.SemanticsNode
import androidx.compose.ui.semantics.SemanticsOwner
import androidx.compose.ui.semantics.SemanticsPropertyKey
import androidx.compose.ui.semantics.SemanticsPropertyReceiver
import androidx.compose.ui.semantics.getOrNull
import io.github.taetae98coding.diary.library.webkit.WebKitWebViewPanel
import java.awt.event.HierarchyEvent
import java.awt.event.HierarchyListener
import javax.swing.SwingUtilities
import kotlin.uuid.Uuid

private val MapWebViewOverlayIdKey = SemanticsPropertyKey<Uuid>(name = "MapWebViewOverlayId")

internal var SemanticsPropertyReceiver.mapWebViewOverlayId: Uuid by MapWebViewOverlayIdKey

/**
 * 창의 Compose 레이어(Dialog·Popup)마다 semantics 루트가 하나씩 생기고, 나중에 열린 레이어가 뒤에 온다.
 * 맨 위 레이어의 semantics 트리에 이 지도가 없으면 지도보다 위에 레이어가 열린 것이므로 웹뷰를 감춘다.
 */
@Composable
internal fun MapWebViewOverlayEffect(
    webViewPanel: WebKitWebViewPanel,
    overlayId: Uuid,
) {
    val entryPoint = rememberComposeDesktopEntryPoint(webViewPanel)

    LaunchedEffect(webViewPanel, overlayId, entryPoint) {
        if (entryPoint == null) {
            webViewPanel.isCoveredByOverlay = false
            return@LaunchedEffect
        }

        snapshotFlow { entryPoint.semanticsOwners.lastOrNull() }
            .collect { topmost ->
                webViewPanel.isCoveredByOverlay = topmost != null && !topmost.contains(overlayId)
            }
    }
}

// SwingPanel의 컴포넌트는 배치된 뒤에야 창에 붙으므로, 창 소속이 바뀔 때마다 다시 찾는다.
@Composable
private fun rememberComposeDesktopEntryPoint(webViewPanel: WebKitWebViewPanel): ComposeDesktopEntryPoint? {
    var entryPoint by remember(webViewPanel) {
        mutableStateOf(SwingUtilities.getWindowAncestor(webViewPanel) as? ComposeDesktopEntryPoint)
    }

    DisposableEffect(webViewPanel) {
        val listener =
            HierarchyListener { event ->
                if (event.changeFlags and HierarchyEvent.PARENT_CHANGED.toLong() != 0L) {
                    entryPoint = SwingUtilities.getWindowAncestor(webViewPanel) as? ComposeDesktopEntryPoint
                }
            }

        webViewPanel.addHierarchyListener(listener)

        onDispose {
            webViewPanel.removeHierarchyListener(listener)
        }
    }

    return entryPoint
}

private fun SemanticsOwner.contains(overlayId: Uuid): Boolean = unmergedRootSemanticsNode.contains(overlayId)

private fun SemanticsNode.contains(overlayId: Uuid): Boolean = config.getOrNull(MapWebViewOverlayIdKey) == overlayId || children.any { child -> child.contains(overlayId) }
