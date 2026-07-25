package io.github.taetae98coding.diary.compose.core.input

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe

class DiaryDescriptionInputPageListTest :
    FunSpec({
        test("페이지는 입력, 미리보기 순서다") {
            diaryDescriptionInputPageList shouldContainExactly
                listOf(
                    DiaryDescriptionInputPage.Input,
                    DiaryDescriptionInputPage.Preview,
                )
        }

        test("페이지 목록은 모든 페이지를 한 번씩만 담는다") {
            diaryDescriptionInputPageList.toSet() shouldBe DiaryDescriptionInputPage.entries.toSet()
            diaryDescriptionInputPageList.size shouldBe DiaryDescriptionInputPage.entries.size
        }
    })
