package io.github.taetae98coding.diary.compose.place

import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.model.place.Place
import io.github.taetae98coding.diary.core.model.place.PlaceDetail
import io.github.taetae98coding.diary.library.compose.ui.color.toColor
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import kotlin.time.Instant
import kotlin.uuid.Uuid

class DiaryMapPinMapperTest :
    FunSpec({
        test(
            "TC-c4fdf19e-6b46-4b2f-9cb8-cb7a20e2e956, TC-b4820f67-50fa-4e61-a61e-f86dabc63a79 장소는 좌표 위치에 컬러 색과 제목 라벨을 가진 핀으로 변환된다",
        ) {
            val place = place()

            val pin = place.toDiaryMapPin()

            pin.coordinate shouldBe place.detail.coordinate.toDiaryMapCoordinate()
            pin.color shouldBe place.detail.color.toColor()
            pin.label shouldBe place.detail.title
        }

        test("TC-846bef63-ecc0-4f27-8bd4-c5672bb2db4e, TC-324e8d9b-1ec9-4829-82a9-4b300b72947d 핀의 식별 값은 그 장소의 식별자다") {
            val placeList = List(2) { place() }

            val pinList = placeList.map { place -> place.toDiaryMapPin() }

            pinList.map { pin -> pin.id } shouldBe placeList.map { place -> place.id }
            pinList.map { pin -> pin.id }.toSet().size shouldBe placeList.size
        }
    }) {
    private companion object {
        private val fixtureMonkey: FixtureMonkey =
            diaryFixtureMonkey()

        private fun place(): Place =
            Place(
                id = Uuid.random(),
                detail = fixtureMonkey.giveMeOne<PlaceDetail>(),
                isDeleted = false,
                updatedAt = fixtureMonkey.giveMeOne<Instant>(),
                createdAt = fixtureMonkey.giveMeOne<Instant>(),
            )
    }
}
