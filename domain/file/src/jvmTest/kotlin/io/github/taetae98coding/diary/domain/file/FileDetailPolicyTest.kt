package io.github.taetae98coding.diary.domain.file

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class FileDetailPolicyTest :
    FunSpec({
        test("TC-FILE-DETAIL-DOMAIN-003 파일 이름의 길이는 유니코드 문자 단위로 센다") {
            listOf(
                "a".repeat(255) to 255,
                "😀".repeat(255) to 255,
                "👨‍👩‍👧" to 5,
                "" to 0,
            ).forEach { (name, expected) ->
                name.fileNameLength() shouldBe expected
            }
        }

        test("짝이 없는 대리 문자는 한 자로 센다") {
            "a\uD83D".fileNameLength() shouldBe 2
        }
    })
