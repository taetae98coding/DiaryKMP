package io.github.taetae98coding.diary.feature.tag.ui.detail.tab

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe

class TagDetailTabListTest :
    FunSpec({
        test("TC-79088e46-429f-4d1f-b42c-47c46d916a42 탭은 태그 디테일, 메모, 웹, 장소, 연락처, 체크리스트, 곡, QR, 디데이, 루틴 순서다") {
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
                    TagDetailTab.DDAY,
                    TagDetailTab.ROUTINE,
                )
        }

        test("탭 목록은 모든 탭을 한 번씩만 담는다") {
            tagDetailTabList.toSet() shouldBe TagDetailTab.entries.toSet()
            tagDetailTabList.size shouldBe TagDetailTab.entries.size
        }
    })
