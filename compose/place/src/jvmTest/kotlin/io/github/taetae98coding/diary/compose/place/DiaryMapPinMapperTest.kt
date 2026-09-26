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
            "TC-PLACE-HOME-FEATURE-024, TC-MEMO-PLACE-CARD-FEATURE-018 장소는 좌표 위치에 컬러 색과 제목 라벨을 가진 핀으로 변환된다",
        ) {
            val place = place()

            val pin = place.toDiaryMapPin()

            pin.coordinate shouldBe place.detail.coordinate.toDiaryMapCoordinate()
            pin.color shouldBe place.detail.color.toColor()
            pin.label shouldBe place.detail.title
        }

        test("TC-PLACE-HOME-DOMAIN-015, TC-MEMO-PLACE-CARD-DOMAIN-016 핀의 식별 값은 그 장소의 식별자다") {
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
