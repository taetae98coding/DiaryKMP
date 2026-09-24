package io.github.taetae98coding.diary.library.webkit

import java.lang.foreign.Arena
import java.lang.foreign.FunctionDescriptor
import java.lang.foreign.MemoryLayout
import java.lang.foreign.MemorySegment
import java.lang.foreign.ValueLayout
import java.lang.invoke.MethodHandles
import java.lang.invoke.MethodType
import java.util.concurrent.ConcurrentHashMap

/**
 * 인자 없는 Objective-C 블록(`void (^)(void)`)을 만든다. 런타임이 복사·해제하지 않는 전역 블록 형태로 만들고,
 * 블록이 호출되면 등록한 콜백을 한 번 실행한다. WebKit의 completion handler는 메인 스레드에서 호출된다.
 */
internal object ObjCBlock {
    private const val BLOCK_IS_GLOBAL = 1 shl 28
    private const val BLOCK_HAS_SIGNATURE = 1 shl 30

    // 반환 없이 블록 포인터 하나만 받는 함수의 objc 타입 인코딩.
    private const val SIGNATURE = "v8@?0"

    private const val ISA_FIELD = "isa"
    private const val FLAGS_FIELD = "flags"
    private const val RESERVED_FIELD = "reserved"
    private const val INVOKE_FIELD = "invoke"
    private const val DESCRIPTOR_FIELD = "descriptor"
    private const val SIZE_FIELD = "size"
    private const val SIGNATURE_FIELD = "signature"

    private val blockLayout: MemoryLayout =
        MemoryLayout.structLayout(
            ValueLayout.ADDRESS.withName(ISA_FIELD),
            ValueLayout.JAVA_INT.withName(FLAGS_FIELD),
            ValueLayout.JAVA_INT.withName(RESERVED_FIELD),
            ValueLayout.ADDRESS.withName(INVOKE_FIELD),
            ValueLayout.ADDRESS.withName(DESCRIPTOR_FIELD),
        )
    private val descriptorLayout: MemoryLayout =
        MemoryLayout.structLayout(
            ValueLayout.JAVA_LONG.withName(RESERVED_FIELD),
            ValueLayout.JAVA_LONG.withName(SIZE_FIELD),
            ValueLayout.ADDRESS.withName(SIGNATURE_FIELD),
        )

    private val callbacks = ConcurrentHashMap<Long, () -> Unit>()

    private val invokeStub: MemorySegment =
        ObjCRuntime.upcallStub(
            handle = MethodHandles.lookup().bind(this, "invoke", MethodType.methodType(Void.TYPE, MemorySegment::class.java)),
            descriptor = FunctionDescriptor.ofVoid(ValueLayout.ADDRESS),
        )

    private val descriptor: MemorySegment =
        Arena.global().allocate(descriptorLayout).apply {
            set(ValueLayout.JAVA_LONG, descriptorLayout.byteOffset(MemoryLayout.PathElement.groupElement(RESERVED_FIELD)), 0L)
            set(ValueLayout.JAVA_LONG, descriptorLayout.byteOffset(MemoryLayout.PathElement.groupElement(SIZE_FIELD)), blockLayout.byteSize())
            set(
                ValueLayout.ADDRESS,
                descriptorLayout.byteOffset(MemoryLayout.PathElement.groupElement(SIGNATURE_FIELD)),
                Arena.global().allocateUtf8String(SIGNATURE),
            )
        }

    // 런타임이 블록을 마지막으로 읽는 시점을 알 수 없어 블록 메모리는 해제하지 않는다.
    fun create(callback: () -> Unit): MemorySegment {
        val block = Arena.global().allocate(blockLayout)

        block.set(ValueLayout.ADDRESS, blockLayout.byteOffset(MemoryLayout.PathElement.groupElement(ISA_FIELD)), ObjCRuntime.concreteGlobalBlock)
        block.set(ValueLayout.JAVA_INT, blockLayout.byteOffset(MemoryLayout.PathElement.groupElement(FLAGS_FIELD)), BLOCK_IS_GLOBAL or BLOCK_HAS_SIGNATURE)
        block.set(ValueLayout.JAVA_INT, blockLayout.byteOffset(MemoryLayout.PathElement.groupElement(RESERVED_FIELD)), 0)
        block.set(ValueLayout.ADDRESS, blockLayout.byteOffset(MemoryLayout.PathElement.groupElement(INVOKE_FIELD)), invokeStub)
        block.set(ValueLayout.ADDRESS, blockLayout.byteOffset(MemoryLayout.PathElement.groupElement(DESCRIPTOR_FIELD)), descriptor)

        callbacks[block.address()] = callback

        return block
    }

    fun invokeAddress(block: MemorySegment): MemorySegment =
        block
            .reinterpret(blockLayout.byteSize())
            .get(ValueLayout.ADDRESS, blockLayout.byteOffset(MemoryLayout.PathElement.groupElement(INVOKE_FIELD)))

    // 네이티브 프레임으로 예외가 전파되면 JVM이 중단되므로 여기서 모두 삼킨다.
    @Suppress("unused")
    private fun invoke(block: MemorySegment) {
        try {
            callbacks.remove(block.address())?.invoke()
        } catch (throwable: Throwable) {
            Thread.currentThread().uncaughtExceptionHandler?.uncaughtException(Thread.currentThread(), throwable)
        }
    }
}
