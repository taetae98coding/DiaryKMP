package io.github.taetae98coding.diary.core.mapper.place

import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeKotlinBuilder
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.google.network.api.entity.GooglePlaceDisplayNameRemoteEntity
import io.github.taetae98coding.diary.core.google.network.api.entity.GooglePlaceLocationRemoteEntity
import io.github.taetae98coding.diary.core.google.network.api.entity.GooglePlaceRemoteEntity
import io.github.taetae98coding.diary.core.naver.network.api.entity.NaverPlaceRemoteEntity
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.doubles.plusOrMinus
import io.kotest.matchers.doubles.shouldBeNaN
import io.kotest.matchers.shouldBe
import kotlin.uuid.Uuid

class SearchedPlaceMapperTest :
    FunSpec({
        test("TC-PLACE-SEARCH-DIALOG-DOMAIN-010 네이버 이름의 강조 표식을 제거한다") {
            val place = naverPlace(title = "<b>서울</b>역 <B>맛집</B>")

            place.toDomain(id = fixtureMonkey.giveMeOne<Uuid>()).name shouldBe "서울역 맛집"
        }

        test("TC-PLACE-SEARCH-DIALOG-DOMAIN-011 네이버 결과의 주소는 도로명 주소를 먼저 보여 준다") {
            val jibun = "지번-${fixtureMonkey.giveMeOne<String>()}"
            val road = "도로명-${fixtureMonkey.giveMeOne<String>()}"

            listOf(
                Triple(jibun, road, road),
                Triple(jibun, "", jibun),
                Triple("", road, road),
                Triple("", "", ""),
            ).forEach { (address, roadAddress, expected) ->
                val place = naverPlace(address = address, roadAddress = roadAddress)

                place.toDomain(id = fixtureMonkey.giveMeOne<Uuid>()).address shouldBe expected
            }
        }

        test("TC-PLACE-SEARCH-DIALOG-DOMAIN-013 Google 결과의 주소는 전체 주소를 먼저 보여 준다") {
            val formatted = "전체-${fixtureMonkey.giveMeOne<String>()}"
            val short = "짧은-${fixtureMonkey.giveMeOne<String>()}"

            listOf(
                Triple(formatted, short, formatted),
                Triple(formatted, "", formatted),
                Triple("", short, short),
                Triple("", "", ""),
            ).forEach { (formattedAddress, shortFormattedAddress, expected) ->
                val place =
                    googlePlace(
                        formattedAddress = formattedAddress,
                        shortFormattedAddress = shortFormattedAddress,
                    )

                place.toDomain(id = fixtureMonkey.giveMeOne<Uuid>()).address shouldBe expected
            }
        }

        test("네이버 좌표는 위도와 경도로 바꾼다") {
            val place = naverPlace(mapx = "1270276620", mapy = "375657760")

            val actual = place.toDomain(id = fixtureMonkey.giveMeOne<Uuid>())

            actual.coordinate.latitude shouldBe (37.565776 plusOrMinus COORDINATE_TOLERANCE)
            actual.coordinate.longitude shouldBe (127.027662 plusOrMinus COORDINATE_TOLERANCE)
        }

        test("네이버 좌표를 읽을 수 없으면 성립하지 않는 좌표로 둔다") {
            val place = naverPlace(mapx = "", mapy = "좌표 아님")

            val actual = place.toDomain(id = fixtureMonkey.giveMeOne<Uuid>())

            actual.coordinate.latitude.shouldBeNaN()
            actual.coordinate.longitude.shouldBeNaN()
        }

        test("전달한 식별자와 제공자가 돌려준 이름과 좌표를 그대로 담는다") {
            val id = fixtureMonkey.giveMeOne<Uuid>()
            val name = "이름-${fixtureMonkey.giveMeOne<String>()}"
            val latitude = 37.5665
            val longitude = 126.9780
            val place =
                googlePlace(
                    name = name,
                    latitude = latitude,
                    longitude = longitude,
                )

            val actual = place.toDomain(id = id)

            actual.id shouldBe id
            actual.name shouldBe name
            actual.coordinate.latitude shouldBe latitude
            actual.coordinate.longitude shouldBe longitude
        }
    }) {
    private companion object {
        private const val COORDINATE_TOLERANCE = 1e-9

        private val fixtureMonkey: FixtureMonkey =
            diaryFixtureMonkey()

        private fun naverPlace(
            title: String = "이름-${fixtureMonkey.giveMeOne<String>()}",
            address: String = "지번-${fixtureMonkey.giveMeOne<String>()}",
            roadAddress: String = "도로명-${fixtureMonkey.giveMeOne<String>()}",
            mapx: String = "1270276620",
            mapy: String = "375657760",
        ): NaverPlaceRemoteEntity =
            fixtureMonkey
                .giveMeKotlinBuilder<NaverPlaceRemoteEntity>()
                .setExp(NaverPlaceRemoteEntity::title, title)
                .setExp(NaverPlaceRemoteEntity::address, address)
                .setExp(NaverPlaceRemoteEntity::roadAddress, roadAddress)
                .setExp(NaverPlaceRemoteEntity::mapx, mapx)
                .setExp(NaverPlaceRemoteEntity::mapy, mapy)
                .sample()

        private fun googlePlace(
            name: String = "이름-${fixtureMonkey.giveMeOne<String>()}",
            formattedAddress: String = "전체-${fixtureMonkey.giveMeOne<String>()}",
            shortFormattedAddress: String = "짧은-${fixtureMonkey.giveMeOne<String>()}",
            latitude: Double = 37.5665,
            longitude: Double = 126.9780,
        ): GooglePlaceRemoteEntity =
            fixtureMonkey
                .giveMeKotlinBuilder<GooglePlaceRemoteEntity>()
                .setExp(GooglePlaceRemoteEntity::displayName, GooglePlaceDisplayNameRemoteEntity(text = name))
                .setExp(GooglePlaceRemoteEntity::formattedAddress, formattedAddress)
                .setExp(GooglePlaceRemoteEntity::shortFormattedAddress, shortFormattedAddress)
                .setExp(
                    GooglePlaceRemoteEntity::location,
                    GooglePlaceLocationRemoteEntity(latitude = latitude, longitude = longitude),
                ).sample()
    }
}
