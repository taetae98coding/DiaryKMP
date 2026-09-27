package io.github.taetae98coding.diary.library.objc

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

public fun MemorySegment.send(selector: MemorySegment): MemorySegment = msgSendReturnId.invoke(this, selector) as MemorySegment

public fun MemorySegment.send(
    selector: MemorySegment,
    argument: MemorySegment,
): MemorySegment = msgSendIdReturnId.invoke(this, selector, argument) as MemorySegment

public fun MemorySegment.send(
    selector: MemorySegment,
    first: MemorySegment,
    second: MemorySegment,
): MemorySegment = msgSendIdIdReturnId.invoke(this, selector, first, second) as MemorySegment

public fun MemorySegment.send(
    selector: MemorySegment,
    argument: Double,
): MemorySegment = msgSendDoubleReturnId.invoke(this, selector, argument) as MemorySegment

public fun MemorySegment.send(
    selector: MemorySegment,
    first: MemorySegment,
    second: Long,
    third: Boolean,
): MemorySegment = msgSendIdLongBooleanReturnId.invoke(this, selector, first, second, third) as MemorySegment

public fun MemorySegment.sendBoolean(
    selector: MemorySegment,
    argument: MemorySegment,
): Boolean = msgSendIdReturnBoolean.invoke(this, selector, argument) as Boolean

public fun MemorySegment.sendBoolean(selector: MemorySegment): Boolean = msgSendReturnBoolean.invoke(this, selector) as Boolean

public fun MemorySegment.sendDouble(selector: MemorySegment): Double = msgSendReturnDouble.invoke(this, selector) as Double

// stringWithUTF8String:은 autorelease된 객체를 반환하므로 withAutoreleasePool 안에서 쓴다.
public fun nsString(value: String): MemorySegment =
    Arena.ofConfined().use { arena ->
        ObjCRuntime.objcClass("NSString").send(ObjCRuntime.selector("stringWithUTF8String:"), arena.allocateUtf8String(value))
    }

public fun MemorySegment.utf8String(): String =
    send(ObjCRuntime.selector("UTF8String"))
        .reinterpret(Long.MAX_VALUE)
        .getUtf8String(0L)
