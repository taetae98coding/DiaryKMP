package io.github.taetae98coding.diary.library.webkit

import io.github.taetae98coding.diary.library.objc.ObjCFramework
import io.github.taetae98coding.diary.library.objc.ObjCRuntime
import java.lang.foreign.MemorySegment

internal const val WEBKIT_PATH: String = "/System/Library/Frameworks/WebKit.framework/WebKit"

internal fun webKitClass(name: String): MemorySegment {
    ObjCFramework.load(WEBKIT_PATH)

    return ObjCRuntime.objcClass(name)
}
