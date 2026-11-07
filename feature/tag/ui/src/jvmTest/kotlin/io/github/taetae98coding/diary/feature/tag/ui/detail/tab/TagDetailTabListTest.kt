package io.github.taetae98coding.diary.feature.tag.ui.detail.tab

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe

class TagDetailTabListTest :
    FunSpec({
        test("TC-b0b32237-2208-49a9-8108-e6028692e8e5 탭은 태그 디테일, 메모, 웹, 장소, 연락처, 체크리스트, 곡, QR 순서다") {
            tagDetailTabList shouldContainExactly
                listOf(
                    TagDetailTab.DETAIL,
                    TagDetailTab.MEMO,
                    TagDetailTab.WEB,
                    TagDetailTab.PLACE,
                    TagDetailTab.CONTACT,
                    TagDetailTab.CHECKLIST,
                    TagDetailTab.MUSIC,
                    TagDetailTab.QR,
                )
        }

        test("탭 목록은 모든 탭을 한 번씩만 담는다") {
            tagDetailTabList.toSet() shouldBe TagDetailTab.entries.toSet()
            tagDetailTabList.size shouldBe TagDetailTab.entries.size
        }
    })
