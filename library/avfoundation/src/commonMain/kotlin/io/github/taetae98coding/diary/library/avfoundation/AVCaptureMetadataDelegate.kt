package io.github.taetae98coding.diary.library.avfoundation

import io.github.taetae98coding.diary.library.objc.ObjCRuntime
import io.github.taetae98coding.diary.library.objc.release
import io.github.taetae98coding.diary.library.objc.send
import io.github.taetae98coding.diary.library.objc.sendBoolean
import io.github.taetae98coding.diary.library.objc.sendLong
import io.github.taetae98coding.diary.library.objc.utf8String
import java.lang.foreign.FunctionDescriptor
import java.lang.foreign.MemorySegment
import java.lang.foreign.ValueLayout
import java.lang.invoke.MethodHandles
import java.lang.invoke.MethodType
import java.util.concurrent.ConcurrentHashMap

/**
 * AVCaptureMetadataOutputObjectsDelegate를 구현하는 objc 인스턴스를 만들어, 출력이 인식한 코드의 문자열 값을
 * [onDetect]로 전달한다. 출력은 대리 객체를 약하게 참조하므로 출력에서 떼어 낸 뒤 [dispose]로 해제한다.
 */
internal class AVCaptureMetadataDelegate(
    private val onDetect: (String) -> Unit,
) {
    val pointer: MemorySegment = delegateClass.send(ObjCRuntime.selector("alloc")).send(ObjCRuntime.selector("init"))

    init {
        registry[pointer.address()] = this
    }

    fun dispose() {
        registry.remove(pointer.address())
        pointer.release()
    }

    private fun deliver(metadataObjects: MemorySegment) {
        val codeClass = avFoundationClass("AVMetadataMachineReadableCodeObject")
        val count = metadataObjects.sendLong(ObjCRuntime.selector("count"))

        for (index in 0L until count) {
            val metadataObject = metadataObjects.send(ObjCRuntime.selector("objectAtIndex:"), index)

            if (metadataObject.sendBoolean(ObjCRuntime.selector("isKindOfClass:"), codeClass)) {
                val stringValue = metadataObject.send(ObjCRuntime.selector("stringValue"))

                onDetect(if (stringValue.isNil()) "" else stringValue.utf8String())
            }
        }
    }

    companion object {
        private val registry = ConcurrentHashMap<Long, AVCaptureMetadataDelegate>()

        private val delegateClass: MemorySegment by lazy(::createDelegateClass)

        // MethodHandles.lookup()은 호출한 클래스의 private 접근 권한만 가지므로,
        // didOutputMetadataObjects와 같은 클래스인 이 메서드 본문에서 lookup을 만든다.
        private fun createDelegateClass(): MemorySegment =
            ObjCRuntime.allocateClass(
                superclassName = "NSObject",
                name = "DiaryAVCaptureMetadataDelegate",
                methodSelector = ObjCRuntime.selector("captureOutput:didOutputMetadataObjects:fromConnection:"),
                methodImplementation =
                    ObjCRuntime.upcallStub(
                        handle =
                            MethodHandles.lookup().bind(
                                this,
                                "didOutputMetadataObjects",
                                MethodType.methodType(
                                    Void.TYPE,
                                    MemorySegment::class.java,
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
                                ValueLayout.ADDRESS,
                            ),
                    ),
                methodTypeEncoding = "v@:@@@",
            )

        // 출력에 넘긴 dispatch 큐에서 호출된다. 네이티브 프레임으로 예외가 전파되면
        // JVM이 중단되므로 여기서 모두 삼킨다.
        @Suppress("unused", "UnusedParameter")
        private fun didOutputMetadataObjects(
            self: MemorySegment,
            command: MemorySegment,
            output: MemorySegment,
            metadataObjects: MemorySegment,
            connection: MemorySegment,
        ) {
            try {
                registry[self.address()]?.deliver(metadataObjects)
            } catch (throwable: Throwable) {
                Thread.currentThread().uncaughtExceptionHandler?.uncaughtException(Thread.currentThread(), throwable)
            }
        }
    }
}
