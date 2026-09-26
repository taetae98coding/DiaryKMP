package io.github.taetae98coding.diary.library.avfoundation

import io.github.taetae98coding.diary.library.objc.ObjCFramework
import io.github.taetae98coding.diary.library.objc.ObjCRuntime
import java.lang.foreign.MemorySegment

private const val AVFOUNDATION_PATH = "/System/Library/Frameworks/AVFoundation.framework/AVFoundation"

internal fun avFoundationClass(name: String): MemorySegment {
    ObjCFramework.load(AVFOUNDATION_PATH)

    return ObjCRuntime.objcClass(name)
}

internal fun avFoundationString(name: String): MemorySegment = ObjCFramework.string(frameworkPath = AVFOUNDATION_PATH, name = name)

internal fun MemorySegment.isNil(): Boolean = address() == 0L
