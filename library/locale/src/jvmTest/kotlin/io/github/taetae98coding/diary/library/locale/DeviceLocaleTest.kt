package io.github.taetae98coding.diary.library.locale

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import java.util.Locale

class DeviceLocaleTest :
    FunSpec({
        test("기기 기본 로케일을 BCP 47 언어 태그로 제공한다") {
            val original = Locale.getDefault()

            try {
                Locale.setDefault(Locale.KOREA)
                DeviceLocale.currentLanguageTag() shouldBe "ko-KR"
                Locale.setDefault(Locale.US)
                DeviceLocale.currentLanguageTag() shouldBe "en-US"
            } finally {
                Locale.setDefault(original)
            }
        }

        test("기기 기본 로케일의 지역을 대문자 지역 코드로 제공한다") {
            val original = Locale.getDefault()

            try {
                Locale.setDefault(Locale.KOREA)
                DeviceLocale.currentRegionCode() shouldBe "KR"
                Locale.setDefault(Locale.US)
                DeviceLocale.currentRegionCode() shouldBe "US"
                Locale.setDefault(Locale.JAPAN)
                DeviceLocale.currentRegionCode() shouldBe "JP"
            } finally {
                Locale.setDefault(original)
            }
        }

        test("지역이 없는 로케일이면 빈 지역 코드를 제공한다") {
            val original = Locale.getDefault()

            try {
                Locale.setDefault(Locale.KOREAN)
                DeviceLocale.currentRegionCode() shouldBe ""
            } finally {
                Locale.setDefault(original)
            }
        }
    })
