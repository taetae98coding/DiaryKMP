package io.github.taetae98coding.diary.feature.memo.ui.gemini

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder

class MemoGeminiFieldListTest :
    FunSpec({
        test("결과는 제목, 설명, 날짜·시간 순서로 놓인다") {
            memoGeminiFieldList shouldContainExactly
                listOf(
                    MemoGeminiField.TITLE,
                    MemoGeminiField.DESCRIPTION,
                    MemoGeminiField.DATE_TIME,
                )
        }

        test("모든 결과 종류를 한 번씩만 담는다") {
            memoGeminiFieldList shouldContainExactlyInAnyOrder MemoGeminiField.entries
        }
    })
