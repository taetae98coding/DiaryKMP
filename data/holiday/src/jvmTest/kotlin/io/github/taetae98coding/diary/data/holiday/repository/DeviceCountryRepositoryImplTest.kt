package io.github.taetae98coding.diary.data.holiday.repository

import io.github.taetae98coding.diary.core.model.holiday.HolidayCountry
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import java.util.Locale

class DeviceCountryRepositoryImplTest :
    FunSpec({
        test("기기 지역을 공휴일 국가로 바꾸고 지원하지 않는 지역이면 국가가 없다") {
            val original = Locale.getDefault()

            try {
                mapOf(
                    Locale.KOREA to HolidayCountry.KOREA,
                    Locale.US to HolidayCountry.UNITED_STATES,
                    Locale.JAPAN to null,
                    Locale.forLanguageTag("en-KR") to HolidayCountry.KOREA,
                    Locale.forLanguageTag("ko-US") to HolidayCountry.UNITED_STATES,
                ).forEach { (locale, expected) ->
                    Locale.setDefault(locale)

                    DeviceCountryRepositoryImpl().find() shouldBe expected
                }
            } finally {
                Locale.setDefault(original)
            }
        }
    })
