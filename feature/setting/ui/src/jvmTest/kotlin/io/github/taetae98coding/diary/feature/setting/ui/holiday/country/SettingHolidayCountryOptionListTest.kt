package io.github.taetae98coding.diary.feature.setting.ui.holiday.country

import io.github.taetae98coding.diary.domain.holiday.model.HolidayCountryOption
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.shouldBe

class SettingHolidayCountryOptionListTest :
    FunSpec({
        test("TC-SETTING-HOLIDAY-FEATURE-034 국가 선택지는 기기값, 한국, 미국 순이다") {
            settingHolidayCountryOptionList shouldBe
                listOf(
                    HolidayCountryOption.DEVICE,
                    HolidayCountryOption.KOREA,
                    HolidayCountryOption.UNITED_STATES,
                )
        }

        test("국가 선택지 목록은 모든 선택지를 한 번씩만 담는다") {
            settingHolidayCountryOptionList shouldContainExactlyInAnyOrder HolidayCountryOption.entries
        }
    })
