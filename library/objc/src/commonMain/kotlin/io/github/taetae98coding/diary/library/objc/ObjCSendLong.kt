package io.github.taetae98coding.diary.library.objc

import java.lang.foreign.FunctionDescriptor
import java.lang.foreign.MemorySegment
import java.lang.foreign.ValueLayout

private val msgSendLongReturnId =
    ObjCRuntime.msgSendHandle(FunctionDescriptor.of(ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.JAVA_LONG))
private val msgSendReturnLong =
    ObjCRuntime.msgSendHandle(FunctionDescriptor.of(ValueLayout.JAVA_LONG, ValueLayout.ADDRESS, ValueLayout.ADDRESS))
private val msgSendIdReturnLong =
    ObjCRuntime.msgSendHandle(FunctionDescriptor.of(ValueLayout.JAVA_LONG, ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.ADDRESS))

public fun MemorySegment.send(
    selector: MemorySegment,
    argument: Long,
): MemorySegment = msgSendLongReturnId.invoke(this, selector, argument) as MemorySegment

public fun MemorySegment.sendLong(selector: MemorySegment): Long = msgSendReturnLong.invoke(this, selector) as Long

public fun MemorySegment.sendLong(
    selector: MemorySegment,
    argument: MemorySegment,
): Long = msgSendIdReturnLong.invoke(this, selector, argument) as Long
