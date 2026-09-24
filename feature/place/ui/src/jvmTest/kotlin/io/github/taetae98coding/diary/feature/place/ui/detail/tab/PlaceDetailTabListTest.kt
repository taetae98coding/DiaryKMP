package io.github.taetae98coding.diary.feature.place.ui.detail.tab

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe

class PlaceDetailTabListTest :
    FunSpec({
        test("TC-PLACE-DETAIL-FEATURE-043 탭은 장소 디테일, 메모 순서다") {
            placeDetailTabList shouldContainExactly listOf(PlaceDetailTab.DETAIL, PlaceDetailTab.MEMO)
        }

        test("탭 목록은 모든 탭을 한 번씩만 담는다") {
            placeDetailTabList.toSet() shouldBe PlaceDetailTab.entries.toSet()
            placeDetailTabList.size shouldBe PlaceDetailTab.entries.size
        }
    })
