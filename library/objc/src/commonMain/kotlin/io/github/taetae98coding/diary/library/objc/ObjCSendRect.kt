package io.github.taetae98coding.diary.library.objc

import java.lang.foreign.Arena
import java.lang.foreign.FunctionDescriptor
import java.lang.foreign.MemorySegment
import java.lang.foreign.ValueLayout

private val msgSendRectReturnId =
    ObjCRuntime.msgSendHandle(FunctionDescriptor.of(ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.ADDRESS, ObjCRuntime.cgRectLayout))
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

public fun MemorySegment.send(
    selector: MemorySegment,
    rect: AppKitRect,
): MemorySegment =
    Arena.ofConfined().use { arena ->
        msgSendRectReturnId.invoke(this, selector, rect.toSegment(arena)) as MemorySegment
    }

public fun MemorySegment.send(
    selector: MemorySegment,
    rect: AppKitRect,
    argument: MemorySegment,
): MemorySegment =
    Arena.ofConfined().use { arena ->
        msgSendRectIdReturnId.invoke(this, selector, rect.toSegment(arena), argument) as MemorySegment
    }
