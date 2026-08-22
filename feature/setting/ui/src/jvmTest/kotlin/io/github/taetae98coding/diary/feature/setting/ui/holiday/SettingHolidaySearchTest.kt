package io.github.taetae98coding.diary.feature.setting.ui.holiday

import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

private val fixtureMonkey: FixtureMonkey =
    diaryFixtureMonkey()

class SettingHolidaySearchTest : FunSpec() {
    init {
        test("TC-SETTING-HOLIDAY-DOMAIN-008 공백을 무시한 부분 일치로 검색을 판정한다") {
            val displayName = "대체 공휴일(설날)"
            val expectedByQuery =
                mapOf(
                    "설날" to true,
                    "대체공휴일" to true,
                    " 대 체 " to true,
                    "추석" to false,
                )

            expectedByQuery.forEach { (query, expected) ->
                matchesSettingHolidaySearch(query = query, displayName = displayName) shouldBe expected
            }
        }

        test("TC-SETTING-HOLIDAY-DOMAIN-009 영문 검색어는 대소문자를 구분하지 않는다") {
            val displayName = "Christmas Day"

            matchesSettingHolidaySearch(query = "christmas", displayName = displayName) shouldBe true
            matchesSettingHolidaySearch(query = "CHRISTMAS", displayName = displayName) shouldBe true
        }

        test("공백뿐인 검색어는 모든 표시 이름과 일치한다") {
            val displayName = fixtureMonkey.giveMeOne<String>()

            matchesSettingHolidaySearch(query = "", displayName = displayName) shouldBe true
            matchesSettingHolidaySearch(query = " \t ", displayName = displayName) shouldBe true
        }
    }
}
