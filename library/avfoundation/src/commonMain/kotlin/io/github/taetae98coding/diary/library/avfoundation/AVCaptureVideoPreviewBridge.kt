package io.github.taetae98coding.diary.library.avfoundation

import io.github.taetae98coding.diary.library.objc.AppKitRect
import io.github.taetae98coding.diary.library.objc.ObjCRuntime
import io.github.taetae98coding.diary.library.objc.release
import io.github.taetae98coding.diary.library.objc.send
import io.github.taetae98coding.diary.library.objc.sendVoid
import java.lang.foreign.MemorySegment

/**
 * 미리보기를 그리는 NSView와 그 안의 AVCaptureVideoPreviewLayer의 생성·해제와 배치를 책임진다.
 * 창에 붙기 전에 받은 세션·프레임·감춤 상태는 기억해 두었다가 뷰를 만들 때 적용하고, 붙잡은 세션은 [setSession]에 null을 넘길 때 놓는다. 모든 상태는 dispatch 메인 큐에서만 읽고 쓴다.
 */
internal class AVCaptureVideoPreviewBridge {
    private var view: MemorySegment? = null
    private var previewLayer: MemorySegment? = null
    private var session: MemorySegment? = null
    private var frame: AppKitRect = AppKitRect.ZERO
    private var isHidden: Boolean = false

    fun attach(nsWindowHandle: Long) {
        ObjCRuntime.performOnMainThread { attachView(nsWindowHandle) }
    }

    fun detach() {
        ObjCRuntime.performOnMainThread { detachView() }
    }

    // 세션을 먼저 해제해도 미리보기가 끊기지 않도록 이 브리지가 세션을 따로 붙잡는다.
    fun setSession(session: MemorySegment?) {
        ObjCRuntime.performOnMainThread {
            if (this.session?.address() == session?.address()) return@performOnMainThread

            removePreviewLayer()
            this.session?.release()
            this.session = session?.send(ObjCRuntime.selector("retain"))
            addPreviewLayer()
        }
    }

    fun setFrame(frame: AppKitRect) {
        ObjCRuntime.performOnMainThread {
            this.frame = frame
            applyFrame()
        }
    }

    fun setHidden(isHidden: Boolean) {
        ObjCRuntime.performOnMainThread {
            this.isHidden = isHidden
            view?.sendVoid(ObjCRuntime.selector("setHidden:"), isHidden)
        }
    }

    private fun attachView(nsWindowHandle: Long) {
        if (view != null) return

        val view = ObjCRuntime.objcClass("NSView").send(ObjCRuntime.selector("alloc")).send(ObjCRuntime.selector("initWithFrame:"), AppKitRect.ZERO)

        view.sendVoid(ObjCRuntime.selector("setWantsLayer:"), true)
        view.sendVoid(ObjCRuntime.selector("setHidden:"), isHidden)
        MemorySegment.ofAddress(nsWindowHandle).send(ObjCRuntime.selector("contentView")).sendVoid(ObjCRuntime.selector("addSubview:"), view)

        this.view = view
        addPreviewLayer()
        applyFrame()
    }

    private fun detachView() {
        val view = this.view ?: return

        removePreviewLayer()
        this.view = null
        view.sendVoid(ObjCRuntime.selector("removeFromSuperview"))
        view.release()
    }

    private fun addPreviewLayer() {
        val session = session ?: return
        val hostLayer = view?.send(ObjCRuntime.selector("layer"))?.takeUnless { layer -> layer.isNil() } ?: return
        val previewLayer = avFoundationClass("AVCaptureVideoPreviewLayer").send(ObjCRuntime.selector("layerWithSession:"), session)

        previewLayer.sendVoid(ObjCRuntime.selector("setVideoGravity:"), avFoundationString("AVLayerVideoGravityResizeAspectFill"))
        hostLayer.sendVoid(ObjCRuntime.selector("addSublayer:"), previewLayer)

        this.previewLayer = previewLayer
        applyFrame()
    }

    // 상위 레이어가 붙잡고 있던 참조만 남아 있으므로 떼어 내면 미리보기 레이어가 해제된다.
    private fun removePreviewLayer() {
        val previewLayer = this.previewLayer ?: return

        this.previewLayer = null
        previewLayer.sendVoid(ObjCRuntime.selector("removeFromSuperlayer"))
    }

    // 하위 레이어의 프레임을 바꾸면 기본으로 짧은 애니메이션이 붙어 창 크기를 바꿀 때 영상이 늦게 따라오므로 끈다.
    private fun applyFrame() {
        view?.sendVoid(ObjCRuntime.selector("setFrame:"), frame)

        val previewLayer = previewLayer ?: return
        val transaction = ObjCRuntime.objcClass("CATransaction")

        transaction.sendVoid(ObjCRuntime.selector("begin"))
        transaction.sendVoid(ObjCRuntime.selector("setDisableActions:"), true)
        previewLayer.sendVoid(ObjCRuntime.selector("setFrame:"), frame.copy(x = 0.0, y = 0.0))
        transaction.sendVoid(ObjCRuntime.selector("commit"))
    }
}
