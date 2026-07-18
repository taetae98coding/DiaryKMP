package io.github.taetae98coding.diary.library.webkit

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
private val msgSendRectReturnVoid =
    ObjCRuntime.msgSendHandle(FunctionDescriptor.ofVoid(ValueLayout.ADDRESS, ValueLayout.ADDRESS, ObjCRuntime.cgRectLayout))

internal fun MemorySegment.sendVoid(selector: MemorySegment) {
    msgSendReturnVoid.invoke(this, selector)
}

internal fun MemorySegment.sendVoid(
    selector: MemorySegment,
    argument: MemorySegment,
) {
    msgSendIdReturnVoid.invoke(this, selector, argument)
}

internal fun MemorySegment.sendVoid(
    selector: MemorySegment,
    first: MemorySegment,
    second: MemorySegment,
) {
    msgSendIdIdReturnVoid.invoke(this, selector, first, second)
}

internal fun MemorySegment.sendVoid(
    selector: MemorySegment,
    argument: Boolean,
) {
    msgSendBooleanReturnVoid.invoke(this, selector, argument)
}

internal fun MemorySegment.sendVoid(
    selector: MemorySegment,
    rect: AppKitRect,
) {
    Arena.ofConfined().use { arena ->
        msgSendRectReturnVoid.invoke(this, selector, rect.toSegment(arena))
    }
}

internal fun MemorySegment.release() {
    sendVoid(ObjCRuntime.selector("release"))
}
