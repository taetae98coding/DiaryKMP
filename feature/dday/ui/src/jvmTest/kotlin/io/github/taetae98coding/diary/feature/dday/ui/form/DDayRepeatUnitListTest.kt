package io.github.taetae98coding.diary.feature.dday.ui.form

import io.github.taetae98coding.diary.domain.dday.repeat.DDayRepeatUnit
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder

class DDayRepeatUnitListTest :
    FunSpec({
        test("일, 개월, 년 순서로 둔다") {
            ddayRepeatUnitList shouldContainExactly
                listOf(
                    DDayRepeatUnit.DAY,
                    DDayRepeatUnit.MONTH,
                    DDayRepeatUnit.YEAR,
                )
        }

        test("모든 반복 간격 단위를 한 번씩만 담는다") {
            ddayRepeatUnitList shouldContainExactlyInAnyOrder DDayRepeatUnit.entries
        }
    })
