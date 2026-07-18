package io.github.taetae98coding.diary.library.webkit

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

// UTF-8 인코딩 규칙 자체가 검증 대상이라 임의 생성 대신 대표 고정 값을 쓴다.
// 임의 문자열은 unpaired surrogate를 담을 수 있어 UTF-8 왕복이 정의되지 않는다.
class ObjCRuntimeTest : FunSpec() {
    init {
        test("NSString으로 변환한 문자열을 UTF-8 그대로 되돌린다") {
            val values =
                listOf(
                    "",
                    "plain ascii",
                    "한글 본문",
                    "emoji 😀🗺️",
                    "\"quote\" <script> & 'single'",
                    "줄바꿈\n탭\t끝",
                )

            ObjCRuntime.withAutoreleasePool {
                values.forEach { value ->
                    nsString(value).utf8String() shouldBe value
                }
            }
        }
    }
}
