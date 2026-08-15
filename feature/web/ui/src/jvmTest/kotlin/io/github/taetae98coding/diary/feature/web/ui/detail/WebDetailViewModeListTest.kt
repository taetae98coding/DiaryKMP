package io.github.taetae98coding.diary.feature.web.ui.detail

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe

class WebDetailViewModeListTest :
    FunSpec({
        test("표시 방식은 URL, 응답 본문 순서다") {
            webDetailViewModeList shouldContainExactly
                listOf(
                    WebDetailViewMode.URL,
                    WebDetailViewMode.RESPONSE,
                )
        }

        test("표시 방식 목록은 모든 표시 방식을 한 번씩만 담는다") {
            webDetailViewModeList.toSet() shouldBe WebDetailViewMode.entries.toSet()
            webDetailViewModeList.size shouldBe WebDetailViewMode.entries.size
        }
    })
