package io.github.taetae98coding.diary.feature.tag.ui.detail.place

import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.model.location.Coordinate
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe

class TagDetailPlaceStateTest :
    FunSpec({
        test("TC-TAG-DETAIL-PLACE-FEATURE-020 TC-TAG-DETAIL-PLACE-DOMAIN-007 새로 만든 상태는 목록 모드로 시작한다") {
            TagDetailPlaceState().viewMode shouldBe TagDetailPlaceViewMode.LIST
        }

        test("TC-TAG-DETAIL-PLACE-FEATURE-021 TC-TAG-DETAIL-PLACE-FEATURE-022 전환할 때마다 두 보기 모드를 오간다") {
            val state = TagDetailPlaceState()

            state.toggleViewMode()
            state.viewMode shouldBe TagDetailPlaceViewMode.MAP

            state.toggleViewMode()
            state.viewMode shouldBe TagDetailPlaceViewMode.LIST
        }

        test("TC-TAG-DETAIL-PLACE-FEATURE-019 목록 모드에서는 장소 추가로 넘길 지도 위치가 없다") {
            val state = TagDetailPlaceState()
            state.moveMap(coordinate = fixtureMonkey.giveMeOne<Coordinate>())

            state.addCoordinate.shouldBeNull()
        }

        test("TC-TAG-DETAIL-PLACE-FEATURE-031 지도 모드에서는 보고 있던 지도 위치를 장소 추가로 넘긴다") {
            val coordinate = fixtureMonkey.giveMeOne<Coordinate>()
            val state = TagDetailPlaceState(initialViewMode = TagDetailPlaceViewMode.MAP)

            state.moveMap(coordinate = coordinate)

            state.addCoordinate shouldBe coordinate
        }

        test("TC-TAG-DETAIL-PLACE-FEATURE-031 지도가 위치를 알려 주기 전에는 장소 추가로 넘길 지도 위치가 없다") {
            val state = TagDetailPlaceState(initialViewMode = TagDetailPlaceViewMode.MAP)

            state.addCoordinate.shouldBeNull()
        }

        test("TC-TAG-DETAIL-PLACE-DOMAIN-005 목록 모드로 되돌려도 보고 있던 지도 위치는 유지한다") {
            val coordinate = fixtureMonkey.giveMeOne<Coordinate>()
            val state = TagDetailPlaceState(initialViewMode = TagDetailPlaceViewMode.MAP)
            state.moveMap(coordinate = coordinate)

            state.toggleViewMode()
            state.toggleViewMode()

            state.coordinate shouldBe coordinate
            state.addCoordinate shouldBe coordinate
        }
    }) {
    private companion object {
        private val fixtureMonkey: FixtureMonkey =
            diaryFixtureMonkey()
    }
}
