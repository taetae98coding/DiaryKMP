package io.github.taetae98coding.diary.feature.dday.ui.form

import io.github.taetae98coding.diary.core.model.dday.DDayOverflowPolicy
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder

class DDayOverflowPolicyListTest :
    FunSpec({
        test("말일로 당김, 표시하지 않음, 다음 달 1일로 미룸 순서로 둔다") {
            ddayOverflowPolicyList shouldContainExactly
                listOf(
                    DDayOverflowPolicy.CLAMP_TO_MONTH_END,
                    DDayOverflowPolicy.SKIP,
                    DDayOverflowPolicy.NEXT_MONTH_FIRST_DAY,
                )
        }

        test("모든 달력에 없는 날짜 처리를 한 번씩만 담는다") {
            ddayOverflowPolicyList shouldContainExactlyInAnyOrder DDayOverflowPolicy.entries
        }
    })
