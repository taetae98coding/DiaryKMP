package io.github.taetae98coding.diary.library.webkit

import io.github.taetae98coding.diary.library.objc.AppKitRect
import io.github.taetae98coding.diary.library.objc.ObjCRuntime
import io.github.taetae98coding.diary.library.objc.nsString
import io.github.taetae98coding.diary.library.objc.release
import io.github.taetae98coding.diary.library.objc.send
import io.github.taetae98coding.diary.library.objc.sendVoid
import java.lang.foreign.MemorySegment

private const val IPC_HANDLER_NAME = "ipc"

// wry가 노출하던 window.ipc 계약을 유지해 웹 문서를 수정하지 않고 메시지를 받는다.
private const val IPC_BRIDGE_SCRIPT =
    "window.ipc = { postMessage: (message) => window.webkit.messageHandlers.$IPC_HANDLER_NAME.postMessage(message) };"

private const val INJECTION_TIME_AT_DOCUMENT_START = 0L

internal class WebKitLoadCommand(
    val apply: (MemorySegment) -> Unit,
)

/**
 * WKWebView의 생성·해제와 명령 실행 순서를 책임진다. 웹뷰가 아직 없으면 명령을 쌓아 두고,
 * 생성 직후 마지막 로드 명령과 쌓인 명령을 순서대로 적용한다. 모든 상태는 dispatch 메인
 * 큐에서만 읽고 쓴다.
 */
internal class WebKitWebViewBridge(
    private val onIpcMessage: (String) -> Unit,
) {
    private var webView: MemorySegment? = null
    private var messageHandler: WebKitScriptMessageHandler? = null
    private var appliedLoad: WebKitLoadCommand? = null
    private val pendingOperations = ArrayDeque<(MemorySegment) -> Unit>()

    @Volatile
    private var lastLoad: WebKitLoadCommand? = null

    fun load(command: WebKitLoadCommand) {
        lastLoad = command

        ObjCRuntime.performOnMainThread {
            val target = webView ?: return@performOnMainThread

            // 웹뷰 생성 시점에 lastLoad를 이미 적용했으면 같은 명령을 다시 로드하지 않는다.
            if (appliedLoad === command) {
                return@performOnMainThread
            }

            appliedLoad = command
            command.apply(target)
        }
    }

    fun perform(operation: (MemorySegment) -> Unit) {
        ObjCRuntime.performOnMainThread {
            val target = webView

            if (target == null) {
                pendingOperations.addLast(operation)
            } else {
                operation(target)
            }
        }
    }

    fun attach(nsWindowHandle: Long) {
        ObjCRuntime.performOnMainThread { attachWebView(nsWindowHandle) }
    }

    fun detach() {
        ObjCRuntime.performOnMainThread { detachWebView() }
    }

    private fun attachWebView(nsWindowHandle: Long) {
        if (webView != null) {
            return
        }

        val configuration = webKitClass("WKWebViewConfiguration").send(ObjCRuntime.selector("new"))
        val contentController = configuration.send(ObjCRuntime.selector("userContentController"))
        val messageHandler = WebKitScriptMessageHandler(onMessage = onIpcMessage)

        contentController.sendVoid(
            ObjCRuntime.selector("addScriptMessageHandler:name:"),
            messageHandler.pointer,
            nsString(IPC_HANDLER_NAME),
        )

        val userScript =
            webKitClass("WKUserScript")
                .send(ObjCRuntime.selector("alloc"))
                .send(
                    ObjCRuntime.selector("initWithSource:injectionTime:forMainFrameOnly:"),
                    nsString(IPC_BRIDGE_SCRIPT),
                    INJECTION_TIME_AT_DOCUMENT_START,
                    true,
                )

        contentController.sendVoid(ObjCRuntime.selector("addUserScript:"), userScript)
        userScript.release()

        val webView =
            webKitClass("WKWebView")
                .send(ObjCRuntime.selector("alloc"))
                .send(ObjCRuntime.selector("initWithFrame:configuration:"), AppKitRect.ZERO, configuration)

        configuration.release()

        val contentView = MemorySegment.ofAddress(nsWindowHandle).send(ObjCRuntime.selector("contentView"))

        contentView.sendVoid(ObjCRuntime.selector("addSubview:"), webView)

        this.webView = webView
        this.messageHandler = messageHandler

        lastLoad?.let { command ->
            appliedLoad = command
            command.apply(webView)
        }

        while (pendingOperations.isNotEmpty()) {
            pendingOperations.removeFirst()(webView)
        }
    }

    private fun detachWebView() {
        val webView = this.webView ?: return

        this.webView = null
        appliedLoad = null

        val configuration = webView.send(ObjCRuntime.selector("configuration"))
        val contentController = configuration.send(ObjCRuntime.selector("userContentController"))

        contentController.sendVoid(ObjCRuntime.selector("removeScriptMessageHandlerForName:"), nsString(IPC_HANDLER_NAME))
        webView.sendVoid(ObjCRuntime.selector("removeFromSuperview"))
        webView.release()

        messageHandler?.dispose()
        messageHandler = null
    }
}
