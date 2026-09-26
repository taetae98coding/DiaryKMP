package io.github.taetae98coding.diary.library.webkit

import androidx.compose.ui.awt.ComposeDialog
import androidx.compose.ui.awt.ComposeWindow
import io.github.taetae98coding.diary.library.objc.ObjCRuntime
import io.github.taetae98coding.diary.library.objc.appKitFrame
import io.github.taetae98coding.diary.library.objc.nsString
import io.github.taetae98coding.diary.library.objc.send
import io.github.taetae98coding.diary.library.objc.sendVoid
import java.awt.event.ComponentAdapter
import java.awt.event.ComponentEvent
import java.awt.event.HierarchyBoundsListener
import java.awt.event.HierarchyEvent
import java.lang.foreign.MemorySegment
import java.util.concurrent.ConcurrentLinkedQueue
import javax.swing.JPanel
import javax.swing.SwingUtilities

/**
 * WKWebView를 소유한 창의 contentView에 붙이고, 이 패널의 화면 위치를 따라 프레임을
 * 동기화하는 Swing 컴포넌트. macOS 전용이고, Compose 창(ComposeWindow·ComposeDialog)
 * 안에서만 네이티브 창 핸들을 얻을 수 있다.
 */
public class WebKitWebViewPanel : JPanel() {
    private val ipcMessages = ConcurrentLinkedQueue<String>()
    private val bridge = WebKitWebViewBridge(onIpcMessage = ipcMessages::add)

    private var isComponentHidden: Boolean = false
        set(value) {
            field = value
            updateWebViewHidden()
        }

    /**
     * 웹뷰는 창에 직접 붙은 네이티브 뷰라 Compose가 그리는 다이얼로그·팝업 위에 항상 떠 있다.
     * 웹뷰보다 위에 Compose 레이어가 열려 있는 동안 호출자가 `true`로 두면 웹뷰를 감춰 그 레이어가 보이게 한다.
     */
    public var isCoveredByOverlay: Boolean = false
        set(value) {
            field = value
            updateWebViewHidden()
        }

    init {
        addComponentListener(
            object : ComponentAdapter() {
                override fun componentResized(event: ComponentEvent?) {
                    updateWebViewFrame()
                }

                override fun componentMoved(event: ComponentEvent?) {
                    updateWebViewFrame()
                }

                override fun componentShown(event: ComponentEvent?) {
                    isComponentHidden = false
                }

                override fun componentHidden(event: ComponentEvent?) {
                    isComponentHidden = true
                }
            },
        )
        addHierarchyBoundsListener(
            object : HierarchyBoundsListener {
                override fun ancestorMoved(event: HierarchyEvent?) {
                    updateWebViewFrame()
                }

                override fun ancestorResized(event: HierarchyEvent?) {
                    updateWebViewFrame()
                }
            },
        )
    }

    public fun loadUrl(url: String) {
        bridge.load(
            WebKitLoadCommand { target ->
                val nsUrl = ObjCRuntime.objcClass("NSURL").send(ObjCRuntime.selector("URLWithString:"), nsString(url))
                val request = ObjCRuntime.objcClass("NSURLRequest").send(ObjCRuntime.selector("requestWithURL:"), nsUrl)

                target.send(ObjCRuntime.selector("loadRequest:"), request)
            },
        )
    }

    public fun loadHtml(
        html: String,
        baseUrl: String? = null,
    ) {
        bridge.load(
            WebKitLoadCommand { target ->
                val nsBaseUrl =
                    baseUrl
                        ?.let { ObjCRuntime.objcClass("NSURL").send(ObjCRuntime.selector("URLWithString:"), nsString(it)) }
                        ?: MemorySegment.NULL

                target.send(ObjCRuntime.selector("loadHTMLString:baseURL:"), nsString(html), nsBaseUrl)
            },
        )
    }

    public fun evaluateJavaScript(script: String) {
        bridge.perform { target ->
            target.sendVoid(
                ObjCRuntime.selector("evaluateJavaScript:completionHandler:"),
                nsString(script),
                MemorySegment.NULL,
            )
        }
    }

    public fun drainIpcMessages(): List<String> =
        buildList {
            while (true) {
                add(ipcMessages.poll() ?: break)
            }
        }

    override fun addNotify() {
        super.addNotify()

        val nsWindowHandle = nsWindowHandle() ?: return

        bridge.attach(nsWindowHandle)
        updateWebViewFrame()
    }

    override fun removeNotify() {
        bridge.detach()
        super.removeNotify()
    }

    private fun nsWindowHandle(): Long? =
        when (val window = SwingUtilities.getWindowAncestor(this)) {
            is ComposeWindow -> window.windowHandle
            is ComposeDialog -> window.windowHandle
            else -> null
        }

    private fun updateWebViewFrame() {
        val rootPane = SwingUtilities.getRootPane(this) ?: return
        val location = SwingUtilities.convertPoint(this, 0, 0, rootPane)
        val frame =
            appKitFrame(
                x = location.x,
                y = location.y,
                width = width,
                height = height,
                contentHeight = rootPane.height,
            )

        bridge.perform { target -> target.sendVoid(ObjCRuntime.selector("setFrame:"), frame) }
    }

    private fun updateWebViewHidden() {
        val isHidden = isComponentHidden || isCoveredByOverlay

        bridge.perform { target -> target.sendVoid(ObjCRuntime.selector("setHidden:"), isHidden) }
    }
}
