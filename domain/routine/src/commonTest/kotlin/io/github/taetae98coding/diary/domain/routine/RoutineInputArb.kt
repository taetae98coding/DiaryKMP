package io.github.taetae98coding.diary.domain.routine

import io.github.taetae98coding.diary.core.testing.routine.routineDetail
import io.github.taetae98coding.diary.library.fixture.nonBlankString
import io.kotest.property.Arb
import io.kotest.property.arbitrary.arbitrary

// 유효 범위를 만족하는 입력이다. 제목은 비어 있지 않게 채운다.
internal fun Arb.Companion.routineInput(): Arb<RoutineInput> =
    arbitrary {
        val detail = Arb.routineDetail().bind()

        RoutineInput(
            title = Arb.nonBlankString().bind(),
            description = detail.description,
            color = detail.color,
            startDate = detail.startDate,
            cycle = detail.cycle,
            unit = detail.unit,
            goalAmount = detail.goalAmount,
            amountPerRecord = detail.amountPerRecord,
        )
    }
