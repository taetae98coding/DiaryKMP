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
        test("TC-f2d66fa6-c99d-4cb9-af58-b63460e7bf88 TC-a7b1b209-3b0a-4564-af5d-f1ae346d8568 새로 만든 상태는 목록 모드로 시작한다") {
            TagDetailPlaceState().viewMode shouldBe TagDetailPlaceViewMode.LIST
        }

        test("TC-8243e022-52a0-4d7c-b764-a3ee5f9c965e TC-f58c43db-7374-44b4-8cb8-4c166bbdfb50 전환할 때마다 두 보기 모드를 오간다") {
            val state = TagDetailPlaceState()

            state.toggleViewMode()
            state.viewMode shouldBe TagDetailPlaceViewMode.MAP

            state.toggleViewMode()
            state.viewMode shouldBe TagDetailPlaceViewMode.LIST
        }

        test("TC-25b808f4-b089-4a0b-8555-a06a86632ff6 목록 모드에서는 장소 추가로 넘길 지도 위치가 없다") {
            val state = TagDetailPlaceState()
            state.moveMap(coordinate = fixtureMonkey.giveMeOne<Coordinate>())

            state.addCoordinate.shouldBeNull()
        }

        test("TC-9a764db7-36f2-4db2-960c-f503a819a9ab 지도 모드에서는 보고 있던 지도 위치를 장소 추가로 넘긴다") {
            val coordinate = fixtureMonkey.giveMeOne<Coordinate>()
            val state = TagDetailPlaceState(initialViewMode = TagDetailPlaceViewMode.MAP)

            state.moveMap(coordinate = coordinate)

            state.addCoordinate shouldBe coordinate
        }

        test("TC-9a764db7-36f2-4db2-960c-f503a819a9ab 지도가 위치를 알려 주기 전에는 장소 추가로 넘길 지도 위치가 없다") {
            val state = TagDetailPlaceState(initialViewMode = TagDetailPlaceViewMode.MAP)

            state.addCoordinate.shouldBeNull()
        }

        test("TC-efccf1d0-a4ca-4aa2-8366-52fa1f9234d1 목록 모드로 되돌려도 보고 있던 지도 위치는 유지한다") {
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
