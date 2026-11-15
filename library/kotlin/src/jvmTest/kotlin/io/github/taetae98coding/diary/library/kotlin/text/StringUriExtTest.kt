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

        test("인코딩한 값을 디코딩하면 원래 글자로 돌아온다") {
            val text = "a b&c=d/e?f 서울 😀 100%"

            text.encodeUriComponent().decodeUriComponent() shouldBe text
        }

        test("퍼센트 형식이 아닌 글자는 그대로 둔다") {
            "100%".decodeUriComponent() shouldBe "100%"
            "%zz%4".decodeUriComponent() shouldBe "%zz%4"
            "%+1서울".decodeUriComponent() shouldBe "%+1서울"
        }

        test("quoted-printable의 =XX 형식을 UTF-8 글자로 되돌린다") {
            "=ED=99=8D길동=3Da".decodeQuotedPrintable() shouldBe "홍길동=a"
            "a=b".decodeQuotedPrintable() shouldBe "a=b"
        }
    })
