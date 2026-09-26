package io.github.taetae98coding.diary.library.avfoundation

import androidx.compose.ui.awt.ComposeDialog
import androidx.compose.ui.awt.ComposeWindow
import io.github.taetae98coding.diary.library.objc.appKitFrame
import java.awt.Color
import java.awt.event.ComponentAdapter
import java.awt.event.ComponentEvent
import java.awt.event.HierarchyBoundsListener
import java.awt.event.HierarchyEvent
import javax.swing.JPanel
import javax.swing.SwingUtilities

/**
 * [scanner]의 카메라 영상을 화면 비율을 채우도록 보여 주는 NSView를 소유한 창의 contentView에 붙이고, 이 패널의 화면 위치를 따라
 * 프레임을 동기화하는 Swing 컴포넌트. macOS 전용이고, Compose 창(ComposeWindow·ComposeDialog) 안에서만 네이티브 창 핸들을 얻을 수 있다.
 * 영상이 없는 동안에는 뷰가 투명해 패널의 배경이 보인다.
 */
public class AVCaptureVideoPreviewPanel : JPanel() {
    private val bridge = AVCaptureVideoPreviewBridge()

    public var scanner: AVCaptureQrScanner? = null
        set(value) {
            field = value
            bridge.setSession(value?.session)
        }

    init {
        background = Color.BLACK
        addComponentListener(
            object : ComponentAdapter() {
                override fun componentResized(event: ComponentEvent?) {
                    updatePreviewFrame()
                }

                override fun componentMoved(event: ComponentEvent?) {
                    updatePreviewFrame()
                }

                override fun componentShown(event: ComponentEvent?) {
                    bridge.setHidden(false)
                }

                override fun componentHidden(event: ComponentEvent?) {
                    bridge.setHidden(true)
                }
            },
        )
        addHierarchyBoundsListener(
            object : HierarchyBoundsListener {
                override fun ancestorMoved(event: HierarchyEvent?) {
                    updatePreviewFrame()
                }

                override fun ancestorResized(event: HierarchyEvent?) {
                    updatePreviewFrame()
                }
            },
        )
    }

    override fun addNotify() {
        super.addNotify()

        val nsWindowHandle = nsWindowHandle() ?: return

        bridge.attach(nsWindowHandle)
        updatePreviewFrame()
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

    private fun updatePreviewFrame() {
        val rootPane = SwingUtilities.getRootPane(this) ?: return
        val location = SwingUtilities.convertPoint(this, 0, 0, rootPane)

        bridge.setFrame(
            appKitFrame(
                x = location.x,
                y = location.y,
                width = width,
                height = height,
                contentHeight = rootPane.height,
            ),
        )
    }
}
