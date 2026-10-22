package io.github.taetae98coding.diary.feature.place.ui.search

import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.model.location.Coordinate
import io.github.taetae98coding.diary.core.model.place.SearchedPlace
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import kotlin.uuid.Uuid

private val fixtureMonkey: FixtureMonkey = diaryFixtureMonkey()

class PlaceSearchResultColorTest : FunSpec() {
    init {
        test("TC-f419b964-23f9-4a19-a0f7-a8103f2a58c2 이름과 좌표가 같은 장소는 다시 받아도 같은 결과 컬러를 갖는다") {
            val place = fixtureMonkey.giveMeOne<SearchedPlace>()
            val researched = place.copy(id = Uuid.random(), address = place.address + ADDRESS_SUFFIX)

            researched.resultColor shouldBe place.resultColor
            place.resultColor.alpha shouldBe 1F
        }

        test("TC-eeb4d847-d46e-40be-886b-7fdadca58ded 이름이나 좌표가 다른 장소는 각자의 이름과 좌표로 결과 컬러를 정한다") {
            val place = fixtureMonkey.giveMeOne<SearchedPlace>().copy(coordinate = validCoordinate())
            val variantList =
                listOf(
                    place.copy(name = place.name + NAME_SUFFIX),
                    place.copy(coordinate = place.coordinate.copy(latitude = place.coordinate.latitude + COORDINATE_OFFSET)),
                    place.copy(coordinate = place.coordinate.copy(longitude = place.coordinate.longitude + COORDINATE_OFFSET)),
                )

            variantList.forEach { other -> other.copy(id = Uuid.random()).resultColor shouldBe other.resultColor }
            variantList.any { other -> other.resultColor != place.resultColor } shouldBe true
        }
    }

    private companion object {
        private const val ADDRESS_SUFFIX = "-changed"
        private const val NAME_SUFFIX = "-other"
        private const val COORDINATE_OFFSET = 0.001
        private const val LATITUDE_RANGE = 170
        private const val LATITUDE_MIN = -85.0
        private const val LONGITUDE_RANGE = 350
        private const val LONGITUDE_MIN = -175.0

        // 생성한 Double은 아주 큰 값일 수 있어 작은 차이를 더해도 값이 바뀌지 않으므로, 지도에 놓일 수 있는 범위의 좌표를 쓴다.
        private fun validCoordinate(): Coordinate =
            Coordinate(
                latitude = LATITUDE_MIN + fixtureMonkey.giveMeOne<Int>().mod(LATITUDE_RANGE),
                longitude = LONGITUDE_MIN + fixtureMonkey.giveMeOne<Int>().mod(LONGITUDE_RANGE),
            )
    }
}
