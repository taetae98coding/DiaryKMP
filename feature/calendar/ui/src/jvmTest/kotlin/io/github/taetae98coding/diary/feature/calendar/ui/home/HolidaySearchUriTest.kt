package io.github.taetae98coding.diary.feature.calendar.ui.home

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

private const val NAVER_SEARCH_URI = "https://search.naver.com/search.naver?query="

class HolidaySearchUriTest :
    FunSpec({
        test("TC-CALENDAR-HOME-DOMAIN-001 공휴일 이름으로 Naver 통합검색 주소를 만든다") {
            listOf(
                "제헌절" to "%EC%A0%9C%ED%97%8C%EC%A0%88",
                "New Year" to "New%20Year",
            ).forEach { (name, query) ->
                holidaySearchUri(name = name) shouldBe "$NAVER_SEARCH_URI$query"
            }
        }

        test("주소에 그대로 담을 수 있는 글자는 바꾸지 않는다") {
            holidaySearchUri(name = "a-Z_0.9~") shouldBe "${NAVER_SEARCH_URI}a-Z_0.9~"
        }

        test("검색어를 구분하는 글자는 인코딩해 검색어에 포함한다") {
            holidaySearchUri(name = "a&b=c?d#e") shouldBe "${NAVER_SEARCH_URI}a%26b%3Dc%3Fd%23e"
        }

        test("이름이 비어 있으면 검색어가 비어 있는 주소를 만든다") {
            holidaySearchUri(name = "") shouldBe NAVER_SEARCH_URI
        }
    })
