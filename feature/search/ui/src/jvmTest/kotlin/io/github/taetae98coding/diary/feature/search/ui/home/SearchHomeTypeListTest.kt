package io.github.taetae98coding.diary.feature.search.ui.home

import io.github.taetae98coding.diary.feature.search.api.SearchHomeType
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe

class SearchHomeTypeListTest :
    FunSpec({
        test("유형은 메모, 태그, 장소, 웹 순서다") {
            searchHomeTypeList shouldContainExactly
                listOf(
                    SearchHomeType.MEMO,
                    SearchHomeType.TAG,
                    SearchHomeType.PLACE,
                    SearchHomeType.WEB,
                )
        }

        test("유형 목록은 모든 유형을 한 번씩만 담는다") {
            searchHomeTypeList.toSet() shouldBe SearchHomeType.entries.toSet()
            searchHomeTypeList.size shouldBe SearchHomeType.entries.size
        }
    })
