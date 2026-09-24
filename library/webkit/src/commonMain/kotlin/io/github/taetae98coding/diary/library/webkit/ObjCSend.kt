package io.github.taetae98coding.diary.library.webkit

import java.lang.foreign.Arena
import java.lang.foreign.FunctionDescriptor
import java.lang.foreign.MemorySegment
import java.lang.foreign.ValueLayout

private val msgSendReturnId =
    ObjCRuntime.msgSendHandle(FunctionDescriptor.of(ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.ADDRESS))
private val msgSendIdReturnId =
    ObjCRuntime.msgSendHandle(FunctionDescriptor.of(ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.ADDRESS))
private val msgSendIdIdReturnId =
    ObjCRuntime.msgSendHandle(
        FunctionDescriptor.of(ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.ADDRESS),
    )
private val msgSendDoubleReturnId =
    ObjCRuntime.msgSendHandle(FunctionDescriptor.of(ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.JAVA_DOUBLE))
private val msgSendReturnDouble =
    ObjCRuntime.msgSendHandle(FunctionDescriptor.of(ValueLayout.JAVA_DOUBLE, ValueLayout.ADDRESS, ValueLayout.ADDRESS))
private val msgSendReturnBoolean =
    ObjCRuntime.msgSendHandle(FunctionDescriptor.of(ValueLayout.JAVA_BOOLEAN, ValueLayout.ADDRESS, ValueLayout.ADDRESS))
private val msgSendIdReturnBoolean =
    ObjCRuntime.msgSendHandle(FunctionDescriptor.of(ValueLayout.JAVA_BOOLEAN, ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.ADDRESS))
private val msgSendRectIdReturnId =
    ObjCRuntime.msgSendHandle(
        FunctionDescriptor.of(
            ValueLayout.ADDRESS,
            ValueLayout.ADDRESS,
            ValueLayout.ADDRESS,
            ObjCRuntime.cgRectLayout,
            ValueLayout.ADDRESS,
        ),
    )
private val msgSendIdLongBooleanReturnId =
    ObjCRuntime.msgSendHandle(
        FunctionDescriptor.of(
            ValueLayout.ADDRESS,
            ValueLayout.ADDRESS,
            ValueLayout.ADDRESS,
            ValueLayout.ADDRESS,
            ValueLayout.JAVA_LONG,
            ValueLayout.JAVA_BOOLEAN,
        ),
    )

internal fun MemorySegment.send(selector: MemorySegment): MemorySegment = msgSendReturnId.invoke(this, selector) as MemorySegment

internal fun MemorySegment.send(
    selector: MemorySegment,
    argument: MemorySegment,
): MemorySegment = msgSendIdReturnId.invoke(this, selector, argument) as MemorySegment

internal fun MemorySegment.send(
    selector: MemorySegment,
    first: MemorySegment,
    second: MemorySegment,
): MemorySegment = msgSendIdIdReturnId.invoke(this, selector, first, second) as MemorySegment

internal fun MemorySegment.send(
    selector: MemorySegment,
    argument: Double,
): MemorySegment = msgSendDoubleReturnId.invoke(this, selector, argument) as MemorySegment

internal fun MemorySegment.send(
    selector: MemorySegment,
    rect: AppKitRect,
    argument: MemorySegment,
): MemorySegment =
    Arena.ofConfined().use { arena ->
        msgSendRectIdReturnId.invoke(this, selector, rect.toSegment(arena), argument) as MemorySegment
    }

internal fun MemorySegment.send(
    selector: MemorySegment,
    first: MemorySegment,
    second: Long,
    third: Boolean,
): MemorySegment = msgSendIdLongBooleanReturnId.invoke(this, selector, first, second, third) as MemorySegment

internal fun MemorySegment.sendBoolean(
    selector: MemorySegment,
    argument: MemorySegment,
): Boolean = msgSendIdReturnBoolean.invoke(this, selector, argument) as Boolean

internal fun MemorySegment.sendBoolean(selector: MemorySegment): Boolean = msgSendReturnBoolean.invoke(this, selector) as Boolean

internal fun MemorySegment.sendDouble(selector: MemorySegment): Double = msgSendReturnDouble.invoke(this, selector) as Double

// stringWithUTF8String:은 autorelease된 객체를 반환하므로 withAutoreleasePool 안에서 쓴다.
internal fun nsString(value: String): MemorySegment =
    Arena.ofConfined().use { arena ->
        ObjCRuntime.objcClass("NSString").send(ObjCRuntime.selector("stringWithUTF8String:"), arena.allocateUtf8String(value))
    }

internal fun MemorySegment.utf8String(): String =
    send(ObjCRuntime.selector("UTF8String"))
        .reinterpret(Long.MAX_VALUE)
        .getUtf8String(0L)
