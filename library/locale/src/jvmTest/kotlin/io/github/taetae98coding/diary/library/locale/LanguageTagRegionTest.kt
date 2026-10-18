package io.github.taetae98coding.diary.library.locale

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class LanguageTagRegionTest :
    FunSpec({
        test("TC-HOLIDAY-COUNTRY-DOMAIN-007 웹 앱에서는 브라우저 언어 설정에 적힌 지역을 기기 지역으로 쓴다") {
            listOf(
                "ko-KR" to "KR",
                "en-US" to "US",
                "ja-JP" to "JP",
                "ko" to "",
            ).forEach { (languageTag, regionCode) ->
                languageTag.languageTagRegionCode() shouldBe regionCode
            }
        }

        test("문자 두 개가 아닌 하위 태그는 지역으로 보지 않는다") {
            "zh-Hant-TW".languageTagRegionCode() shouldBe "TW"
            "es-419".languageTagRegionCode() shouldBe ""
            "".languageTagRegionCode() shouldBe ""
        }
    })
