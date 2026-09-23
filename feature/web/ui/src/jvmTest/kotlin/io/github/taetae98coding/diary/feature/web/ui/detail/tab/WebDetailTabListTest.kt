package io.github.taetae98coding.diary.feature.web.ui.detail.tab

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe

class WebDetailTabListTest :
    FunSpec({
        test("탭은 수정 폼, 웹 페이지 순서다") {
            webDetailTabList shouldContainExactly
                listOf(
                    WebDetailTab.FORM,
                    WebDetailTab.PAGE,
                )
        }

        test("탭 목록은 모든 탭을 한 번씩만 담는다") {
            webDetailTabList.toSet() shouldBe WebDetailTab.entries.toSet()
            webDetailTabList.size shouldBe WebDetailTab.entries.size
        }
    })
