package io.github.taetae98coding.diary.domain.dday

import io.github.taetae98coding.diary.core.testing.dday.ddayDetail
import io.github.taetae98coding.diary.domain.dday.repeat.toRepeatInputList
import io.github.taetae98coding.diary.library.fixture.nonBlankString
import io.kotest.property.Arb
import io.kotest.property.arbitrary.arbitrary

// 기록할 수 있는 간격만 담은 입력이다. 저장된 간격 모음을 입력 모양으로 되돌려 만들므로 기록하면 같은 간격 모음이 된다.
internal fun Arb.Companion.ddayAddInput(): Arb<DDayInput> =
    arbitrary {
        val detail = Arb.ddayDetail().bind()

        DDayInput(
            title = Arb.nonBlankString().bind(),
            description = detail.description,
            color = detail.color,
            baseDate = detail.baseDate,
            countsBaseDay = detail.countsBaseDay,
            repeatList = detail.repeats.toRepeatInputList(),
            endDate = detail.endDate,
            overflowPolicy = detail.overflowPolicy,
        )
    }
