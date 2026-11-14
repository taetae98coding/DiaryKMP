package io.github.taetae98coding.diary.feature.routine.ui.form

import io.github.taetae98coding.diary.core.model.routine.RoutineCycle
import io.github.taetae98coding.diary.core.testing.routine.routineAmount
import io.github.taetae98coding.diary.core.testing.routine.routineUnit
import io.github.taetae98coding.diary.domain.routine.RoutineInput
import io.github.taetae98coding.diary.library.fixture.kotlinxLocalDate
import io.kotest.property.Arb
import io.kotest.property.arbitrary.arbitrary
import io.kotest.property.arbitrary.enum
import io.kotest.property.arbitrary.long
import io.kotest.property.arbitrary.orNull
import io.kotest.property.arbitrary.string

internal fun Arb.Companion.routineInput(): Arb<RoutineInput> =
    arbitrary {
        RoutineInput(
            title = Arb.string().bind(),
            description = Arb.string().bind(),
            color = Arb.long().bind(),
            startDate = Arb.kotlinxLocalDate().bind(),
            cycle = Arb.enum<RoutineCycle>().bind(),
            unit = Arb.routineUnit().bind(),
            goalAmount = Arb.routineAmount().orNull().bind(),
            amountPerRecord = Arb.routineAmount().bind(),
        )
    }
