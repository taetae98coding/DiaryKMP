package io.github.taetae98coding.diary.library.webkit

import io.github.taetae98coding.diary.library.objc.ObjCRuntime
import io.github.taetae98coding.diary.library.objc.release
import io.github.taetae98coding.diary.library.objc.send
import io.github.taetae98coding.diary.library.objc.sendBoolean
import io.github.taetae98coding.diary.library.objc.utf8String
import java.lang.foreign.FunctionDescriptor
import java.lang.foreign.MemorySegment
import java.lang.foreign.ValueLayout
import java.lang.invoke.MethodHandles
import java.lang.invoke.MethodType
import java.util.concurrent.ConcurrentHashMap

/**
 * WKScriptMessageHandler 프로토콜을 구현하는 objc 인스턴스를 만들어, 웹 문서가 보낸
 * 메시지를 [onMessage]로 전달한다. 메인 스레드에서만 생성하고 해제해야 한다.
 */
internal class WebKitScriptMessageHandler(
    private val onMessage: (String) -> Unit,
) {
    val pointer: MemorySegment = handlerClass.send(ObjCRuntime.selector("alloc")).send(ObjCRuntime.selector("init"))

    init {
        registry[pointer.address()] = this
    }

    fun dispose() {
        registry.remove(pointer.address())
        pointer.release()
    }

    private fun deliver(message: MemorySegment) {
        val body = message.send(ObjCRuntime.selector("body"))
        val nsString =
            if (body.sendBoolean(ObjCRuntime.selector("isKindOfClass:"), ObjCRuntime.objcClass("NSString"))) {
                body
            } else {
                body.send(ObjCRuntime.selector("description"))
            }

        onMessage(nsString.utf8String())
    }

    companion object {
        private val registry = ConcurrentHashMap<Long, WebKitScriptMessageHandler>()

        private val handlerClass: MemorySegment by lazy(::createHandlerClass)

        // MethodHandles.lookup()은 호출한 클래스의 private 접근 권한만 가지므로,
        // didReceiveScriptMessage와 같은 클래스인 이 메서드 본문에서 lookup을 만든다.
        private fun createHandlerClass(): MemorySegment =
            ObjCRuntime.allocateClass(
                superclassName = "NSObject",
                name = "DiaryWebKitScriptMessageHandler",
                methodSelector = ObjCRuntime.selector("userContentController:didReceiveScriptMessage:"),
                methodImplementation =
                    ObjCRuntime.upcallStub(
                        handle =
                            MethodHandles.lookup().bind(
                                this,
                                "didReceiveScriptMessage",
                                MethodType.methodType(
                                    Void.TYPE,
                                    MemorySegment::class.java,
                                    MemorySegment::class.java,
                                    MemorySegment::class.java,
                                    MemorySegment::class.java,
                                ),
                            ),
                        descriptor =
                            FunctionDescriptor.ofVoid(
                                ValueLayout.ADDRESS,
                                ValueLayout.ADDRESS,
                                ValueLayout.ADDRESS,
                                ValueLayout.ADDRESS,
                            ),
                    ),
                methodTypeEncoding = "v@:@@",
            )

        // objc 런타임이 메인 스레드에서 호출한다. 네이티브 프레임으로 예외가 전파되면
        // JVM이 중단되므로 여기서 모두 삼킨다.
        @Suppress("unused", "UnusedParameter")
        private fun didReceiveScriptMessage(
            self: MemorySegment,
            command: MemorySegment,
            controller: MemorySegment,
            message: MemorySegment,
        ) {
            try {
                registry[self.address()]?.deliver(message)
            } catch (throwable: Throwable) {
                Thread.currentThread().uncaughtExceptionHandler?.uncaughtException(Thread.currentThread(), throwable)
            }
        }
    }
}
