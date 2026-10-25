package io.github.taetae98coding.diary.feature.place.ui.home

import io.github.taetae98coding.diary.feature.place.ui.home.viewmode.PlaceHomeViewMode
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class PlaceHomeScaffoldStateTest :
    FunSpec({
        test("TC-6e8c1c58-3b06-481a-a8cd-f214abf35584 TC-cb8ecf01-7e1a-4a58-b38c-8bb28afd285a 새 화면은 지도 모드로 시작한다") {
            PlaceHomeScaffoldState().viewMode shouldBe PlaceHomeViewMode.MAP
        }

        test("보기 모드 전환은 지도 모드와 목록 모드를 오간다") {
            val state = PlaceHomeScaffoldState()

            state.toggleViewMode()
            state.viewMode shouldBe PlaceHomeViewMode.LIST

            state.toggleViewMode()
            state.viewMode shouldBe PlaceHomeViewMode.MAP
        }
    })
