package io.github.taetae98coding.diary.feature.place.ui.detail

import androidx.compose.ui.platform.UriHandler
import io.github.taetae98coding.diary.compose.map.DiaryMapCoordinate
import io.github.taetae98coding.diary.compose.map.DiaryMapProvider
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify

private val SEOUL_CITY_HALL = DiaryMapCoordinate(latitude = 37.5665, longitude = 126.978)
private const val TITLE = "서울시청"
private const val ENCODED_TITLE = "%EC%84%9C%EC%9A%B8%EC%8B%9C%EC%B2%AD"
private const val ADDRESS = "서울 중구 세종대로 110"
private const val ENCODED_TITLE_WITH_ADDRESS =
    "%EC%84%9C%EC%9A%B8%EC%8B%9C%EC%B2%AD%20%EC%84%9C%EC%9A%B8%20%EC%A4%91%EA%B5%AC%20%EC%84%B8%EC%A2%85%EB%8C%80%EB%A1%9C%20110"
private const val ENCODED_ADDRESS = "%EC%84%9C%EC%9A%B8%20%EC%A4%91%EA%B5%AC%20%EC%84%B8%EC%A2%85%EB%8C%80%EB%A1%9C%20110"
private const val SAVED_TITLE = "저장된 제목"

class ExternalMapUriTest :
    FunSpec({
        test("TC-PLACE-DETAIL-DOMAIN-022 네이버 지도는 좌표에 제목을 붙인 네이버 웹 지도 주소를 만든다") {
            externalMapUri(
                provider = DiaryMapProvider.NAVER,
                coordinate = SEOUL_CITY_HALL,
                title = TITLE,
                address = ADDRESS,
            ) shouldBe "https://map.naver.com/?lng=126.978&lat=37.5665&title=$ENCODED_TITLE"
        }

        test("TC-PLACE-DETAIL-DOMAIN-022 Google 지도는 좌표를 중심으로 제목과 주소를 검색하는 Google 웹 지도 주소를 만든다") {
            externalMapUri(
                provider = DiaryMapProvider.GOOGLE,
                coordinate = SEOUL_CITY_HALL,
                title = TITLE,
                address = ADDRESS,
            ) shouldBe "https://www.google.com/maps/search/$ENCODED_TITLE_WITH_ADDRESS/@37.5665,126.978,15z"
        }

        test("TC-PLACE-DETAIL-DOMAIN-024 제목과 주소가 없으면 네이버 웹 지도 주소에 좌표만 담는다") {
            externalMapUri(
                provider = DiaryMapProvider.NAVER,
                coordinate = SEOUL_CITY_HALL,
                title = "",
                address = "",
            ) shouldBe "https://map.naver.com/?lng=126.978&lat=37.5665"
        }

        test("TC-PLACE-DETAIL-DOMAIN-024 제목과 주소가 없으면 Google 웹 지도 주소에 좌표만 담는다") {
            externalMapUri(
                provider = DiaryMapProvider.GOOGLE,
                coordinate = SEOUL_CITY_HALL,
                title = "",
                address = "",
            ) shouldBe "https://www.google.com/maps/search/?api=1&query=37.5665%2C126.978"
        }

        test("TC-PLACE-DETAIL-DOMAIN-026 제목만 있으면 Google 지도의 검색어는 제목이다") {
            externalMapUri(
                provider = DiaryMapProvider.GOOGLE,
                coordinate = SEOUL_CITY_HALL,
                title = TITLE,
                address = "",
            ) shouldBe "https://www.google.com/maps/search/$ENCODED_TITLE/@37.5665,126.978,15z"
        }

        test("TC-PLACE-DETAIL-DOMAIN-026 주소만 있으면 Google 지도의 검색어는 주소다") {
            externalMapUri(
                provider = DiaryMapProvider.GOOGLE,
                coordinate = SEOUL_CITY_HALL,
                title = "",
                address = ADDRESS,
            ) shouldBe "https://www.google.com/maps/search/$ENCODED_ADDRESS/@37.5665,126.978,15z"
        }

        test("TC-PLACE-DETAIL-DOMAIN-025 입력한 제목이 있으면 입력한 제목을 전달한다") {
            externalMapTitle(inputTitle = TITLE, savedTitle = SAVED_TITLE) shouldBe TITLE
        }

        test("TC-PLACE-DETAIL-DOMAIN-025 입력한 제목이 비어 있거나 공백만 있으면 저장된 제목을 전달한다") {
            listOf("", "   ").forEach { inputTitle ->
                externalMapTitle(inputTitle = inputTitle, savedTitle = SAVED_TITLE) shouldBe SAVED_TITLE
            }
        }

        test("TC-PLACE-DETAIL-DOMAIN-025 입력한 제목과 저장된 제목이 모두 비어 있으면 제목을 전달하지 않는다") {
            externalMapTitle(inputTitle = "", savedTitle = "") shouldBe ""
        }

        test("외부 지도를 열지 못해도 실패를 전파하지 않는다") {
            val uriHandler =
                mockk<UriHandler> {
                    every { openUri(any()) } throws IllegalArgumentException("외부 지도를 열 수 없습니다.")
                }

            uriHandler.openExternalMap(
                provider = DiaryMapProvider.NAVER,
                coordinate = SEOUL_CITY_HALL,
                title = TITLE,
                address = ADDRESS,
            )

            verify(exactly = 1) {
                uriHandler.openUri(
                    externalMapUri(
                        provider = DiaryMapProvider.NAVER,
                        coordinate = SEOUL_CITY_HALL,
                        title = TITLE,
                        address = ADDRESS,
                    ),
                )
            }
        }
    })
