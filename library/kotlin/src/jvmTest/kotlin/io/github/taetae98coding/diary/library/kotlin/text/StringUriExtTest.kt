package io.github.taetae98coding.diary.library.kotlin.text

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class StringUriExtTest :
    FunSpec({
        test("unreserved 문자는 그대로 남는다") {
            "azAZ09-_.~".encodeUriComponent() shouldBe "azAZ09-_.~"
        }

        test("공백과 예약 문자는 퍼센트 인코딩된다") {
            "a b&c=d/e?f".encodeUriComponent() shouldBe "a%20b%26c%3Dd%2Fe%3Ff"
        }

        test("한글은 UTF-8 바이트 단위로 퍼센트 인코딩된다") {
            "서울".encodeUriComponent() shouldBe "%EC%84%9C%EC%9A%B8"
        }

        test("빈 문자열은 빈 문자열로 남는다") {
            "".encodeUriComponent() shouldBe ""
        }
    })
