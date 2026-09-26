package io.github.taetae98coding.diary.library.objc

import java.lang.foreign.Arena
import java.lang.foreign.FunctionDescriptor
import java.lang.foreign.MemorySegment
import java.lang.foreign.ValueLayout

private val msgSendReturnVoid = ObjCRuntime.msgSendHandle(FunctionDescriptor.ofVoid(ValueLayout.ADDRESS, ValueLayout.ADDRESS))
private val msgSendIdReturnVoid =
    ObjCRuntime.msgSendHandle(FunctionDescriptor.ofVoid(ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.ADDRESS))
private val msgSendIdIdReturnVoid =
    ObjCRuntime.msgSendHandle(FunctionDescriptor.ofVoid(ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.ADDRESS))
private val msgSendBooleanReturnVoid =
    ObjCRuntime.msgSendHandle(FunctionDescriptor.ofVoid(ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.JAVA_BOOLEAN))
private val msgSendIdIdIdReturnVoid =
    ObjCRuntime.msgSendHandle(
        FunctionDescriptor.ofVoid(ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.ADDRESS),
    )
private val msgSendRectReturnVoid =
    ObjCRuntime.msgSendHandle(FunctionDescriptor.ofVoid(ValueLayout.ADDRESS, ValueLayout.ADDRESS, ObjCRuntime.cgRectLayout))

public fun MemorySegment.sendVoid(selector: MemorySegment) {
    msgSendReturnVoid.invoke(this, selector)
}

public fun MemorySegment.sendVoid(
    selector: MemorySegment,
    argument: MemorySegment,
) {
    msgSendIdReturnVoid.invoke(this, selector, argument)
}

public fun MemorySegment.sendVoid(
    selector: MemorySegment,
    first: MemorySegment,
    second: MemorySegment,
) {
    msgSendIdIdReturnVoid.invoke(this, selector, first, second)
}

public fun MemorySegment.sendVoid(
    selector: MemorySegment,
    first: MemorySegment,
    second: MemorySegment,
    third: MemorySegment,
) {
    msgSendIdIdIdReturnVoid.invoke(this, selector, first, second, third)
}

public fun MemorySegment.sendVoid(
    selector: MemorySegment,
    argument: Boolean,
) {
    msgSendBooleanReturnVoid.invoke(this, selector, argument)
}

public fun MemorySegment.sendVoid(
    selector: MemorySegment,
    rect: AppKitRect,
) {
    Arena.ofConfined().use { arena ->
        msgSendRectReturnVoid.invoke(this, selector, rect.toSegment(arena))
    }
}

public fun MemorySegment.release() {
    sendVoid(ObjCRuntime.selector("release"))
}
