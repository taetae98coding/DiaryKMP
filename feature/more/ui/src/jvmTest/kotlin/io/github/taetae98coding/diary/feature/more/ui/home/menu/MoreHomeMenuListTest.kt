package io.github.taetae98coding.diary.feature.more.ui.home.menu

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe

class MoreHomeMenuListTest :
    FunSpec({
        test("기능 바로가기는 한글이 아닌 이름을 앞에 두고 가나다순으로 둔다") {
            moreHomeMenuList shouldContainExactly
                listOf(
                    MoreHomeMenu.QR,
                    MoreHomeMenu.SEARCH,
                    MoreHomeMenu.D_DAY,
                    MoreHomeMenu.CONTACT,
                    MoreHomeMenu.WEB,
                    MoreHomeMenu.PLACE,
                    MoreHomeMenu.CHECKLIST,
                    MoreHomeMenu.FILE,
                    MoreHomeMenu.PLAYLIST,
                    MoreHomeMenu.HOLIDAY,
                )
        }

        test("기능 바로가기 목록은 모든 항목을 한 번씩만 담는다") {
            moreHomeMenuList.toSet() shouldBe MoreHomeMenu.entries.toSet()
            moreHomeMenuList.size shouldBe MoreHomeMenu.entries.size
        }
    })
