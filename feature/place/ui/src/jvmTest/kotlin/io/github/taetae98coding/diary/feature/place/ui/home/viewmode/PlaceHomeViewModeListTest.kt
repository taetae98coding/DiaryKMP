package io.github.taetae98coding.diary.feature.place.ui.home.viewmode

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe

class PlaceHomeViewModeListTest :
    FunSpec({
        test("보기 모드는 지도 모드, 목록 모드 순서다") {
            placeHomeViewModeList shouldContainExactly
                listOf(
                    PlaceHomeViewMode.MAP,
                    PlaceHomeViewMode.LIST,
                )
        }

        test("보기 모드 목록은 모든 모드를 한 번씩만 담는다") {
            placeHomeViewModeList.toSet() shouldBe PlaceHomeViewMode.entries.toSet()
            placeHomeViewModeList.size shouldBe PlaceHomeViewMode.entries.size
        }
    })
