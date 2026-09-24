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
    })
