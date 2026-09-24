package io.github.taetae98coding.diary.library.webkit

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import java.lang.foreign.FunctionDescriptor
import java.lang.foreign.Linker
import java.lang.foreign.ValueLayout

class ObjCBlockTest :
    FunSpec({
        test("블록의 invoke 포인터를 호출하면 등록한 콜백이 한 번만 실행된다") {
            var invokeCount = 0
            val block = ObjCBlock.create { invokeCount += 1 }
            val invoke =
                Linker
                    .nativeLinker()
                    .downcallHandle(ObjCBlock.invokeAddress(block), FunctionDescriptor.ofVoid(ValueLayout.ADDRESS))

            invoke.invoke(block)
            invoke.invoke(block)

            invokeCount shouldBe 1
        }

        test("블록은 전역 블록 클래스를 isa로 가진다") {
            val block = ObjCBlock.create {}

            block.reinterpret(ValueLayout.ADDRESS.byteSize()).get(ValueLayout.ADDRESS, 0L).address() shouldBe
                ObjCRuntime.concreteGlobalBlock.address()
        }
    })
