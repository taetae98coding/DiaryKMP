package io.github.taetae98coding.diary.library.objc

import java.lang.foreign.Arena
import java.lang.foreign.MemorySegment
import java.lang.foreign.SymbolLookup
import java.lang.foreign.ValueLayout
import java.util.concurrent.ConcurrentHashMap

public object ObjCFramework {
    public const val FOUNDATION_PATH: String = "/System/Library/Frameworks/Foundation.framework/Foundation"

    private val lookups = ConcurrentHashMap<String, SymbolLookup>()

    public fun load(frameworkPath: String) {
        lookup(frameworkPath)
    }

    public fun symbol(
        frameworkPath: String,
        name: String,
    ): MemorySegment = lookup(frameworkPath).find(name).orElseThrow()

    // 프레임워크가 내보내는 NSString 상수는 포인터 변수라 한 번 더 읽어야 객체가 나온다.
    public fun string(
        frameworkPath: String,
        name: String,
    ): MemorySegment =
        symbol(frameworkPath = frameworkPath, name = name)
            .reinterpret(ValueLayout.ADDRESS.byteSize())
            .get(ValueLayout.ADDRESS, 0L)

    private fun lookup(frameworkPath: String): SymbolLookup = lookups.getOrPut(frameworkPath) { SymbolLookup.libraryLookup(frameworkPath, Arena.global()) }
}
