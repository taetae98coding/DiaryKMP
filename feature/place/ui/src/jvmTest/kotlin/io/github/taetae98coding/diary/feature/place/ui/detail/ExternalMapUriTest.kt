package io.github.taetae98coding.diary.feature.place.ui.detail

import io.github.taetae98coding.diary.compose.map.DiaryMapCoordinate
import io.github.taetae98coding.diary.compose.map.provider.DiaryMapProvider
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

private val SEOUL_CITY_HALL = DiaryMapCoordinate(latitude = 37.5665, longitude = 126.978)
private const val TITLE = "서울시청"
private const val ENCODED_TITLE = "%EC%84%9C%EC%9A%B8%EC%8B%9C%EC%B2%AD"
private const val ADDRESS = "서울 중구 세종대로 110"
private const val ENCODED_TITLE_WITH_ADDRESS =
    "%EC%84%9C%EC%9A%B8%EC%8B%9C%EC%B2%AD%20%EC%84%9C%EC%9A%B8%20%EC%A4%91%EA%B5%AC%20%EC%84%B8%EC%A2%85%EB%8C%80%EB%A1%9C%20110"
private const val ENCODED_ADDRESS = "%EC%84%9C%EC%9A%B8%20%EC%A4%91%EA%B5%AC%20%EC%84%B8%EC%A2%85%EB%8C%80%EB%A1%9C%20110"
private val SMALL_COORDINATE = DiaryMapCoordinate(latitude = 0.0005, longitude = -0.0005)
private const val SAVED_TITLE = "저장된 제목"
private const val APP_NAME = "io.github.taetae98coding.diary"

class ExternalMapUriTest :
    FunSpec({
        test("TC-PLACE-DETAIL-DOMAIN-022 네이버 지도는 좌표에 제목을 붙인 네이버 웹 지도 주소를 만든다") {
            externalMapWebUri(
                provider = DiaryMapProvider.NAVER,
                coordinate = SEOUL_CITY_HALL,
                title = TITLE,
                address = ADDRESS,
            ) shouldBe "https://map.naver.com/?lng=126.978000&lat=37.566500&title=$ENCODED_TITLE"
        }

        test("TC-PLACE-DETAIL-DOMAIN-022 Google 지도는 좌표를 중심으로 제목과 주소를 검색하는 Google 웹 지도 주소를 만든다") {
            externalMapWebUri(
                provider = DiaryMapProvider.GOOGLE,
                coordinate = SEOUL_CITY_HALL,
                title = TITLE,
                address = ADDRESS,
            ) shouldBe "https://www.google.com/maps/search/$ENCODED_TITLE_WITH_ADDRESS/@37.566500,126.978000,15z"
        }

        test("TC-PLACE-DETAIL-DOMAIN-024 제목과 주소가 없으면 네이버 웹 지도 주소에 좌표만 담는다") {
            externalMapWebUri(
                provider = DiaryMapProvider.NAVER,
                coordinate = SEOUL_CITY_HALL,
                title = "",
                address = "",
            ) shouldBe "https://map.naver.com/?lng=126.978000&lat=37.566500"
        }

        test("TC-PLACE-DETAIL-DOMAIN-024 제목과 주소가 없으면 Google 웹 지도 주소에 좌표만 담는다") {
            externalMapWebUri(
                provider = DiaryMapProvider.GOOGLE,
                coordinate = SEOUL_CITY_HALL,
                title = "",
                address = "",
            ) shouldBe "https://www.google.com/maps/search/?api=1&query=37.566500%2C126.978000"
        }

        test("TC-PLACE-DETAIL-DOMAIN-026 제목만 있으면 Google 지도의 검색어는 제목이다") {
            externalMapWebUri(
                provider = DiaryMapProvider.GOOGLE,
                coordinate = SEOUL_CITY_HALL,
                title = TITLE,
                address = "",
            ) shouldBe "https://www.google.com/maps/search/$ENCODED_TITLE/@37.566500,126.978000,15z"
        }

        test("TC-PLACE-DETAIL-DOMAIN-026 주소만 있으면 Google 지도의 검색어는 주소다") {
            externalMapWebUri(
                provider = DiaryMapProvider.GOOGLE,
                coordinate = SEOUL_CITY_HALL,
                title = "",
                address = ADDRESS,
            ) shouldBe "https://www.google.com/maps/search/$ENCODED_ADDRESS/@37.566500,126.978000,15z"
        }

        test("TC-PLACE-DETAIL-DOMAIN-032 네이버 지도 앱 주소는 좌표와 제목과 호출 앱 식별 값을 담는다") {
            naverMapAppUri(
                coordinate = SEOUL_CITY_HALL,
                title = TITLE,
                appName = APP_NAME,
            ) shouldBe "nmap://place?lat=37.566500&lng=126.978000&name=$ENCODED_TITLE&appname=$APP_NAME"
        }

        test("TC-PLACE-DETAIL-DOMAIN-033 제목이 없으면 네이버 지도 앱 주소는 좌표와 호출 앱 식별 값만 담는다") {
            listOf("", "   ").forEach { title ->
                naverMapAppUri(
                    coordinate = SEOUL_CITY_HALL,
                    title = title,
                    appName = APP_NAME,
                ) shouldBe "nmap://map?lat=37.566500&lng=126.978000&appname=$APP_NAME"
            }
        }

        test("TC-PLACE-DETAIL-DOMAIN-034 Google 지도 앱 주소는 검색어와 좌표 중심을 담는다") {
            googleMapAppUri(
                coordinate = SEOUL_CITY_HALL,
                title = TITLE,
                address = ADDRESS,
            ) shouldBe "comgooglemaps://?q=$ENCODED_TITLE_WITH_ADDRESS&center=37.566500,126.978000"
        }

        test("TC-PLACE-DETAIL-DOMAIN-034 제목과 주소가 없으면 Google 지도 앱 주소는 좌표를 검색어로 담는다") {
            googleMapAppUri(
                coordinate = SEOUL_CITY_HALL,
                title = "",
                address = "",
            ) shouldBe "comgooglemaps://?q=37.566500,126.978000"
        }

        test("TC-PLACE-DETAIL-DOMAIN-024 TC-PLACE-DETAIL-DOMAIN-033 TC-PLACE-DETAIL-DOMAIN-034 0에 가까운 좌표도 지수 표기 없이 주소에 담는다") {
            listOf(
                externalMapWebUri(provider = DiaryMapProvider.NAVER, coordinate = SMALL_COORDINATE, title = "", address = "") to
                    "https://map.naver.com/?lng=-0.000500&lat=0.000500",
                externalMapWebUri(provider = DiaryMapProvider.GOOGLE, coordinate = SMALL_COORDINATE, title = "", address = "") to
                    "https://www.google.com/maps/search/?api=1&query=0.000500%2C-0.000500",
                naverMapAppUri(coordinate = SMALL_COORDINATE, title = "", appName = APP_NAME) to
                    "nmap://map?lat=0.000500&lng=-0.000500&appname=$APP_NAME",
                googleMapAppUri(coordinate = SMALL_COORDINATE, title = "", address = "") to
                    "comgooglemaps://?q=0.000500,-0.000500",
            ).forEach { (actual, expected) ->
                actual shouldBe expected
            }
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
    })
