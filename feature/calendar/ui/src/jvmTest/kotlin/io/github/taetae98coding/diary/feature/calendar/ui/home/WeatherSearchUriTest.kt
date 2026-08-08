package io.github.taetae98coding.diary.feature.calendar.ui.home

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

private const val NAVER_SEARCH_URI = "https://search.naver.com/search.naver?query="

class WeatherSearchUriTest :
    FunSpec({
        test("TC-CALENDAR-HOME-DOMAIN-008 지역명으로 날씨 검색 주소를 만든다") {
            listOf(
                "성남시" to "%EC%84%B1%EB%82%A8%EC%8B%9C%20%EB%82%A0%EC%94%A8",
                "Paris" to "Paris%20%EB%82%A0%EC%94%A8",
                "" to "%EB%82%A0%EC%94%A8",
            ).forEach { (locationName, query) ->
                weatherSearchUri(locationName = locationName) shouldBe "$NAVER_SEARCH_URI$query"
            }
        }

        test("검색어를 구분하는 글자는 인코딩해 검색어에 포함한다") {
            weatherSearchUri(locationName = "a&b") shouldBe "${NAVER_SEARCH_URI}a%26b%20%EB%82%A0%EC%94%A8"
        }
    })
