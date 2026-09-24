package io.github.taetae98coding.diary.library.webkit

import java.lang.foreign.Arena
import java.lang.foreign.FunctionDescriptor
import java.lang.foreign.Linker
import java.lang.foreign.MemoryLayout
import java.lang.foreign.MemorySegment
import java.lang.foreign.SymbolLookup
import java.lang.foreign.ValueLayout
import java.lang.invoke.MethodHandle
import java.lang.invoke.MethodHandles
import java.lang.invoke.MethodType
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicLong

/**
 * Objective-C 런타임과 libdispatch를 java.lang.foreign으로 감싼 최소 바인딩.
 * AppKit·WebKit 객체는 메인 스레드에서만 다뤄야 하므로, 모든 호출은 [performOnMainThread]로
 * 감싼 블록 안에서 이뤄져야 한다. 메시지 전송은 [MemorySegment.send] 계열 확장 함수가 맡는다.
 */
internal object ObjCRuntime {
    // AWT 창의 좌표는 point 단위라 AppKit 좌표와 배율 변환 없이 맞는다.
    val cgRectLayout: MemoryLayout =
        MemoryLayout.structLayout(
            ValueLayout.JAVA_DOUBLE.withName("x"),
            ValueLayout.JAVA_DOUBLE.withName("y"),
            ValueLayout.JAVA_DOUBLE.withName("width"),
            ValueLayout.JAVA_DOUBLE.withName("height"),
        )

    private val linker = Linker.nativeLinker()
    private val objc = SymbolLookup.libraryLookup("/usr/lib/libobjc.A.dylib", Arena.global())

    private val objcGetClass = downcall("objc_getClass", FunctionDescriptor.of(ValueLayout.ADDRESS, ValueLayout.ADDRESS))
    private val selRegisterName = downcall("sel_registerName", FunctionDescriptor.of(ValueLayout.ADDRESS, ValueLayout.ADDRESS))
    private val objcAllocateClassPair =
        downcall(
            "objc_allocateClassPair",
            FunctionDescriptor.of(ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.JAVA_LONG),
        )
    private val classAddMethod =
        downcall(
            "class_addMethod",
            FunctionDescriptor.of(
                ValueLayout.JAVA_BOOLEAN,
                ValueLayout.ADDRESS,
                ValueLayout.ADDRESS,
                ValueLayout.ADDRESS,
                ValueLayout.ADDRESS,
            ),
        )
    private val objcRegisterClassPair = downcall("objc_registerClassPair", FunctionDescriptor.ofVoid(ValueLayout.ADDRESS))
    private val autoreleasePoolPush = downcall("objc_autoreleasePoolPush", FunctionDescriptor.of(ValueLayout.ADDRESS))
    private val autoreleasePoolPop = downcall("objc_autoreleasePoolPop", FunctionDescriptor.ofVoid(ValueLayout.ADDRESS))

    private val dispatchAsyncF =
        linker.downcallHandle(
            linker.defaultLookup().find("dispatch_async_f").orElseThrow(),
            FunctionDescriptor.ofVoid(ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.ADDRESS),
        )
    private val mainQueue = linker.defaultLookup().find("_dispatch_main_q").orElseThrow()

    // 전역 블록의 isa. 런타임이 이 클래스의 블록은 복사하거나 해제하지 않는다.
    val concreteGlobalBlock: MemorySegment = linker.defaultLookup().find("_NSConcreteGlobalBlock").orElseThrow()

    private val mainThreadBlockId = AtomicLong(0L)
    private val mainThreadBlocks = ConcurrentHashMap<Long, () -> Unit>()
    private val runOnMainThreadStub =
        linker.upcallStub(
            MethodHandles.lookup().bind(this, "runOnMainThread", MethodType.methodType(Void.TYPE, MemorySegment::class.java)),
            FunctionDescriptor.ofVoid(ValueLayout.ADDRESS),
            Arena.global(),
        )

    private val selectors = ConcurrentHashMap<String, MemorySegment>()
    private val classes = ConcurrentHashMap<String, MemorySegment>()

    init {
        // WKWebView 클래스가 objc 런타임에 등록되도록 WebKit 프레임워크를 프로세스에 적재한다.
        SymbolLookup.libraryLookup("/System/Library/Frameworks/WebKit.framework/WebKit", Arena.global())
    }

    private fun downcall(
        name: String,
        descriptor: FunctionDescriptor,
    ): MethodHandle = linker.downcallHandle(objc.find(name).orElseThrow(), descriptor)

    fun msgSendHandle(descriptor: FunctionDescriptor): MethodHandle = linker.downcallHandle(objc.find("objc_msgSend").orElseThrow(), descriptor)

    fun performOnMainThread(block: () -> Unit) {
        val id = mainThreadBlockId.incrementAndGet()

        mainThreadBlocks[id] = block
        dispatchAsyncF.invoke(mainQueue, MemorySegment.ofAddress(id), runOnMainThreadStub)
    }

    // dispatch_async_f가 메인 스레드에서 호출하는 트램펄린. 네이티브 프레임으로 예외가
    // 전파되면 JVM이 중단되므로 여기서 모두 삼킨다.
    @Suppress("unused")
    private fun runOnMainThread(context: MemorySegment) {
        val block = mainThreadBlocks.remove(context.address()) ?: return

        try {
            withAutoreleasePool(block)
        } catch (throwable: Throwable) {
            Thread.currentThread().uncaughtExceptionHandler?.uncaughtException(Thread.currentThread(), throwable)
        }
    }

    // stringWithUTF8String: 같은 autorelease 반환 객체가 풀 없이 누수되지 않도록,
    // objc 객체를 다루는 블록은 항상 풀 안에서 실행한다.
    fun <T> withAutoreleasePool(block: () -> T): T {
        val pool = autoreleasePoolPush.invoke() as MemorySegment

        try {
            return block()
        } finally {
            autoreleasePoolPop.invoke(pool)
        }
    }

    fun objcClass(name: String): MemorySegment =
        classes.getOrPut(name) {
            Arena.ofConfined().use { arena ->
                objcGetClass.invoke(arena.allocateUtf8String(name)) as MemorySegment
            }
        }

    fun selector(name: String): MemorySegment =
        selectors.getOrPut(name) {
            Arena.ofConfined().use { arena ->
                selRegisterName.invoke(arena.allocateUtf8String(name)) as MemorySegment
            }
        }

    fun allocateClass(
        superclassName: String,
        name: String,
        methodSelector: MemorySegment,
        methodImplementation: MemorySegment,
        methodTypeEncoding: String,
    ): MemorySegment {
        // 클래스 이름과 타입 인코딩 문자열은 objc 런타임이 계속 참조하므로 해제하지 않는다.
        val arena = Arena.global()
        val objcClass =
            objcAllocateClassPair.invoke(objcClass(superclassName), arena.allocateUtf8String(name), 0L) as MemorySegment

        classAddMethod.invoke(objcClass, methodSelector, methodImplementation, arena.allocateUtf8String(methodTypeEncoding))
        objcRegisterClassPair.invoke(objcClass)

        return objcClass
    }

    fun upcallStub(
        handle: MethodHandle,
        descriptor: FunctionDescriptor,
    ): MemorySegment = linker.upcallStub(handle, descriptor, Arena.global())

    fun frameworkSymbol(
        frameworkPath: String,
        name: String,
    ): MemorySegment = SymbolLookup.libraryLookup(frameworkPath, Arena.global()).find(name).orElseThrow()
}
